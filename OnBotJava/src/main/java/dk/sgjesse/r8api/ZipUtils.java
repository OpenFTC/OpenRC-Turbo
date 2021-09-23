// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// From https://github.com/sgjesse/r8api/tree/0f41b95db680ddc98fd7197f31a800f4ed56816b/
// r8api/src/main/java/dk/sgjesse/r8api/ZipUtils.java
// From https://r8.googlesource.com/r8/+/master/src/main/java/com/android/tools/r8/utils/ZipUtils.java
package dk.sgjesse.r8api;

import com.android.tools.r8.ByteDataView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import static dk.sgjesse.r8api.FileUtils.CLASS_EXTENSION;
import static dk.sgjesse.r8api.FileUtils.MODULE_INFO_CLASS;

/**
 * Utilities for ZIP files.
 */
class ZipUtils {
    public static boolean isClassFile(String entry) {
        String name = entry.toLowerCase();
        if (name.endsWith(MODULE_INFO_CLASS)) {
            return false;
        }
        if (name.startsWith("meta-inf") || name.startsWith("/meta-inf")) {
            return false;
        }
        return name.endsWith(CLASS_EXTENSION);
    }

    public static void writeToZipStream(
            ZipOutputStream stream, String entry, ByteDataView content, int compressionMethod)
            throws IOException {
        byte[] buffer = content.getBuffer();
        int offset = content.getOffset();
        int length = content.getLength();
        CRC32 crc = new CRC32();
        crc.update(buffer, offset, length);
        ZipEntry zipEntry = new ZipEntry(entry);
        zipEntry.setMethod(compressionMethod);
        zipEntry.setSize(length);
        zipEntry.setCrc(crc.getValue());
        zipEntry.setTime(0);
        stream.putNextEntry(zipEntry);
        stream.write(buffer, offset, length);
        stream.closeEntry();
    }

    // Added by lizlooney to use instead of com.google.common.io.ByteStreams.toByteArray.
    public static byte[] toByteArray(InputStream is) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      AppUtil.getInstance().copyStream(is, os);
      return os.toByteArray();
    }
}
