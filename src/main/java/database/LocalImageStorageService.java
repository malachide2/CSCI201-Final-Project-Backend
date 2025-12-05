package database;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Simple local filesystem storage implementation.
 * Stores files under a configured base directory and returns a path
 * relative to server's static file serving root (e.g. "/uploads/...").
 *
 * Make sure your servlet container is configured to serve the upload dir,
 * or copy files to a web-accessible location. For development this works
 * out of the box if your server serves the project's resources directory.
 */
public class LocalImageStorageService implements ImageStorageService {

    // configure these paths as needed
    private final Path baseDir;        // e.g. /var/www/myapp/uploads
    private final String publicBase;   // e.g. "/uploads" — URL path prefix

    public LocalImageStorageService(Path baseDir, String publicBase) {
        this.baseDir = baseDir;
        this.publicBase = publicBase;
    }

    public LocalImageStorageService() {
        // Defaults: ./uploads directory in project root, served at /uploads
        this.baseDir = Paths.get(System.getProperty("user.dir"), "uploads");
        this.publicBase = "/uploads";
    }

    private void ensureDir() throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
    }

    @Override
    public String storeImage(InputStream in, String filename) throws Exception {
        ensureDir();
        String finalName = makeUniqueFilename(filename);
        Path dest = baseDir.resolve(finalName);

        try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW)) {
            copyStream(in, out);
        }

        return publicBase + "/" + finalName;
    }

    @Override
    public String storeThumbnail(InputStream in, String filename) throws Exception {
        ensureDir();
        String thumbName = "thumb_" + makeUniqueFilename(filename);
        Path dest = baseDir.resolve(thumbName);

        try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW)) {
            copyStream(in, out);
        }

        return publicBase + "/" + thumbName;
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
    }

    private String makeUniqueFilename(String original) {
        String clean = original.replaceAll("[^a-zA-Z0-9\\.\\-\\_]", "_");
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        long ts = Instant.now().toEpochMilli();
        return ts + "_" + uuid + "_" + clean;
    }
}
