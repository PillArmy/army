package io.army.example.stock.utils;

import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;

public abstract class FileUtils {

    private FileUtils() {
    }


    /// @see Base64#getEncoder()
    public static String fileSHA256Base64(Path filePath) {

        try (InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ)) {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] buffer = new byte[2048];
            for (int length; (length = in.read(buffer)) > 0; ) {
                digest.update(buffer, 0, length);
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (Exception e) {
            String m = String.format("%s error", filePath);
            throw new RuntimeException(m, e);
        }
    }


    public static void deleteIfExists(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            String m = String.format("%s error", filePath);
            throw new RuntimeException(m, e);
        }
    }

    @Nullable
    public static String fileTypeName(Path filePath) {
        final String fileName;
        fileName = filePath.getFileName().toString();
        final int periodIndex;
        periodIndex = fileName.lastIndexOf('.');
        if (periodIndex > 1) {
            return fileName.substring(periodIndex + 1);
        }
        return null;
    }

}
