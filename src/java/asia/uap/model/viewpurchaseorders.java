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
// Lists purchase orders from the database, filtered by status. URL: /do.purchaseorders
@WebServlet(name = "viewpurchaseorders", urlPatterns = {"/do.purchaseorders"})
public class viewpurchaseorders extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Purchase Orders</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
 
            // Status picked in the dropdown ("all" shows everything)
            String status = request.getParameter("status");
 
            // Stop if the page was opened directly, without the form data
            if (status == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='inventory.jsp'>Go to Inventory</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            status = status.trim();
 
            // One row per ordered product. The status filter is only added when a status was picked.
            boolean showAll = status.equals("all") || status.isEmpty();
            String sql = "SELECT po.po_id, s.name AS supplier, p.name AS product, i.quantity, po.status "
                    + "FROM purchase_orders po "
                    + "JOIN suppliers s ON po.supplier_id = s.supplier_id "
                    + "JOIN purchase_order_items i ON i.po_id = po.po_id "
                    + "JOIN products p ON i.product_id = p.product_id "
                    + (showAll ? "" : "WHERE po.status = ? ")
                    + "ORDER BY po.po_id DESC";
 
            out.println("<h1>Purchase orders</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>PO</th><th>Supplier</th><th>Product</th><th>Qty</th><th>Status</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (!showAll) {
                        ps.setString(1, status);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            // Same PO number format as createpurchaseorder (PO-001)
                            out.println("<tr><td>" + String.format("PO-%03d", rs.getInt("po_id"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("supplier"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("product"))
                                    + "</td><td>" + rs.getInt("quantity")
                                    + "</td><td>" + iosqlclass.esc(rs.getString("status"))
                                    + "</td></tr>");
                            count++;
                        }
                    }
                }
                out.println("</table>");
                out.println("<p>" + count + " purchase order(s) found.</p>");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load purchase orders from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='inventory.jsp'>Back to Inventory</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
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
