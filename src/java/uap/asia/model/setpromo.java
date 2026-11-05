/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package uap.asia.model;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.*;

/**
 *
 * @author Group 2
 */
// Admin creates a promo/discount (form on admin.jsp). URL: /do.setpromo
@WebServlet(name = "setpromo", urlPatterns = {"/do.setpromo"})
public class setpromo extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Set Promo Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String promoName = request.getParameter("promoname");
            String percentText = request.getParameter("percent");
            String id = request.getParameter("target");

            // Stop if the page was opened directly, without the form data
            if (promoName == null || percentText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='admin.jsp'>Go to Admin</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            if (id == null) {
                id = "";
            }
            promoName = promoName.trim();
            id = id.trim();

            // The database keeps which employee created the promo, so the user must be logged in
            HttpSession session = request.getSession(false);
            Integer employeeId = null;
            if (session != null) {
                employeeId = (Integer) session.getAttribute("e_id");
            }
            if (employeeId == null) {
                out.println("<h1>Promo not created</h1>");
                out.println("<p>Please login as an employee first.</p>");
                out.println("<a href='login.jsp'>Go to Login</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Convert the percent to a number (0 if invalid)
            double percent = 0;
            try {
                percent = Double.parseDouble(percentText.trim());
            } catch (NumberFormatException e) {
                percent = 0;
            }

            // product = { name, price } of the promo product ({ "", "" } means all products),
            // or null when the promo was not saved
            String[] product = null;
            String problem = "";

            // Validate, then save the promo
            if (promoName.isEmpty() || percent <= 0 || percent > 100) {
                problem = "Give the promo a name and a percent from 1 to 100.";
            } else if (promoName.length() > 100) {
                problem = "Promo name can be 100 characters at most.";
            } else {
                try {
                    product = savePromo(promoName, percent, id, employeeId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
                if (product == null && problem.isEmpty()) {
                    problem = "Product ID " + id + " was not found.";
                }
            }

            if (product == null) {
                out.println("<h1>Promo not created</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            } else {
                out.println("<h1>Promo created</h1>");
                out.println("<p>Promo: " + iosqlclass.esc(promoName) + "</p>");
                out.println("<p>Discount: " + percent + "% off</p>");
                // Blank product ID means the promo applies to all products
                if (id.isEmpty()) {
                    out.println("<p>Applies to: ALL products</p>");
                } else {
                    double regular = Double.parseDouble(product[1]);
                    double promoPrice = regular - (regular * percent / 100);
                    out.println("<p>Applies to: " + iosqlclass.esc(product[0]) + "</p>");
                    out.println("<p>Regular price: PHP " + String.format("%.2f", regular) + "</p>");
                    out.println("<p>Promo price: PHP " + String.format("%.2f", promoPrice) + "</p>");
                }
            }
            out.println("<p><a href='admin.jsp'>Back to Admin</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the promo in the promos table.
    // Returns { product name, product price } ({ "", "" } for a promo on all products),
    // or null when the product ID is not found.
    private String[] savePromo(String promoName, double percent, String code, int employeeId) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // A blank product ID means all products (product_id stays NULL)
            int productId = -1;
            String productName = "";
            String productPrice = "";
            if (!code.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT product_id, name, price FROM products WHERE code = ?")) {
                    ps.setString(1, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            productId = rs.getInt("product_id");
                            productName = rs.getString("name");
                            productPrice = String.valueOf(rs.getDouble("price"));
                        }
                    }
                }
                if (productId == -1) {
                    return null;
                }
            }

            // Save the promo
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO promos (product_id, created_by, name, percentage) VALUES (?, ?, ?, ?)")) {
                if (productId == -1) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, productId);
                }
                ps.setInt(2, employeeId);
                ps.setString(3, promoName);
                ps.setDouble(4, percent);
                ps.executeUpdate();
            }
            return new String[]{productName, productPrice};
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    // GET and POST both run processRequest()
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
