package LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/send-code")
public class CodeSendingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long EXPIRATION_MINUTES = 15;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Add CORS headers
        setCorsHeaders(response);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject result = new JsonObject();
        DBConnect db = new DBConnect();

        try {
            BufferedReader reader = request.getReader();
            JsonObject params = gson.fromJson(reader, JsonObject.class);
            
            if (params == null || !params.has("email")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("status", "fail");
                result.addProperty("message", "Email is required");
                out.print(gson.toJson(result));
                return;
            }

            String email = params.get("email").getAsString();

            if (!db.emailExists(email)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.addProperty("status", "fail");
                result.addProperty("message", "Email not registered");
                out.print(gson.toJson(result));
                return;
            }

            SecureRandom random = new SecureRandom();
            int num = random.nextInt(1000000);
            String code = String.format("%06d", num);
            long expiryTime = System.currentTimeMillis() + (EXPIRATION_MINUTES * 60 * 1000);
            boolean stored = db.storeSecurityCode(email, code, expiryTime);

            if (stored) {
                try {
                    PassResetEmail.sendSecurityCode(email, code);
                    
                    result.addProperty("status", "success");
                    result.addProperty("message", "Verification code sent to email");
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    result.addProperty("status", "error");
                    result.addProperty("message", "Failed to send email");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.addProperty("status", "error");
                result.addProperty("message", "Database error storing code");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Server Error");
            out.print(gson.toJson(err));
        }
        
        out.print(gson.toJson(result));
    }
    
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}