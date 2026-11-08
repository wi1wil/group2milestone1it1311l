/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;
 
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
import asia.uap.functionalclasses.PageHelper;
 
/**
 *
 * @author Group 2
 */
// Lists stock levels from the database with low-stock alerts. URL: /do.stock
@WebServlet(name = "viewstock", urlPatterns = {"/do.stock"})
public class viewstock extends HttpServlet {
 
    // Low stock means this many items or fewer (same limit as addproduct)
    private static final int LOW_STOCK = 5;
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Stock Levels</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // "all" shows every product, "low" shows only low stock
            String filter = request.getParameter("filter");
 
            // Stop if the page was opened directly, without the form data
            if (filter == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='stock.jsp'>Go to Stock</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            filter = filter.trim();
 
            // Anything other than "low" shows every product. Lowest stock is listed first.
            // EDITED: sold-out products (status "outofstock") are shown too; they are the ones that need restocking most.
            boolean lowOnly = filter.equals("low");
            String sql = "SELECT p.code, p.name, c.name AS category, p.price, p.stock_quantity "
                    + "FROM products p JOIN categories c ON p.category_id = c.category_id "
                    + "WHERE p.status IN ('active', 'outofstock') "
                    + (lowOnly ? "AND p.stock_quantity <= ? " : "")
                    + "ORDER BY p.stock_quantity, p.code";
 
            out.println("<h1>Stock levels</h1>");
            out.println("<p>Low stock alert at " + LOW_STOCK + " or fewer.</p>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Alert</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (lowOnly) {
                        ps.setInt(1, LOW_STOCK);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int stock = rs.getInt("stock_quantity");
                            out.println("<tr><td>" + iosqlclass.esc(rs.getString("code"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("name"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("category"))
                                    + "</td><td>" + String.format("%.2f", rs.getDouble("price"))
                                    + "</td><td>" + stock
                                    + "</td><td>" + (stock <= 0 ? "OUT OF STOCK" : (stock <= LOW_STOCK ? "LOW STOCK" : ""))
                                    + "</td></tr>");
                            count++;
                        }
                    }
                }
                out.println("</table>");
                out.println("<p>" + count + " product(s) shown.</p>");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load stock from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='stock.jsp'>Back to Stock</a> | <a href='inventory.jsp'>Inventory</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
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
