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
// Lists products from the database, filtered by category. URL: /do.products
@WebServlet(name = "viewproducts", urlPatterns = {"/do.products"})
public class viewproducts extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Product List</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
 
            // Category picked in the dropdown ("all" shows everything)
            String category = request.getParameter("category");
 
            // Stop if the page was opened directly, without the form data
            if (category == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='products.jsp'>Go to Products</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            category = category.trim();
 
            // Build the query. The category filter is only added when a category was picked.
            boolean showAll = category.equals("all") || category.isEmpty();
            String sql = "SELECT p.code, p.name, c.name AS category, p.price, p.stock_quantity "
                    + "FROM products p JOIN categories c ON p.category_id = c.category_id "
                    + "WHERE p.status = 'active' "
                    + (showAll ? "" : "AND c.name = ? ")
                    + "ORDER BY p.code";
 
            out.println("<h1>Product Catalog</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Price (PHP)</th><th>Stock</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (!showAll) {
                        ps.setString(1, category);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        // Print one table row per product found in the database
                        while (rs.next()) {
                            int stock = rs.getInt("stock_quantity");
                            out.println("<tr><td>" + iosqlclass.esc(rs.getString("code"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("name"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("category"))
                                    + "</td><td>" + String.format("%.2f", rs.getDouble("price"))
                                    + "</td><td>" + stock
                                    + (stock <= 5 ? " <b>(LOW)</b>" : "")
                                    + "</td></tr>");
                            count++;
                        }
                    }
                }
                out.println("</table>");
                out.println("<p>" + count + " product(s) found.</p>");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load products from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='products.jsp'>Back to Products</a> | <a href='index.jsp'>Index</a></p>");
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
