package LoginService;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {
    "/api/hikes/add",
    "/api/reviews",
    "/api/reviews/upvote",
    "/api/friends",
    "/api/friends/*"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        System.out.println("AuthFilter: Filter invoked for: " + ((HttpServletRequest) request).getRequestURI());
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Handle CORS preflight
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            setCorsHeaders(httpResponse);
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Get JWT token from cookie
        String token = getTokenFromCookie(httpRequest);

        if (token != null) {
            if (JwtUtil.validateToken(token)) {
                // Extract userId from token
                Integer userId = JwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    // Set userId as request attribute for servlets to use
                    httpRequest.setAttribute("userId", userId);
                    System.out.println("AuthFilter: User authenticated, userId=" + userId);
                } else {
                    System.out.println("AuthFilter: Token valid but userId is null");
                }
            } else {
                System.out.println("AuthFilter: Token validation failed");
            }
        } else {
            System.out.println("AuthFilter: No token found in cookies. Request path: " + httpRequest.getRequestURI());
            if (httpRequest.getCookies() != null) {
                System.out.println("AuthFilter: Cookies present: " + httpRequest.getCookies().length);
                for (Cookie c : httpRequest.getCookies()) {
                    System.out.println("AuthFilter: Cookie name: " + c.getName());
                }
            } else {
                System.out.println("AuthFilter: No cookies in request");
            }
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthFilter: Filter initialized");
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}

