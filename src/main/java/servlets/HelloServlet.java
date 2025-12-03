package servlets;
import database.DBConnector;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
  private final DBConnector connector = DBConnector.getInstance();
  private final Gson gson = new Gson();
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello from HelloServlet!");
  }
}