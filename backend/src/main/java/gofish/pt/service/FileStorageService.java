package gofish.pt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Process a list of photo URLs/base64 strings.
     * If a string is base64 encoded, save it to disk and return the URL.
     * If it's already a URL, return it as-is.
     */
    public List<String> processPhotoUrls(List<String> photoUrls) {
        if (photoUrls == null || photoUrls.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> processedUrls = new ArrayList<>();
        for (String photoUrl : photoUrls) {
            if (isBase64Image(photoUrl)) {
                String savedUrl = saveBase64Image(photoUrl);
                processedUrls.add(savedUrl);
            } else {
                // Already a URL, keep it as-is
                processedUrls.add(photoUrl);
            }
        }
        return processedUrls;
    }

    /**
     * Check if the string is a base64 encoded image
     */
    private boolean isBase64Image(String data) {
        return data != null && data.startsWith("data:image/");
    }

    /**
     * Save a base64 encoded image to disk and return the URL
     */
    private String saveBase64Image(String base64Data) {
        try {
            // Parse the base64 data URI
            // Format: data:image/jpeg;base64,/9j/4AAQ...
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid base64 image format");
            }

            // Extract the mime type to determine file extension
            String header = parts[0]; // e.g., "data:image/jpeg;base64"
            String base64Content = parts[1];

            String extension = extractExtension(header);
            byte[] imageBytes = Base64.getDecoder().decode(base64Content);

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "." + extension;

            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, imageBytes);

            // Return the URL to access the file
            return baseUrl + "/uploads/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    /**
     * Extract file extension from the data URI header
     */
    private String extractExtension(String header) {
        // header format: "data:image/jpeg;base64" or "data:image/png;base64"
        if (header.contains("jpeg") || header.contains("jpg")) {
            return "jpg";
        } else if (header.contains("png")) {
            return "png";
        } else if (header.contains("gif")) {
            return "gif";
        } else if (header.contains("webp")) {
            return "webp";
        }
        return "jpg"; // default
    }
}
