/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
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
// Admin changes a product's price (form on admin.jsp). URL: /do.editprice
@WebServlet(name = "editprice", urlPatterns = {"/do.editprice"})
public class editprice extends HttpServlet {

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
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Edit Price Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Read the form fields
            String id = request.getParameter("productid");
            String newPriceText = request.getParameter("newprice");

            // Stop if the page was opened directly, without the form data
            if (id == null || newPriceText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='admin.jsp'>Go to Admin</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            id = id.trim();

            // Convert the new price to a number (-1 means invalid)
            double newPrice = -1;
            try {
                newPrice = Double.parseDouble(newPriceText.trim());
            } catch (NumberFormatException e) {
                newPrice = -1;
            }

            // product = { name, old price }, or null when the product ID is not found
            String[] product = null;
            String problem = "";

            if (newPrice < 0) {
                problem = "New price must be a number of 0 or more.";
            } else if (id.isEmpty()) {
                problem = "Product ID is required.";
            } else {
                try {
                    product = changePrice(id, newPrice);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
                if (product == null && problem.isEmpty()) {
                    problem = "Product ID " + id + " was not found.";
                }
            }

            if (product == null) {
                out.println("<h1>Price not changed</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            } else {
                // Show the old price, new price and the change
                double oldPrice = Double.parseDouble(product[1]);
                double difference = newPrice - oldPrice;
                double percent = 0;
                if (oldPrice > 0) {
                    percent = difference / oldPrice * 100;
                }
                out.println("<h1>Price updated</h1>");
                out.println("<p>Product: " + iosqlclass.esc(product[0]) + "</p>");
                out.println("<p>Old price: PHP " + String.format("%.2f", oldPrice) + "</p>");
                out.println("<p>New price: PHP " + String.format("%.2f", newPrice) + "</p>");
                out.println("<p>Change: " + String.format("%.2f", difference) + " (" + String.format("%.1f", percent) + "%)</p>");
            }
            out.println("<p><a href='admin.jsp'>Back to Admin</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Changes the price in the products table.
    // Returns { product name, old price }, or null when the product ID is not found.
    private String[] changePrice(String code, double newPrice) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // Find the product and its current price
            String name = null;
            double oldPrice = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, price FROM products WHERE code = ?")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        name = rs.getString("name");
                        oldPrice = rs.getDouble("price");
                    }
                }
            }
            if (name == null) {
                return null;
            }

            // Save the new price
            try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET price = ? WHERE code = ?")) {
                ps.setDouble(1, newPrice);
                ps.setString(2, code);
                ps.executeUpdate();
            }
            return new String[]{name, String.valueOf(oldPrice)};
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
