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
// Customer buys a product (form on products.jsp). URL: /do.buyproduct
@WebServlet(name = "buyproduct", urlPatterns = {"/do.buyproduct"})
public class buyproduct extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Order Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
 
            // Read the form fields
            String id = request.getParameter("productid");
            String qtyText = request.getParameter("quantity");
 
            // Stop if the page was opened directly, without the form data
            if (id == null || qtyText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='products.jsp'>Go to Products</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            id = id.trim();
 
            // Convert quantity to a number (0 if invalid)
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyText.trim());
            } catch (NumberFormatException e) {
                qty = 0;
            }
 
            // result = { name, price each, stock left }, or { "", reason } when the order failed
            String[] result = null;
            String problem = "";
 
            if (id.isEmpty()) {
                problem = "Product ID is required.";
            } else if (qty <= 0) {
                problem = "Quantity must be a whole number of at least 1.";
            } else {
                try {
                    result = buy(id, qty);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
                // When the order failed, result[0] is empty and the reason is in result[1]
                if (result != null && result[0].isEmpty()) {
                    problem = result[1];
                    result = null;
                }
            }
 
            if (result == null) {
                out.println("<h1>Order failed</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            } else {
                double price = Double.parseDouble(result[1]);
                out.println("<h1>Order confirmed</h1>");
                out.println("<p>Order no: OR-" + (System.currentTimeMillis() % 100000) + "</p>");
                out.println("<p>" + iosqlclass.esc(result[0]) + " x " + qty + "</p>");
                out.println("<p>Price each: PHP " + String.format("%.2f", price) + "</p>");
                out.println("<p><b>Total: PHP " + String.format("%.2f", price * qty) + "</b></p>");
                out.println("<p>Stock left after this order: " + result[2] + "</p>");
            }
            out.println("<p><a href='products.jsp'>Back to Products</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
 
    // Checks the product and stock, then subtracts the quantity from stock_quantity.
    // Returns { name, price, stock left } when bought, or { "", reason } when not.
    private String[] buy(String code, int qty) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {
            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                String name = null;
                double price = 0;
                int stock = 0;
 
                // FOR UPDATE locks the row so two buyers cannot take the same stock
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT name, price, stock_quantity FROM products "
                        + "WHERE code = ? AND status = 'active' FOR UPDATE")) {
                    ps.setString(1, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            name = rs.getString("name");
                            price = rs.getDouble("price");
                            stock = rs.getInt("stock_quantity");
                        }
                    }
                }
                if (name == null) {
                    conn.rollback();
                    return new String[]{"", "Product ID " + code + " was not found."};
                }
                if (qty > stock) {
                    conn.rollback();
                    return new String[]{"", "Only " + stock + " of " + name + " in stock."};
                }
 
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock_quantity = stock_quantity - ? WHERE code = ?")) {
                    ps.setInt(1, qty);
                    ps.setString(2, code);
                    ps.executeUpdate();
                }
                conn.commit();
                return new String[]{name, String.valueOf(price), String.valueOf(stock - qty)};
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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
