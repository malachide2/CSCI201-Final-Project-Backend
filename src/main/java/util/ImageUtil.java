package util;

import jakarta.servlet.http.Part;

/**
 * Small helpers to parse uploaded Part headers & validate extension.
 */
public class ImageUtil {

    public static String getFileName(Part part) {
        if (part == null) return null;
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String[] kv = token.split("=", 2);
                if (kv.length == 2) {
                    String fn = kv[1].trim();
                    if (fn.startsWith("\"") && fn.endsWith("\"")) {
                        fn = fn.substring(1, fn.length()-1);
                    }
                    // Some browsers include full path, keep only filename
                    int lastSep = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
                    if (lastSep >= 0) fn = fn.substring(lastSep + 1);
                    return fn;
                }
            }
        }
        return null;
    }

    public static boolean isValidImageType(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
    }
}
