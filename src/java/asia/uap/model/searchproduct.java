/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;
 
import asia.uap.functionalclasses.PageHelper;
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
// Searches products by text (form on products.jsp). URL: /do.searchproduct
@WebServlet(name = "searchproduct", urlPatterns = {"/do.searchproduct"})
public class searchproduct extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Search Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
 
            // Text typed in the search box
            String q = request.getParameter("q");
            // Stop if the page was opened directly, without the form data
            if (q == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='products.jsp'>Go to Products</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            q = q.trim();
 
            // Make % and _ typed by the user match as normal characters, then wrap in % for "contains"
            String pattern = "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
 
            // Match the text against ID (code), name, brand and category (LIKE ignores upper/lower case)
            String sql = "SELECT p.code, p.name, c.name AS category, p.price, p.stock_quantity, p.image "   // EDITED: + picture
                    + "FROM products p JOIN categories c ON p.category_id = c.category_id "
                    + "WHERE p.status = 'active' "
                    + "AND (p.code LIKE ? OR p.name LIKE ? OR p.brand LIKE ? OR c.name LIKE ?) "
                    + "ORDER BY p.code";
 
            out.println("<h1>Search results for: " + iosqlclass.esc(q) + "</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th></th><th>ID</th><th>Name</th><th>Category</th><th>Price (PHP)</th><th>Stock</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (int i = 1; i <= 4; i++) {
                        ps.setString(i, pattern);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            // EDITED: a small picture first (placeholder when the product has none)
                            out.println("<tr><td>" + PageHelper.productImage(rs.getString("image"), rs.getString("name"), "product-thumb")
                                    + "</td><td>" + iosqlclass.esc(rs.getString("code"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("name"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("category"))
                                    + "</td><td>" + String.format("%.2f", rs.getDouble("price"))
                                    + "</td><td>" + rs.getInt("stock_quantity")
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
                out.println("<p>Could not search the database. Please try again.</p>");
            }
 
            out.println("<p><a href='products.jsp'>Back to Products</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
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