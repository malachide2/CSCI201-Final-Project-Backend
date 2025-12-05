package servlets;

import com.google.gson.Gson;
import database.HikeDao;
import database.PhotoDao;
import database.ReviewDao;

import util.ImageUtil;
import util.LocalImageStorage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Servlet to create a Hike (no thumbnails).
 * URL: /api/hikes/add
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,        // 1 MB
    maxFileSize = 5L * 1024L * 1024L,      // 5 MB per file
    maxRequestSize = 30L * 1024L * 1024L   // 30 MB total
)
@WebServlet("/api/hikes/add")
public class AddHikeServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final HikeDao hikeDao = new HikeDao();
    private final PhotoDao photoDao = new PhotoDao();
    private final ReviewDao reviewDao = new ReviewDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json; charset=UTF-8");

        // Authentication - keep same style as your project
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(error("Not authenticated")));
            return;
        }

        // Parse fields (multipart-safe)
        String name = safeGetFormField(req, "name");
        String location = safeGetFormField(req, "location");
        Double difficulty = parseDoubleOrNull(safeGetFormField(req, "difficulty"));
        Double distance = parseDoubleOrNull(safeGetFormField(req, "distance"));
        Integer elevation = parseIntOrNull(safeGetFormField(req, "elevation"));
        Double latitude = parseDoubleOrNull(safeGetFormField(req, "latitude"));
        Double longitude = parseDoubleOrNull(safeGetFormField(req, "longitude"));

        Double initialRating = parseDoubleOrNull(safeGetFormField(req, "initialRating"));
        String initialComment = safeGetFormField(req, "initialComment");

        // Validate required
        if (name == null || name.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(error("Missing required field: name")));
            return;
        }

        // Validate rating if provided
        if (initialRating != null) {
            if (initialRating < 1.0 || initialRating > 5.0 || ((initialRating * 2) % 1) != 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(error("initialRating must be between 1.0 and 5.0 in 0.5 increments")));
                return;
            }
        }

        // Duplicate name check (case-insensitive)
        try {
            if (hikeDao.existsByNameIgnoreCase(name)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(gson.toJson(error("A hike with that name already exists")));
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(error("Database error during duplicate check: " + e.getMessage())));
            return;
        }

        // Determine project root and image directories
        String projectRoot = req.getServletContext().getRealPath("/");
        if (projectRoot == null) {
            // fallback to working dir
            projectRoot = System.getProperty("user.dir");
        }

        // main flow within DB transaction using the shared connection
        Connection conn = null;
        try {
            conn = database.DBConnector.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Create hike
            int hikeId = hikeDao.createHike(
                name,
                location,
                difficulty,
                distance,
                elevation,
                userId,
                latitude,
                longitude
            );

            // Process images (parts named "images" or single part "image")
            List<String> imageUrls = new ArrayList<>();
            Collection<Part> parts = req.getParts();
            for (Part part : parts) {
                String partName = part.getName();
                if (!"images".equals(partName) && !"image".equals(partName)) continue;
                if (part.getSize() <= 0) continue;

                String submittedFileName = ImageUtil.getFileName(part);
                if (!ImageUtil.isValidImageType(submittedFileName)) {
                    conn.rollback();
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(gson.toJson(error("Unsupported image type. Allowed: .jpg, .jpeg, .png")));
                    return;
                }

                // Save file to disk: <projectRoot>/images/hikes/<hikeId>/
                String savedPublicPath = LocalImageStorage.saveHikeImage(hikeId, submittedFileName, part, projectRoot);

                // Persist into photos table
                photoDao.insertPhoto(hikeId, userId, savedPublicPath, null);

                imageUrls.add(savedPublicPath);
            }

            // Optional initial review
            Integer createdReviewId = null;
            if (initialRating != null) {
                createdReviewId = reviewDao.createReview(hikeId, userId, initialRating, initialComment == null ? "" : initialComment);
            }

            conn.commit();

            // Build success response
            var success = new java.util.HashMap<String, Object>();
            success.put("success", true);
            success.put("hikeId", hikeId);
            success.put("imageUrls", imageUrls);
            if (createdReviewId != null) success.put("initialReviewId", createdReviewId);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(success));
            return;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(error("Internal server error: " + e.getMessage())));
            return;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    // helpers
    private java.util.Map<String,Object> error(String msg) {
        var m = new java.util.HashMap<String,Object>();
        m.put("success", false);
        m.put("error", msg);
        return m;
    }

    private String safeGetFormField(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v != null) return v;
        try {
            Part p = req.getPart(name);
            if (p != null) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {}
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

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}
