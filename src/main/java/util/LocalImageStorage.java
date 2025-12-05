package util;

import jakarta.servlet.http.Part;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Save uploaded images into project-root/images/hikes/<hikeId>/filename
 * Returns a public path like "/images/hikes/{hikeId}/{filename}"
 */
public class LocalImageStorage {

    /**
     * Save an uploaded image Part for the given hikeId.
     *
     * @param hikeId numeric id
     * @param originalFilename original filename from upload header
     * @param part uploaded Part (do not close outside)
     * @param projectRoot servlet context real path (can be obtained via getServletContext().getRealPath("/"))
     * @return public path (starting with "/images/hikes/...")
     */
    public static String saveHikeImage(int hikeId, String originalFilename, Part part, String projectRoot) throws IOException {
        String cleanName = sanitizeFilename(originalFilename);
        Path imagesDir = Path.of(projectRoot, "images", "hikes", String.valueOf(hikeId));

        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }

        Path target = imagesDir.resolve(cleanName);

        // Copy stream to the target file (replace if exists)
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return a web-accessible path (relative to context root)
        String publicPath = "/images/hikes/" + hikeId + "/" + cleanName;
        return publicPath;
    }

    private static String sanitizeFilename(String name) {
        if (name == null) {
            long ts = System.currentTimeMillis();
            return "upload_" + ts + ".jpg";
        }
        // keep only safe chars
        String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        // avoid path traversal
        if (cleaned.contains("..")) cleaned = cleaned.replace("..", "_");
        return cleaned;
    }
}
