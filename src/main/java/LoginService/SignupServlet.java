package LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/api/signup")
public class SignupServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set CORS headers
        setCorsHeaders(response);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        DBConnect db = new DBConnect();
        JsonObject result = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            JsonObject params = gson.fromJson(reader, JsonObject.class);
            
            if (params == null || !params.has("email") || !params.has("password") || !params.has("username")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("status", "fail");
                result.addProperty("message", "Missing required fields: email, password, or username");
                out.print(gson.toJson(result));
                return;
            }

            String email = params.get("email").getAsString().trim();
            String username = params.get("username").getAsString().trim();
            String rawPassword = params.get("password").getAsString();

            // Validate input
            if (email.isEmpty() || username.isEmpty() || rawPassword.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("status", "fail");
                result.addProperty("message", "Email, username, and password cannot be empty");
                out.print(gson.toJson(result));
                return;
            }

            // Check if email already exists
            if (db.emailExists(email)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                result.addProperty("status", "fail");
                result.addProperty("message", "Email already registered");
                out.print(gson.toJson(result));
                return;
            }

            // Check if username already exists
            if (db.usernameExists(username)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                result.addProperty("status", "fail");
                result.addProperty("message", "Username already taken");
                out.print(gson.toJson(result));
                return;
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            // Create the user
            int userId = db.createUser(username, email, hashedPassword);
            
            if (userId > 0) {
                // Generate JWT token
                String token = JwtUtil.generateToken(email, userId);
                String cookieHeader = String.format(
                    "accessToken=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Lax",
                    token, 24 * 60 * 60 // 1 day in seconds
                );
                response.addHeader("Set-Cookie", cookieHeader);
                result.addProperty("status", "success");
                result.addProperty("user_id", userId);
                result.addProperty("message", "Signup successful");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.addProperty("status", "fail");
                result.addProperty("message", "Failed to create user");
            }
            
            out.print(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Server Error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
    
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
