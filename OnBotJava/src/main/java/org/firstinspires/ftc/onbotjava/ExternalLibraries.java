/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.firstinspires.ftc.onbotjava;

import android.os.Process;
import androidx.annotation.Nullable;
import com.qualcomm.robotcore.util.RobotLog;
import fi.iki.elonen.NanoHTTPD;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.internal.opmode.ClassManager;
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class ExternalLibraries {
  private static final String TAG = "ExternalLibraries";

  private static final Comparator<File> compareByLastModifiedTime = new Comparator<File>() {
    @Override
    public int compare(File a, File b) {
      return Long.compare(a.lastModified(), b.lastModified());
    }
  };

  private static String jniSubdirPrefix;

  private final Map<File, ExternalLibrary> map = new TreeMap<>(compareByLastModifiedTime);
  private @Nullable ClassLoader classLoader;

  private static class InstanceHolder {
    private static ExternalLibraries theInstance = new ExternalLibraries();
  }

  public static ExternalLibraries getInstance() {
    return InstanceHolder.theInstance;
  }

  /**
   * Loads all the external libraries that have been uploaded and creates a class loader.
   */
  public void onCreate() {
    jniSubdirPrefix = getJniSubdirPrefix();

    List<File> uploadedFiles = AppUtil.getInstance().filesIn(OnBotJavaManager.extLibDir,
        new Predicate<File>() {
          @Override
          public boolean test(File file) {
            return file.isFile() && isExternalLibrariesFile(file.getName());
          }
        });
    // Sort uploadedFiles by last modified time so we go through them in upload order. That way if
    // one is rejected for having already existing classes, it is deterministic as to which one is
    // rejected.
    Collections.sort(uploadedFiles, compareByLastModifiedTime);

    // Create the ExternalLibrary instances and gather what we need to create a class loader.
    List<File> dexFiles = new ArrayList<>();
    List<File> soFiles = new ArrayList<>();
    for (File uploadedFile : uploadedFiles) {
      ExternalLibrary externalLibrary = new ExternalLibrary(uploadedFile);
      String errorMessage = externalLibrary.onCreate();
      if (errorMessage == null) {
        map.put(uploadedFile, externalLibrary);
        dexFiles.addAll(externalLibrary.getDexFiles());
        soFiles.addAll(externalLibrary.getSoFiles());
      }
    }

    classLoader = createClassLoader(null, dexFiles, soFiles, true /* deleteOrphanedSoFiles */);
  }

  private static ClassLoader createClassLoader(ClassLoader parentClassLoader,
      Collection<File> dexFiles, Collection<File> soFiles, boolean deleteOrphanedSoFiles) {
    AppUtil appUtil = AppUtil.getInstance();
    List<File> librarySearchPathFiles = new ArrayList<>();
    List<File> librarySearchPathDirs = new ArrayList<>();
    for (File soFile : soFiles) {
      // In order to load a .so file, it must be under the app's private internal storage
      // folder. So, here we copy of the .so file to there.
      try {
        File srcParentDir = soFile.getParentFile();
        if (!srcParentDir.getName().startsWith(jniSubdirPrefix)) {
          // This .so file is not the one we need. It is either 64-bit and we need 32-bit
          // or it is 32-bit and we need 64-bit.
          continue;
        }
        String relativePath = appUtil.getRelativePath(AppUtil.FIRST_FOLDER, soFile).getPath();
        File destFile = new File(AppUtil.getDefContext().getFilesDir(), relativePath);
        if (destFile.isDirectory()) {
          appUtil.delete(destFile);
        }
        // Save time by not copying a file that is already there.
        if (!destFile.exists() || !AppUtil.computeMd5(soFile).equals(AppUtil.computeMd5(destFile))) {
          destFile.getParentFile().mkdirs();
          appUtil.copyFile(soFile, destFile);
        }

        librarySearchPathFiles.add(destFile);
        librarySearchPathDirs.add(destFile.getParentFile());
      } catch (Exception e) {
        RobotLog.ee(TAG, e, "Could not use " + soFile.getAbsolutePath());
      }
    }

    if (deleteOrphanedSoFiles) {
      // Remove any .so files under AppUtil.getDefContext.getFilesDir() that aren't in
      // librarySearchPathFiles.
      for (File soFile : appUtil.filesUnder(AppUtil.getDefContext().getFilesDir(), ".so")) {
        if (!librarySearchPathFiles.contains(soFile)) {
          appUtil.delete(soFile);
        }
      }
    }

    if (!dexFiles.isEmpty() || !librarySearchPathDirs.isEmpty()) {
      // Build the dex path.
      StringBuilder dexPath = new StringBuilder();
      String delimiter = "";
      for (File dotDexFile : dexFiles) {
        dexPath.append(delimiter).append(dotDexFile.getAbsolutePath());
        delimiter = File.pathSeparator;
      }

      // Build the library search path.
      StringBuilder librarySearchPath = new StringBuilder();
      delimiter = "";
      for (File dir : librarySearchPathDirs) {
        librarySearchPath.append(delimiter).append(dir.getAbsolutePath());
        delimiter = File.pathSeparator;
      }

      // Create the ClassLoader.
      if (parentClassLoader == null) {
        parentClassLoader = ExternalLibraries.class.getClassLoader();
      }
      return new ExternalLibrariesClassLoader(
          dexPath.toString(), librarySearchPath.toString(), parentClassLoader);
    }
    return null;
  }

  /**
   * Determines whether we are running in 64-bit mode (or 32-bit mode), so we know which native
   * libraries to use. Returns the prefix for the appropriate jni subdirectory within a .aar file.
   */
  private static String getJniSubdirPrefix() {
    String jniSubdirPrefix = "armeabi";
    try {
      // https://stackoverflow.com/questions/8740859/how-to-detect-which-native-shared-libraries-are-loaded-by-android-application
      String mapsFile = "/proc/" + Process.myPid() + "/maps";
      try (BufferedReader reader = new BufferedReader(new FileReader(mapsFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.endsWith(".so")) {
            int n = line.lastIndexOf(" ");
            String lib = line.substring(n + 1);
            // Look for RobotCore, which should be there.
            if (lib.endsWith("libRobotCore.so")) {
              if (lib.contains("64")) {
                jniSubdirPrefix = "arm64";
              }
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      RobotLog.ee(TAG, e, "Unable to determine whether we are running in 64-bit mode.");
    }
    return jniSubdirPrefix;
  }

  /**
   * Returns the names of classes in external libraries.
   * Called from OnBotJavaHelperImpl.getExternalLibrariesClassNames(), which is called from
   * ClassManager.getAllClassNames() and ClassManager.processExternalLibrariesClasses().
   */
  Collection<String> getClassNames() {
    Set<String> classNames = new TreeSet<>();
    for (ExternalLibrary externalLibrary : map.values()) {
      classNames.addAll(externalLibrary.getClassNames());
    }

    return Collections.unmodifiableCollection(classNames);
  }

  /**
   * Returns the ClassLoader for external libraries, if one has been created; otherwise null.
   */
  ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Handles upload of a external library. Called from UploadFiles.
   */
  public NanoHTTPD.Response onUpload(File uploadedFile) {
    ExternalLibrary externalLibrary = new ExternalLibrary(uploadedFile);
    NanoHTTPD.Response response = externalLibrary.onUpload();
    if (response.getStatus() == NanoHTTPD.Response.Status.OK) {
      map.put(uploadedFile, externalLibrary);
      Collection<File> dexFiles = externalLibrary.getDexFiles();
      Collection<File> soFiles = externalLibrary.getSoFiles();

      // Create a new ClassLoader that uses the previous one as the parent.
      ClassLoader newClassLoader = createClassLoader(classLoader, dexFiles, soFiles, false /* deleteOrphanedSoFiles */);
      if (newClassLoader != null) {
        classLoader = newClassLoader;
      }

      ClassManager.getInstance().processExternalLibrariesClasses();
      RegisteredOpModes.getInstance().setExternalLibrariesChanged();

    } else {
      // Uh oh... there's an error. Delete the uploaded file.
      uploadedFile.delete();
    }
    return response;
  }

  /**
   * Returns a list of .jar files from external libraries. This is used when OnBotJava is compiling
   * code with javac and also when we prepare the classpath for d8.
   */
  Collection<File> getClasspathFiles() {
    List<File> classpathFiles = new ArrayList<>();
    for (ExternalLibrary externalLibrary : map.values()) {
      classpathFiles.addAll(externalLibrary.getJarFiles());
    }
    return Collections.unmodifiableCollection(classpathFiles);
  }

  /**
   * Deletes the ExternalLibrary corresponding to the given File. Called from DeleteFile.java.
   */
  public void deleteExternalLibrary(File uploadedFile) {
    ExternalLibrary externalLibrary = map.remove(uploadedFile);
    if (externalLibrary != null) {
      externalLibrary.delete();
    }

    // If there are no .so files, create a brand new ClassLoader. If there are .so files, then we
    // can't create a new ClassLoader because the .so file has already been loaded by the previous
    // ClassLoader and can't be loaded by the new ClassLoader.
    List<File> dexFiles = new ArrayList<>();
    List<File> soFiles = new ArrayList<>();
    for (ExternalLibrary otherExternalLibrary : map.values()) {
      dexFiles.addAll(otherExternalLibrary.getDexFiles());
      soFiles.addAll(otherExternalLibrary.getSoFiles());
    }
    if (soFiles.isEmpty()) {
      classLoader = createClassLoader(null, dexFiles, soFiles, false /* deleteOrphanedSoFiles */);
    }

    ClassManager.getInstance().processExternalLibrariesClasses();
    RegisteredOpModes.getInstance().setExternalLibrariesChanged();
  }

  /**
   * Returns true if the class with the given name is already present in another external library.
   */
  boolean isClassIncludedInAnotherExternalLibrary(@Nullable ExternalLibrary extLib, String className) {
    for (ExternalLibrary externalLibrary : map.values()) {
      if (externalLibrary.equals(extLib)) {
        continue;
      }

      if (externalLibrary.containsClassName(className)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns a list of names of all file that have been uploaded. This is called from
   * FetchFileTree, to provide the file tree to the OnBotJava UI.
   * Note that we include the name of an uploaded file regardless of whether the ExternalLibrary
   * for that file was created successfully. This is so the user can delete the file.
   */
  public List<String> getExternalLibrariesNames() {
    List<String> list = new ArrayList<>();
    for (File f : AppUtil.getInstance().filesIn(OnBotJavaManager.extLibDir)) {
      if (f.isFile()) {
        list.add(f.getName());
      }
    }
    return list;
  }

  /**
   * Returns true if the given filename indicates that the file being uploaded is an external
   * library. Called from UploadFiles.java.
   */
  public boolean isExternalLibrariesFile(String filename) {
    // These file extensions are also included in lib/OnBotJava/src/main/assets/java/editor.html in
    // the file-upload-form.
    return filename.endsWith(".jar") || filename.endsWith(".aar");
  }

  /**
   * Returns true if the given NoClassDefFoundError (or one of its causes) is due to a class from
   * an external library not being found; false otherwise.
   */
  boolean isExternalLibrariesError(NoClassDefFoundError noClassDefFoundError) {
    for (Throwable t = noClassDefFoundError; t != null; t = t.getCause()) {
      if (t instanceof ClassNotFoundException) {
        String className = t.getMessage();
        if (isClassIncludedInAnotherExternalLibrary(null, className)) {
          return true;
        }
      }
    }
    return false;
  }
}
