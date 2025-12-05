package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Utility for simple image validation, saving, and thumbnail generation.
 * Uses only standard Java APIs (no external libs).
 */
public class ImageUtil {

    // Allowed extensions (lower-case)
    private static final String[] ALLOWED_EXT = new String[] {"jpg", "jpeg", "png"};

    /**
     * Basic check for allowed content type and/or filename extension.
     * We accept if either contentType looks like image/* or filename has allowed extension.
     */
    public static boolean isAllowedImage(String contentType, String filename) {
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            // We'll still check extension for safety
            return hasAllowedExtension(filename);
        } else {
            return hasAllowedExtension(filename);
        }
    }

    private static boolean hasAllowedExtension(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        for (String ext : ALLOWED_EXT) {
            if (lower.endsWith("." + ext)) return true;
        }
        return false;
    }

    /**
     * Saves an uploaded image InputStream to the given directory (fullDir) under a unique filename.
     * Returns the server-relative path (starting with "/images/full/..." or "/images/thumb/...").
     *
     * The method will create directories if they do not exist.
     */
    public static String saveImageToDisk(InputStream in, Path fullDir, String originalFilename, String publicPrefix) throws IOException {
        if (!Files.exists(fullDir)) {
            Files.createDirectories(fullDir);
        }

        String ext = extractExtension(originalFilename);
        if (ext == null) {
            ext = "jpg";
        }

        String filename = generateUniqueName(ext);
        Path out = fullDir.resolve(filename);

        // Copy stream to file
        try (OutputStream outStream = Files.newOutputStream(out)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }
        }

        // Return a path prefixed by publicPrefix (e.g., "/images/full/filename.jpg")
        return publicPrefix + "/" + filename;
    }

    /**
     * Create a thumbnail (width = targetWidth, scaled proportionally) from the source image file.
     * Writes thumbnail to thumbDir with similar unique naming. Returns public path.
     *
     * This method uses ImageIO + BufferedImage + Graphics2D (pure Java).
     */
    public static String createThumbnail(Path sourceImagePath, Path thumbDir, int targetWidth, String publicThumbPrefix) throws IOException {
        if (!Files.exists(thumbDir)) {
            Files.createDirectories(thumbDir);
        }

        BufferedImage src = ImageIO.read(sourceImagePath.toFile());
        if (src == null) {
            throw new IOException("Unable to read source image for thumbnail creation: " + sourceImagePath);
        }

        int srcW = src.getWidth();
        int srcH = src.getHeight();
        if (srcW <= targetWidth) {
            // image is smaller or equal than target; copy it to thumb folder
            String ext = extractExtension(sourceImagePath.getFileName().toString());
            String thumbName = generateUniqueName(ext);
            Path dest = thumbDir.resolve(thumbName);
            Files.copy(sourceImagePath, dest, StandardCopyOption.REPLACE_EXISTING);
            return publicThumbPrefix + "/" + thumbName;
        }

        double scale = (double) targetWidth / (double) srcW;
        int targetHeight = (int) Math.max(1, Math.round(srcH * scale));

        Image scaled = src.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

        // Create buffered image and draw scaled image
        BufferedImage thumb = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumb.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(scaled, 0, 0, null);
        } finally {
            g2d.dispose();
        }

        // Save thumbnail
        String ext = extractExtension(sourceImagePath.getFileName().toString());
        if (ext == null) ext = "jpg";
        String thumbName = generateUniqueName(ext);
        Path thumbPath = thumbDir.resolve(thumbName);

        // Prefer "jpg" or "png" writers based on extension
        String writeFormat = ext.equalsIgnoreCase("png") ? "png" : "jpg";
        try (OutputStream os = Files.newOutputStream(thumbPath)) {
            ImageIO.write(thumb, writeFormat, os);
        }

        return publicThumbPrefix + "/" + thumbName;
    }

    private static String generateUniqueName(String ext) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        long ts = System.currentTimeMillis();
        return ts + "_" + uuid + "." + ext;
    }

    private static String extractExtension(String filename) {
        if (filename == null) return null;
        int idx = filename.lastIndexOf('.');
        if (idx == -1) return null;
        String ext = filename.substring(idx + 1).toLowerCase();
        // normalize jpeg -> jpg
        if (ext.equals("jpeg")) ext = "jpg";
        return ext;
    }
}
