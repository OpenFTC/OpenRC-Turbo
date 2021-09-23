// From https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/AndroidDexIndexedConsumer.java

package dk.sgjesse.r8api;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.utils.ExceptionDiagnostic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * D8/R8 Consumer for DEX programs compatible with Android. This consumer does not use the
 * java.nio.file APIs which are only present on Android from API level 26.
 */
public class AndroidDexIndexedConsumer extends DexIndexedConsumer.ForwardingConsumer {
    private final File destination;
    private final SortedMap<String, ByteDataView> dexFiles = new TreeMap<>();

    public AndroidDexIndexedConsumer(File destination) {
        super(null);
        this.destination = destination;
    }

    @Override
    public void finished(DiagnosticsHandler handler) {
        // Write the DEX files sorted by name.
        try {
            ZipOutputStream stream =
                    new ZipOutputStream(
                            new BufferedOutputStream(
                                    new FileOutputStream(destination)));

            // lizlooney added the following if to prevent us from dying while writing an empty
            // zip.
            if (dexFiles.isEmpty()) {
              stream.putNextEntry(new ZipEntry("README.txt"));
              stream.write("Created by FTC SDK.".getBytes());
              stream.closeEntry();
            }

            for (Map.Entry<String, ByteDataView> entry : dexFiles.entrySet()) {
                ZipUtils.writeToZipStream(
                        stream, entry.getKey(), entry.getValue(), ZipEntry.DEFLATED);
            }
            stream.close();
        } catch (IOException e) {
            handler.error(new ExceptionDiagnostic(e));
        }
    }

    @Override
    public void accept(
            int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler) {
        String name = getDefaultDexFileName(fileIndex);
        // Buffer DEX content to ensure deterministic ZIP file. Data is released in the
        // application writer, take a copy.
        dexFiles.put(name, ByteDataView.of(data.copyByteData()));
    }

    // TODO(b/180261430): This method is in DexIndexedConsumer.ForwardingConsumer, but is not kept.
    private String getDefaultDexFileName(int fileIndex) {
        return "classes" + (fileIndex == 0 ? "" : (fileIndex + 1)) + FileUtils.DEX_EXTENSION;
    }
}
