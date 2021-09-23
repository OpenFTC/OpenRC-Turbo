// From https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/ArchiveProgramResourceProvider.java

package dk.sgjesse.r8api;

import com.android.tools.r8.ProgramResource;
import com.android.tools.r8.ProgramResourceProvider;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.origin.ArchiveEntryOrigin;
import com.android.tools.r8.origin.Origin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * D8/R8 program resource provider for class files in an archive. This provider does not use the
 * java.nio.file APIs which are only present on Android from API level 26.
 */
public class ArchiveProgramResourceProvider implements ProgramResourceProvider {
    private final File archive;
    private final Origin origin;

    public ArchiveProgramResourceProvider(File archive) {
        this.archive = archive;
        this.origin = new FileOrigin(archive);
    }

    @Override
    public Collection<ProgramResource> getProgramResources() throws ResourceException {
        List<ProgramResource> programResources = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(archive)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (ZipUtils.isClassFile(name)) {
                    Origin entryOrigin = new ArchiveEntryOrigin(name, origin);
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        String descriptor = DescriptorUtils.guessTypeDescriptor(name);
                        programResources.add(
                                ProgramResource.fromBytes(
                                        entryOrigin,
                                        ProgramResource.Kind.CF,
                                        ZipUtils.toByteArray(stream),
                                        Collections.singleton(descriptor)));
                    }
                }
            }
        } catch (IOException e) {
            throw new ResourceException(origin, e);
        }
        return programResources;
    }
}
