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
import jakarta.servlet.http.Cookie;
import LoginService.JwtUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json; charset=UTF-8");

        // Authentication - check request attribute first (set by filter), then check cookie directly
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Fallback: if filter didn't set userId, try to get it from cookie directly
        if (userId == null) {
            userId = getUserIdFromCookie(req);
            if (userId != null) {
                req.setAttribute("userId", userId);
            }
        }
        
        if (userId == null) {
            System.out.println("AddHikeServlet: No userId found - not authenticated");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(error("Not authenticated")));
            return;
        }
        
        System.out.println("AddHikeServlet: User authenticated, userId=" + userId);

        // Parse fields (multipart-safe)
        String name = safeGetFormField(req, "name");
        String location = safeGetFormField(req, "location");
        String description = safeGetFormField(req, "description");
        Double difficulty = parseDoubleOrNull(safeGetFormField(req, "difficulty"));
        Double distance = parseDoubleOrNull(safeGetFormField(req, "distance"));
        Integer elevation = parseIntOrNull(safeGetFormField(req, "elevation"));
        Double latitude = parseDoubleOrNull(safeGetFormField(req, "latitude"));
        Double longitude = parseDoubleOrNull(safeGetFormField(req, "longitude"));

        Double initialRating = parseDoubleOrNull(safeGetFormField(req, "initialRating"));
        String initialComment = safeGetFormField(req, "initialComment");

        // Debug logging
        System.out.println("AddHikeServlet: Received fields - name: " + name + ", location: " + location + 
                          ", difficulty: " + difficulty + ", distance: " + distance);

        // Validate required
        if (name == null || name.isBlank()) {
            System.out.println("AddHikeServlet: Validation failed - name is null or blank");
            // Log all available parameters for debugging
            System.out.println("AddHikeServlet: Available parameters:");
            req.getParameterMap().forEach((key, values) -> {
                System.out.println("  " + key + " = " + java.util.Arrays.toString(values));
            });
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(error("Missing required field: name")));
            return;
        }
        
        // Location is optional but let's log if it's missing
        if (location == null || location.isBlank()) {
            System.out.println("AddHikeServlet: Warning - location is null or blank (optional field)");
        }
        
        // Validate distance
        if (distance == null || distance <= 0) {
            System.out.println("AddHikeServlet: Validation failed - distance is null or invalid: " + distance);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(error("Invalid or missing distance field")));
            return;
        }
        
        // Validate difficulty
        if (difficulty == null) {
            System.out.println("AddHikeServlet: Validation failed - difficulty is null");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(error("Missing required field: difficulty")));
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
        String projectRootTemp = req.getServletContext().getRealPath("/");
        if (projectRootTemp == null) {
            // fallback to working dir
            projectRootTemp = System.getProperty("user.dir");
        }
        final String projectRoot = projectRootTemp; // Make final for lambda access

        // main flow within DB transaction using the shared connection
        Connection conn = null;
        try {
            conn = database.DBConnector.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Create hike
            final int hikeId = hikeDao.createHike( // Make final for lambda access
                name,
                location,
                difficulty,
                distance,
                elevation,
                userId,
                latitude,
                longitude,
                description
            );

            // Process images (parts named "images" or single part "image")
            // MULTITHREADING: Process multiple image uploads in parallel using ExecutorService
            List<String> imageUrls = new ArrayList<>();
            Collection<Part> parts;
            try {
                parts = req.getParts();
            } catch (Exception e) {
                System.out.println("AddHikeServlet: Error getting parts: " + e.getMessage());
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(error("Error processing multipart request: " + e.getMessage())));
                return;
            }
            System.out.println("AddHikeServlet: Processing parts, total parts: " + (parts != null ? parts.size() : 0));
            
            if (parts != null) {
                // Helper class to hold image data for thread-safe parallel processing
                class ImageData {
                    final String filename;
                    final byte[] data;
                    ImageData(String filename, byte[] data) {
                        this.filename = filename;
                        this.data = data;
                    }
                }
                
                // Collect valid image parts and read their data into memory (thread-safe)
                List<ImageData> imageDataList = new ArrayList<>();
                for (Part part : parts) {
                    String partName = part.getName();
                    System.out.println("AddHikeServlet: Found part - name: " + partName + ", size: " + part.getSize());
                    
                    if (!"images".equals(partName) && !"image".equals(partName)) {
                        System.out.println("AddHikeServlet: Skipping part (not an image): " + partName);
                        continue;
                    }
                    if (part.getSize() <= 0) {
                        System.out.println("AddHikeServlet: Skipping part (size <= 0): " + partName);
                        continue;
                    }

                    String submittedFileName = ImageUtil.getFileName(part);
                    System.out.println("AddHikeServlet: Processing image file: " + submittedFileName);
                    
                    if (!ImageUtil.isValidImageType(submittedFileName)) {
                        System.out.println("AddHikeServlet: Invalid image type: " + submittedFileName);
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(gson.toJson(error("Unsupported image type. Allowed: .jpg, .jpeg, .png, .webp")));
                        return;
                    }
                    
                    // Read Part data into memory for thread-safe parallel processing
                    try (java.io.InputStream in = part.getInputStream()) {
                        byte[] imageData = in.readAllBytes();
                        imageDataList.add(new ImageData(submittedFileName, imageData));
                    } catch (IOException e) {
                        System.out.println("AddHikeServlet: Error reading image data: " + e.getMessage());
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(gson.toJson(error("Error reading image data: " + e.getMessage())));
                        return;
                    }
                }
                
                // MULTITHREADING: Process image saving in parallel using thread pool
                int threadPoolSize = Math.min(imageDataList.size(), 5);
                System.out.println("AddHikeServlet: Starting multithreaded image processing with " + threadPoolSize + " threads for " + imageDataList.size() + " images");
                ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
                List<Future<String>> futures = new ArrayList<>();
                
                for (int i = 0; i < imageDataList.size(); i++) {
                    final ImageData imageData = imageDataList.get(i);
                    final int imageIndex = i + 1;
                    Future<String> future = executorService.submit(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            long startTime = System.currentTimeMillis();
                            String threadName = Thread.currentThread().getName();
                            System.out.println("AddHikeServlet: [Thread " + threadName + "] Starting to process image " + imageIndex + ": " + imageData.filename);
                            
                            // Save file to disk: <projectRoot>/images/hikes/<hikeId>/
                            // This I/O operation is done in parallel across multiple threads
                            String cleanName = imageData.filename.replaceAll("[^a-zA-Z0-9._-]", "_");
                            if (cleanName.contains("..")) cleanName = cleanName.replace("..", "_");
                            
                            java.nio.file.Path imagesDir = java.nio.file.Path.of(projectRoot, "images", "hikes", String.valueOf(hikeId));
                            if (!java.nio.file.Files.exists(imagesDir)) {
                                java.nio.file.Files.createDirectories(imagesDir);
                            }
                            
                            java.nio.file.Path target = imagesDir.resolve(cleanName);
                            java.nio.file.Files.write(target, imageData.data);
                            
                            long endTime = System.currentTimeMillis();
                            String savedPublicPath = "/images/hikes/" + hikeId + "/" + cleanName;
                            System.out.println("AddHikeServlet: [Thread " + threadName + "] Completed image " + imageIndex + " in " + (endTime - startTime) + "ms: " + savedPublicPath);
                            return savedPublicPath;
                        }
                    });
                    futures.add(future);
                }
                
                // Collect all results from parallel processing
                for (Future<String> future : futures) {
                    try {
                        String savedPublicPath = future.get(); // Wait for completion
                        imageUrls.add(savedPublicPath);
                        
                        // Persist into photos table (sequential for transaction safety)
                        photoDao.insertPhoto(hikeId, userId, savedPublicPath, null);
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("AddHikeServlet: Error processing image: " + e.getMessage());
                        e.printStackTrace();
                        executorService.shutdownNow();
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(gson.toJson(error("Error processing images: " + e.getMessage())));
                        return;
                    }
                }
                
                executorService.shutdown();
                System.out.println("AddHikeServlet: All " + imageUrls.size() + " images processed in parallel using multithreading");
            }
            
            System.out.println("AddHikeServlet: Total images processed: " + imageUrls.size());
            
            // Validate that at least one image was provided
            if (imageUrls.isEmpty()) {
                System.out.println("AddHikeServlet: No images provided - rolling back");
                conn.rollback();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(error("At least one image is required")));
                return;
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

    private Integer getUserIdFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && JwtUtil.validateToken(token)) {
                        return JwtUtil.getUserIdFromToken(token);
                    }
                }
            }
        }
        return null;
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}
