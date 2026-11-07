/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package uap.asia.model;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 *
 * @author Group 2
 */
// Lists the suppliers from the database. URL: /do.suppliers
@WebServlet(name = "viewsuppliers", urlPatterns = {"/do.suppliers"})
public class viewsuppliers extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Suppliers</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
 
            out.println("<h1>Suppliers</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>Name</th><th>Contact</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT name, contact FROM suppliers ORDER BY name");
                     ResultSet rs = ps.executeQuery()) {
                    // Print one table row per supplier found in the database
                    while (rs.next()) {
                        out.println("<tr><td>" + iosqlclass.esc(rs.getString("name"))
                                + "</td><td>" + iosqlclass.esc(rs.getString("contact"))
                                + "</td></tr>");
                        count++;
                    }
                }
                out.println("</table>");
                out.println("<p>" + count + " supplier(s) found.</p>");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load suppliers from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='suppliers.jsp'>Back to Suppliers</a> | <a href='inventory.jsp'>Inventory</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    // GET and POST both run processRequest()
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
 
}
