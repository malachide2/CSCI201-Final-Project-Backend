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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        DBConnect db = new DBConnect();
        JsonObject result = new JsonObject();

        try {
            BufferedReader reader = request.getReader();
            JsonObject params = gson.fromJson(reader, JsonObject.class);
            
            if (params == null || !params.has("email") || !params.has("password")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("status", "fail");
                result.addProperty("message", "Missing email or password");
                out.print(gson.toJson(result));
                return;
            }

            String email = params.get("email").getAsString();
            String rawPassword = params.get("password").getAsString();

            
            
            String storedHash = db.getPasswordHash(email); 
            int userId = db.getUserId(email);           

           
            if (storedHash != null && BCrypt.checkpw(rawPassword, storedHash)) {
                String token = JwtUtil.generateToken(email, userId);
                String cookieHeader = String.format(
                    "accessToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Strict",
                    token, 24 * 60 * 60 // 1 day in seconds
                );
                response.addHeader("Set-Cookie", cookieHeader);
                result.addProperty("status", "success");
                result.addProperty("user_id", userId);
                result.addProperty("message", "Login successful");
                
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.addProperty("status", "fail");
                result.addProperty("message", "Invalid email or password");
            }
            
            out.print(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Server Error");
            out.print(gson.toJson(err));
        }
    }
}