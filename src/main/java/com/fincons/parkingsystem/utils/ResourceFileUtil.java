package com.fincons.parkingsystem.utils;


import java.io.*;
import java.nio.file.Files;
import org.springframework.core.io.ClassPathResource;

public class ResourceFileUtil {

    public static String copyToTempFile(String resourcePath, String suffix) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            File tempFile = Files.createTempFile("kafka-", suffix).toFile();
            tempFile.deleteOnExit();

            try (InputStream in = resource.getInputStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }
}
