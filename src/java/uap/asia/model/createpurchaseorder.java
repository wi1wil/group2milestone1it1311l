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
// Creates a purchase order (form on inventory.jsp). URL: /do.createpo
@WebServlet(name = "createpurchaseorder", urlPatterns = {"/do.createpo"})
public class createpurchaseorder extends HttpServlet {

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
            out.println("<title>Create Purchase Order Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String supplier = request.getParameter("supplier");
            String id = request.getParameter("productid");
            String qtyText = request.getParameter("quantity");

            // Stop if the page was opened directly, without the form data
            if (supplier == null || id == null || qtyText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='inventory.jsp'>Go to Inventory</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            supplier = supplier.trim();
            id = id.trim();

            // The database keeps which employee created the order, so the user must be logged in
            HttpSession session = request.getSession(false);
            Integer employeeId = null;
            if (session != null) {
                employeeId = (Integer) session.getAttribute("e_id");
            }
            if (employeeId == null) {
                out.println("<h1>Purchase order not created</h1>");
                out.println("<p>Please login as an employee first.</p>");
                out.println("<a href='login.jsp'>Go to Login</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Convert quantity to a number (0 if invalid)
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyText.trim());
            } catch (NumberFormatException e) {
                qty = 0;
            }

            // order = { PO no, product name, estimated cost }, or null when the order was not saved
            String[] order = null;
            String problem = "";

            // Validate, then save the order
            if (supplier.isEmpty() || id.isEmpty()) {
                problem = "Supplier and product ID are required.";
            } else if (qty <= 0) {
                problem = "Quantity must be a whole number of at least 1.";
            } else {
                try {
                    order = savePurchaseOrder(supplier, id, qty, employeeId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
                // When nothing was saved, order[0] is not used; the reason is in problem
                if (order != null && order[0].isEmpty()) {
                    problem = order[1];
                    order = null;
                }
            }

            if (order == null) {
                out.println("<h1>Purchase order not created</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            } else {
                out.println("<h1>Purchase order created</h1>");
                out.println("<p>PO no: " + iosqlclass.esc(order[0]) + "</p>");
                out.println("<p>Supplier: " + iosqlclass.esc(supplier) + "</p>");
                out.println("<p>Product: " + iosqlclass.esc(order[1]) + "</p>");
                out.println("<p>Quantity: " + qty + "</p>");
                out.println("<p>Estimated cost at current price: PHP " + String.format("%.2f", Double.parseDouble(order[2]) * qty) + "</p>");
                out.println("<p>Status: Ordered</p>");
            }
            out.println("<p><a href='inventory.jsp'>Back to Inventory</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the purchase order (a purchase_orders row and its purchase_order_items row).
    // Returns { PO no, product name, unit cost } when saved.
    // Returns { "", reason } when the supplier or product is not found.
    private String[] savePurchaseOrder(String supplierName, String code, int qty, int employeeId) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // Find the supplier by name
            int supplierId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT supplier_id FROM suppliers WHERE name = ?")) {
                ps.setString(1, supplierName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        supplierId = rs.getInt("supplier_id");
                    }
                }
            }
            if (supplierId == -1) {
                return new String[]{"", "Supplier " + supplierName + " was not found. Add it on the Suppliers page first."};
            }

            // Find the product by its ID (code)
            int productId = -1;
            String productName = "";
            double unitCost = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT product_id, name, price FROM products WHERE code = ?")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getInt("product_id");
                        productName = rs.getString("name");
                        unitCost = rs.getDouble("price");
                    }
                }
            }
            if (productId == -1) {
                return new String[]{"", "Product ID " + code + " was not found. Add it on the Inventory page first."};
            }

            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                // the purchase order (status starts as Ordered)
                int poId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO purchase_orders (supplier_id, created_by, order_date, status) "
                        + "VALUES (?, ?, CURDATE(), 'Ordered')", Statement.RETURN_GENERATED_KEYS)) { // generates the database keys immediately
                    ps.setInt(1, supplierId);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { // grabs the generated key 
                        if (keys.next()) {
                            poId = keys.getInt(1); // sets poId into the generated key
                        }
                    }
                }
                if (poId == -1) {
                    throw new SQLException("No po_id was returned for the new purchase order.");
                }

                // the ordered product (the current price is used as the unit cost)
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO purchase_order_items (po_id, product_id, quantity, unit_cost) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, poId); // insert into 
                    ps.setInt(2, productId);
                    ps.setInt(3, qty);
                    ps.setDouble(4, unitCost);
                    ps.executeUpdate();
                }
                conn.commit();
                return new String[]{String.format("PO-%03d", poId), productName, String.valueOf(unitCost)};
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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
