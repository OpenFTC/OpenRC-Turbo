package com.qualcomm.robotcore.util;

import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@SuppressWarnings("WeakerAccess")
public class ReadWriteFile {

    public static final String TAG = "ReadWriteFile";

    protected static Charset charset = Charset.forName("UTF-8");

    private static final String OLD_VERSION_SUFFIX = ".old_version";

    public static String readFileOrThrow(File file) throws IOException {

        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available());
        try {
            AppUtil.getInstance().copyStream(inputStream, outputStream);
        } finally {
            inputStream.close();
        }

        return charset.decode(ByteBuffer.wrap(outputStream.toByteArray())).toString();
    }

    public static String readFile(File file) {
        try {
          return readFileOrThrow(file);
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "error reading file: %s", file.getPath());
        }
        return "";
    }

    //----------------------------------------------------------------------------------------------

    public static byte[] readBytes(RobotCoreCommandList.FWImage fwImage) {
        if (fwImage.isAsset) {
            return readAssetBytes(fwImage.file);
        } else {
            return readFileBytes(fwImage.file);
        }
    }

    public static byte[] readAssetBytes(File assetFile) {
        try {
            return readAssetBytesOrThrow(assetFile);
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "error reading asset: %s", assetFile.getPath());
        }
        return new byte[0];
    }

    public static byte[] readFileBytes(File file) {
        try {
            return readFileBytesOrThrow(file);
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "error reading file: %s", file.getPath());
        }
        return new byte[0];
    }

    public static byte[] readAssetBytesOrThrow(File assetFile) throws IOException {
        InputStream inputStream = AppUtil.getDefContext().getAssets().open(assetFile.getPath());
        return readBytesOrThrow(0, inputStream);
    }

    public static byte[] readRawResourceBytesOrThrow(int id) throws IOException {
        InputStream inputStream = AppUtil.getDefContext().getResources().openRawResource(id);
        return readBytesOrThrow(0, inputStream);
    }

    public static byte[] readFileBytesOrThrow(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        return readBytesOrThrow((int)file.length(), inputStream);
    }

    protected static byte[] readBytesOrThrow(int cbSizeHint, InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(cbSizeHint);
        byte[] buffer = new byte[1000];     // size is arbitrary
        try {
            for (;;) {
                int cbRead = inputStream.read(buffer);
                if (cbRead == -1) {
                    break;  // end of stream hit
                }
                byteArrayOutputStream.write(buffer);
            }
        } finally {
            inputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }

    //----------------------------------------------------------------------------------------------

    public static void writeFile(File file, String fileContents) {
        writeFile(file.getParentFile(), file.getName(), fileContents);
    }

    public static void writeFileOrThrow(File file, String fileContents) throws IOException {
        writeFileOrThrow(file.getParentFile(), file.getName(), fileContents);
    }

    public static void writeFileOrThrow(File directory, String fileName, String fileContents) throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(directory);
        ByteBuffer byteContents = charset.encode(fileContents);
        FileOutputStream outputStream = new FileOutputStream(new File(directory, fileName));
        try {
            outputStream.write(byteContents.array(), 0, byteContents.limit());
            outputStream.flush();
        } finally {
            outputStream.getFD().sync();
            outputStream.close();
        }
    }

    public static void writeFile(File directory, String fileName, String fileContents) {
        try {
            writeFileOrThrow(directory, fileName, fileContents);
        }
        catch (IOException e) {
            RobotLog.ee(TAG, e, "error writing file: %s", (new File(directory, fileName)).getPath());
        }
    }

    //----------------------------------------------------------------------------------------------

    public static void ensureAllChangesAreCommitted(File folder) {
        try {
            Util.forEachInFolder(folder, true, new Predicate<File>() {
                @Override
                public boolean test(File file) {
                    // Because we are renaming files, some of the files passed to this method may no
                    // longer exist.
                    if (file.exists()) {
                        String filePath = file.getAbsolutePath();
                        if (filePath.endsWith(OLD_VERSION_SUFFIX)) {
                            // This file is an old version.
                            File oldFileVersion = file;
                            // Overwrite what should be a non-existant or partially written file.
                            file = new File(filePath.substring(0, filePath.length() - OLD_VERSION_SUFFIX.length()));
                            oldFileVersion.renameTo(file);
                        } else {
                            ensureChangesAreCommitted(file);
                        }
                    }
                    return true; // ignored
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException("failed to ensure all changes have been written", e);
        }
    }

    public static void ensureChangesAreCommitted(File file) {
        File oldFileVersion = new File(file.getAbsolutePath() + OLD_VERSION_SUFFIX);
        if (oldFileVersion.exists()) {
            // overwrite what should be a partially written file
            oldFileVersion.renameTo(file);
            // the old version file should also be deleted in the last step
        }
    }

    public static void updateFileRequiringCommit(File file, String newContents) {
        // make sure the files has no other changes; apply them first so we don't get catch in a strange state
        ensureChangesAreCommitted(file);

        // If the file doesn't exist yet, just write it and return.
        if (!file.exists()) {
            writeFile(file, newContents);
            return;
        }

        // Stage 1: rename the existing file to old version
        File oldVersion = new File(file.getAbsolutePath() + OLD_VERSION_SUFFIX);
        file.renameTo(oldVersion);

        // Stage 2: try writing the new contents
        try {
            writeFileOrThrow(file, newContents);
        } catch (IOException e) {
            // We failed writing the new contents. Log a message and return without deleting the
            // old version. Next time ensureChangesAreCommitted is called it will recover the old
            // version.
            RobotLog.ee(TAG, e, "error writing file: %s", file.getPath());
            return;
        }

        // State 3: delete the old version
        // Note that the operation is not complete until the old version is deleted. If we fail to
        // delete the old version, next time ensureChangesAreCommitted is called it will recover
        // the old version. That might seem wrong, but in that situation we don't know whether the
        // new file was correctly and completely written.
        oldVersion.delete();
        // Now the operation is complete.
    }
}
