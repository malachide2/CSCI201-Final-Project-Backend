package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.HikeDao;
import database.PhotoDao;
import database.ReviewDao;
import util.ImageUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Servlet to create hikes with optional initial review and image upload.
 * - URL: /api/hikes
 * - Expects multipart/form-data with fields:
 *   name (required), location, difficulty, distance, elevation, latitude, longitude,
 *   initialRating, initialComment, and one or more file parts named "images".
 *
 * Stores images to <project-root>/images/full and thumbnails to <project-root>/images/thumb
 */
@WebServlet("/api/hikes")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1MB
        maxFileSize = 5L * 1024L * 1024L, // 5MB per file
        maxRequestSize = 30L * 1024L * 1024L // 30MB total
)
public class HikeSubmissionServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final HikeDao hikeDao = new HikeDao();
    private final PhotoDao photoDao = new PhotoDao();
    private final ReviewDao reviewDao = new ReviewDao();

    // Thumbnail width in pixels
    private static final int THUMB_WIDTH = 300;
    // Max allowed image size (repeat of @MultipartConfig but good to check)
    private static final long MAX_FILE_BYTES = 5L * 1024L * 1024L;

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json; charset=UTF-8");

        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject err = new JsonObject();
            err.addProperty("error", "Not authenticated");
            resp.getWriter().write(gson.toJson(err));
            return;
        }

        // Ensure multipart
        if (!isMultipart(req)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(resp, "Content must be multipart/form-data");
            return;
        }

        // Get project root to store images
        String projectRoot = getServletContext().getRealPath("/");
        if (projectRoot == null) {
            // Fallback to working directory
            projectRoot = System.getProperty("user.dir");
        }

        Path fullDir = Paths.get(projectRoot, "images", "full");
        Path thumbDir = Paths.get(projectRoot, "images", "thumb");
        String publicFullPrefix = "/images/full";
        String publicThumbPrefix = "/images/thumb";

        try {
            // Read form fields (multipart)
            String name = getFormField(req, "name");
            if (name == null || name.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(resp, "Missing required field: name");
                return;
            }
            String location = getFormField(req, "location");
            Double difficulty = parseDoubleOrNull(getFormField(req, "difficulty"));
            Double distance = parseDoubleOrNull(getFormField(req, "distance"));
            Integer elevation = parseIntOrNull(getFormField(req, "elevation"));
            Double latitude = parseDoubleOrNull(getFormField(req, "latitude"));
            Double longitude = parseDoubleOrNull(getFormField(req, "longitude"));

            Double initialRating = parseDoubleOrNull(getFormField(req, "initialRating"));
            String initialComment = getFormField(req, "initialComment");

            // Validate initial rating if provided
            if (initialRating != null) {
                if (initialRating < 1.0 || initialRating > 5.0 || ((initialRating * 2) % 1) != 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeError(resp, "initialRating must be between 1 and 5 in 0.5 increments");
                    return;
                }
            }

            // Duplicate check
            try {
                if (hikeDao.existsByNameIgnoreCase(name)) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    writeError(resp, "A hike with that name already exists");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(resp, "Database error during duplicate check: " + e.getMessage());
                return;
            }

            // Process submission inside DB transaction
            Connection conn = database.DBConnector.getInstance().getConnection();
            try {
                conn.setAutoCommit(false);

                int hikeId = hikeDao.createHike(
                        name, location, difficulty, distance, elevation, userId, latitude, longitude
                );

                List<String> savedImageUrls = new ArrayList<>();
                List<String> savedThumbUrls = new ArrayList<>();

                // Process image parts named "images"
                Collection<Part> parts = req.getParts();
                for (Part part : parts) {
                    if (!"images".equals(part.getName())) continue;
                    if (part.getSize() <= 0) continue;

                    String submittedFilename = getSubmittedFileName(part);
                    String contentType = part.getContentType();

                    if (part.getSize() > MAX_FILE_BYTES) {
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writeError(resp, "Image too large. Max is " + (MAX_FILE_BYTES / (1024*1024)) + " MB");
                        return;
                    }

                    if (!ImageUtil.isAllowedImage(contentType, submittedFilename)) {
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writeError(resp, "Unsupported image type. Allowed: .jpg, .jpeg, .png");
                        return;
                    }

                    // Save original to disk
                    // We will write the uploaded stream to a temp file first then move/create thumbnail from that path.
                    Path tempFile = null;
                    try (InputStream in = part.getInputStream()) {
                        // Save to temp file in fullDir
                        if (!fullDir.toFile().exists()) fullDir.toFile().mkdirs();
                        String ext = (submittedFilename != null) ? submittedFilename : "jpg";
                        String tmpName = "tmp_" + System.currentTimeMillis() + "_" + Math.abs(submittedFilename == null ? 0 : submittedFilename.hashCode()) + "." + (ext.contains(".") ? ext.substring(ext.lastIndexOf('.')+1) : "jpg");
                        tempFile = fullDir.resolve(tmpName);
                        try (OutputStream out = java.nio.file.Files.newOutputStream(tempFile)) {
                            byte[] buf = new byte[8192];
                            int r;
                            while ((r = in.read(buf)) != -1) {
                                out.write(buf, 0, r);
                            }
                        }

                        // Move/rename temp file to unique final name via ImageUtil.saveImageToDisk-like behavior:
                        String publicFullPath = ImageUtil.saveImageToDisk(java.nio.file.Files.newInputStream(tempFile), fullDir, submittedFilename, publicFullPrefix);
                        // Note: saveImageToDisk will create another file; we can delete the tempFile afterwards
                        // Insert photo record
                        photoDao.insertPhoto(hikeId, userId, publicFullPath, null);
                        savedImageUrls.add(publicFullPath);

                        // Create thumbnail from the saved full image file (the saveImageToDisk call created a final file)
                        // We need to locate that final file path on disk:
                        // The saved file path is publicFullPrefix + "/" + filename -> get filename
                        String savedFilename = publicFullPath.substring(publicFullPrefix.length() + 1);
                        Path savedFilePath = fullDir.resolve(savedFilename);

                        String thumbPublic = ImageUtil.createThumbnail(savedFilePath, thumbDir, THUMB_WIDTH, publicThumbPrefix);
                        savedThumbUrls.add(thumbPublic);

                        // Delete temp file if exists
                        try { java.nio.file.Files.deleteIfExists(tempFile); } catch (Exception ignored) {}

                    } catch (Exception ex) {
                        // Cleanup and rollback
                        if (tempFile != null) try { java.nio.file.Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
                        conn.rollback();
                        ex.printStackTrace();
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        writeError(resp, "Failed to process image: " + ex.getMessage());
                        return;
                    }
                }

                // Optional initial review
                Integer createdReviewId = null;
                if (initialRating != null) {
                    createdReviewId = reviewDao.createReview(hikeId, userId, initialRating, initialComment == null ? "" : initialComment);
                }

                conn.commit();

                JsonObject success = new JsonObject();
                success.addProperty("status", "ok");
                success.addProperty("hikeId", hikeId);
                success.add("imageUrls", gson.toJsonTree(savedImageUrls));
                success.add("thumbnailUrls", gson.toJsonTree(savedThumbUrls));
                if (createdReviewId != null) success.addProperty("initialReviewId", createdReviewId);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(gson.toJson(success));
                return;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeError(resp, "Database error: " + e.getMessage());
                return;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(resp, "Internal server error: " + e.getMessage());
        }
    }

    private boolean isMultipart(HttpServletRequest req) {
        String ct = req.getContentType();
        return ct != null && ct.toLowerCase().startsWith("multipart/");
    }

    private String getFormField(HttpServletRequest req, String name) throws IOException, ServletException {
        // first try normal parameter
        String val = req.getParameter(name);
        if (val != null) return val;
        // try multipart part
        try {
            Part p = req.getPart(name);
            if (p != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    return sb.toString();
                }
            }
        } catch (IllegalStateException ise) {
            // large request -> handled by container; propagate as null
        }
        return null;
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    /**
     * Extracts the filename from a Part's content-disposition header.
     */
    private String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String[] kv = token.split("=", 2);
                if (kv.length == 2) {
                    String fn = kv[1].trim();
                    if (fn.startsWith("\"") && fn.endsWith("\"")) {
                        fn = fn.substring(1, fn.length() - 1);
                    }
                    return fn;
                }
            }
        }
        return null;
    }

    private void writeError(HttpServletResponse resp, String msg) throws IOException {
        JsonObject err = new JsonObject();
        err.addProperty("error", msg);
        resp.getWriter().write(gson.toJson(err));
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}
