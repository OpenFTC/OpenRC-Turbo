// Author lizlooney@google.com
// Based on https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/ArchiveProgramResourceProvider.java
// and https://r8.googlesource.com/r8/+/master/src/main/java/com/android/tools/r8/ArchiveClassFileProvider.java

package dk.sgjesse.r8api;

import com.android.tools.r8.ClassFileResourceProvider;
import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

/**
 * D8/R8 class file resource provider for class files in an archive. This provider does not use the
 * java.nio.file APIs which are only present on Android from API level 26.
 */
public class ArchiveClassFileResourceProvider implements ClassFileResourceProvider, Closeable {
  private final File archive;
  private final Origin origin;
  private final ZipFile zipFile;
  private final Set<String> descriptors = new HashSet<>();
  private volatile boolean closed;

  public ArchiveClassFileResourceProvider(File archive) throws IOException {
    this.archive = archive;
    this.origin = new FileOrigin(archive);

    zipFile = new ZipFile(archive);
    for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
      ZipEntry entry = entries.nextElement();
      String entryName = entry.getName();
      if (ZipUtils.isClassFile(entryName)) {
        descriptors.add(DescriptorUtils.guessTypeDescriptor(entryName));
      }
    }
  }

  @Override
  public Set<String> getClassDescriptors() {
    return Collections.unmodifiableSet(descriptors);
  }

  @Override
  public ProgramResource getProgramResource(String descriptor) {
    if (!descriptors.contains(descriptor)) {
      return null;
    }
    ZipEntry entry = zipFile.getEntry(DescriptorUtils.getPathFromDescriptor(descriptor));
    String name = entry.getName();
    Origin entryOrigin = new ArchiveEntryOrigin(name, origin);
    try (InputStream stream = zipFile.getInputStream(entry)) {
      return ProgramResource.fromBytes(
          entryOrigin,
          ProgramResource.Kind.CF,
          ZipUtils.toByteArray(stream),
          Collections.singleton(descriptor));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read '" + descriptor + " from " + archive.getAbsolutePath(), e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      descriptors.clear();
      zipFile.close();
    }
  }
}
