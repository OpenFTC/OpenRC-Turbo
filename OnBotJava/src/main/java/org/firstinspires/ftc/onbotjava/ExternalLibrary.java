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

import com.qualcomm.robotcore.util.RobotLog;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotserver.internal.webserver.RobotWebHandlerManager;

/**
 * A class represents an uploaded external library.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
class ExternalLibrary {
  private static final String TAG = "ExternalLibrary";

  final File uploadedFile;
  final String name;
  private final File subdirectory;
  private final File dexFileParent;
  private final Set<File> jarFiles = new HashSet<>();
  private final Set<File> soFiles = new HashSet<>();
  private final Set<File> dexFiles = new HashSet<>();
  private final Set<String> classNames = new HashSet<>();

  /**
   * Creates a new ExternalLibrary. This contructor does not initialize the data for this instance
   * and should be followed immediately by processUploadedFile (when a file has just been uploaded)
   * or load (at startup).
   */
  ExternalLibrary(File uploadedFile) {
    this.uploadedFile = uploadedFile;
    this.name = uploadedFile.getName();

    File subdirectory = null;
    if (name.endsWith(".jar")) {
      subdirectory = new File(OnBotJavaManager.extLibDir, replaceSuffix(name, ".jar", "_jar"));
    } else if (name.endsWith(".aar")) {
      subdirectory = new File(OnBotJavaManager.extLibDir, replaceSuffix(name, ".aar", "_aar"));
    }
    this.subdirectory = subdirectory;
    dexFileParent = new File(subdirectory, "dex");
  }

  /**
   * Process the uploaded file. This is called when the file is first uploaded.
   * Returns an HTTP response.
   */
  NanoHTTPD.Response onUpload() {
    if (subdirectory != null) {
      subdirectory.mkdirs();
    }
    if (dexFileParent != null) {
      dexFileParent.mkdirs();
    }

    NanoHTTPD.Response response;

    if (name.endsWith(".jar")) {
      response = onUploadJarFile();

    } else if (name.endsWith(".aar")) {
      response = onUploadAarFile();

    } else {
      RobotLog.ee(TAG, "onUpload: unexpected file " + name);
      return null;
    }

    if (response.getStatus() != NanoHTTPD.Response.Status.OK) {
      AppUtil.getInstance().delete(subdirectory);
    }

    return response;
  }

  /**
   * Collects class names and dexifies the jar file.
   * Returns an HTTP response.
   */
  private NanoHTTPD.Response onUploadJarFile() {
    jarFiles.add(uploadedFile);

    String errorMessage = collectClassNames();
    if (errorMessage != null) {
      return RobotWebHandlerManager.clientBadRequestError(TAG, errorMessage);
    }

    boolean dexifySuccess = dexify();
    if (!dexifySuccess) {
      return RobotWebHandlerManager.clientBadRequestError(TAG,
          "Error: The file " + name + " can not be used because it could not be dexified.");
    }
    dexFiles.addAll(AppUtil.getInstance().filesUnder(dexFileParent, ".dex"));

    return StandardResponses.successfulRequest("");
  }

  /**
   * Unpacks the aar, collects class names, and dexifies included jar files.
   * Returns an HTTP response.
   */
  private NanoHTTPD.Response onUploadAarFile() {
    // Unpack the .aar file and collect the .jar and .so files.
    try {
      OnBotJavaManager.unpackZipFile(uploadedFile, subdirectory);
    } catch (IOException e) {
      String errorMessage = "Error: The file " + name + " could not be processed for FTC.";
      return RobotWebHandlerManager.clientBadRequestError(TAG, errorMessage);
    }

    jarFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".jar"));
    soFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".so"));

    String errorMessage = collectClassNames();
    if (errorMessage != null) {
      return RobotWebHandlerManager.clientBadRequestError(TAG, errorMessage);
    }

    boolean dexifySuccess = dexify();
    if (!dexifySuccess) {
      return RobotWebHandlerManager.clientBadRequestError(TAG,
          "Error: The file " + name +
          " can not be used because it contains .jar file(s) that could not be dexified.");
    }
    dexFiles.addAll(AppUtil.getInstance().filesUnder(dexFileParent, ".dex"));

    return StandardResponses.successfulRequest("");
  }

  /**
   * Dexifies the .jar files for this ExternalLibrary
   * Returns true if successful, false otherwise.
   */
  private boolean dexify() {
    // Dexify the jar file(s).
    try {
      OnBotJavaDiagnosticsListener diagnosticListener = new OnBotJavaDiagnosticsListener(null);
      try {
        // Use getJarFiles() instead of jarFiles because we want to pass an unmodifiable
        // collection.
        OnBotJavaManager.dexifyFiles(getJarFiles(), dexFileParent, diagnosticListener);
      } catch (Exception e) {
        RobotLog.ee(TAG, e, "dexify failed");
        return false;
      }
      finally {
        diagnosticListener.flush();
        diagnosticListener.close();
      }
    } catch (IOException e) {
      RobotLog.ee(TAG, e, "Could not flush/close the diagnosticListener");
    }

    return true;
  }

  /**
   * Fills in the data for this instance. This is called at startup for each ExternalLibrary that
   * was uploaded and processed previously.
   * Returns null if successful, or an error message if not successful.
   */
  String onCreate() {
    String errorMessage = null;

    if (name.endsWith(".jar")) {
      errorMessage = onCreateJarFile();
    } else if (name.endsWith(".aar")) {
      errorMessage = onCreateAarFile();
    }

    return errorMessage;
  }

  private String onCreateJarFile() {
    jarFiles.add(uploadedFile);
    dexFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".dex"));
    return collectClassNames();
  }

  private String onCreateAarFile() {
    jarFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".jar"));
    soFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".so"));
    dexFiles.addAll(AppUtil.getInstance().filesUnder(subdirectory, ".dex"));
    return collectClassNames();
  }

  /**
   * Collects the names of classes in the .jar files for this ExternalLibrary, checking to see if
   * any classes are already in the robot controller app or another external library.
   * This method is used at startup and also when a .jar or .aar file is uploaded.
   * Returns null if successful, or an error message if not successful.
   */
  private String collectClassNames() {
    for (File dotJarFile : jarFiles) {
      try {
        JarFile jarFile = new JarFile(dotJarFile);
        try {
          for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
              continue;
            }
            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
              String className = entryName.substring(0, entryName.lastIndexOf('.'))
                  .replace('/', '.');

              // Check whether another external library already has this class.
              if (ExternalLibraries.getInstance().isClassIncludedInAnotherExternalLibrary(this, className)) {
                  return "Error: The file " + name + " can not be " +
                      "used because it contains classes that already exist in another " +
                      "external library.";
              }

              // Check whether the robot controller app already has this class.
              if (isClassIncludedInRobotControllerApp(className)) {
                return "Error: The file " + name + " can not be " +
                    "used because it contains classes that already exist in the " +
                    "FtcRobotController app.";
              }
              classNames.add(className);
            }
          }
        } finally {
          jarFile.close();
        }
      } catch (IOException e) {
        RobotLog.ee(TAG, e, "collectClassNames");
        return "Error: The file " + name + " could not be processed for FTC.";
      }
    }
    return null; // success
  }

  /**
   * Returns true if this external library contains the class with the given class name, false
   * otherwise.
   */
  boolean containsClassName(String className) {
    return classNames.contains(className);
  }

  /**
   * Returns the time that this external library was last modified (the upload time).
   */
  long getLastModified() {
    return uploadedFile.lastModified();
  }

  /**
   * Returns a collection of the .jar files that are part of the external library.
   */
  Collection<File> getJarFiles() {
    return Collections.unmodifiableCollection(jarFiles);
  }

  /**
   * Returns a collection of the .so files that are part of the external library.
   */
  Collection<File> getSoFiles() {
    return Collections.unmodifiableCollection(soFiles);
  }

  /**
   * Returns a collection of the .dex files that are part of the external library.
   */
  Collection<File> getDexFiles() {
    return Collections.unmodifiableCollection(dexFiles);
  }

  /**
   * Returns a collection of the class names that are part of the external library.
   */
  Collection<String> getClassNames() {
    return Collections.unmodifiableCollection(classNames);
  }

  /**
   * Deletes all files associated with this external library.
   */
  void delete() {
    AppUtil appUtil = AppUtil.getInstance();
    if (uploadedFile.exists()) {
      appUtil.delete(uploadedFile);
    }
    if (subdirectory != null && subdirectory.exists()) {
      appUtil.delete(subdirectory);
    }
  }

  // java.lang.Object methods

  @Override
  public boolean equals(Object o) {
    if (o instanceof ExternalLibrary) {
      ExternalLibrary that = (ExternalLibrary) o;
      return this.uploadedFile.equals(that.uploadedFile);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return uploadedFile.hashCode();
  }

  // static methods

  /**
   * Returns true if the given className represents a class that can be loaded by the ClassLoader
   * that loaded this class.
   */
  private static boolean isClassIncludedInRobotControllerApp(String className) {
    try {
      return ExternalLibrary.class.getClassLoader().loadClass(className) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static String replaceSuffix(String s, String oldSuffix, String newSuffix) {
    return s.substring(0, s.length() - oldSuffix.length()) + newSuffix;
  }
}
