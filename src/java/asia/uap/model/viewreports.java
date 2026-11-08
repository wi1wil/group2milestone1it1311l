/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.Sqlc;
import asia.uap.classes.JobOrder;
import asia.uap.classes.Product;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Group 2
 */
// Prints the sales summary or the low-stock report from the database. URL: /do.reports
@WebServlet(name = "viewreports", urlPatterns = {"/do.reports"})
public class viewreports extends HttpServlet {

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
            out.println("<title>Report Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Report picked on the page: summary or lowstock
            String type = request.getParameter("type");

            // Stop if the page was opened directly, without the form data
            if (type == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='reports.jsp'>Go to Reports</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // EDITED: both reports read the database (they used tempdata.java before)
            if (type.equals("lowstock")) {
                printLowStock(out);
            } else {
                try {
                    printSummary(out);
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("<p>Could not load the report from the database. Please try again.</p>");
                }
            }
            out.println("<p><a href='reports.jsp'>Back to Reports</a> | <a href='transactions.jsp'>Transactions</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Low stock report: products with Sqlc.LOW_STOCK (5) or fewer left, sold out ones included
    private void printLowStock(PrintWriter out) {
        out.println("<h1>Low stock items</h1>");
        ArrayList<Product> low = Sqlc.getLowStockProducts();
        if (low.isEmpty()) {
            out.println("<p>No products are low on stock.</p>");
            return;
        }
        out.println("<table border='1'>");
        out.println("<tr><th>Code</th><th>Product</th><th>Category</th><th>Stock</th><th>Status</th></tr>");
        for (Product p : low) {
            out.println("<tr><td>" + PageHelper.esc(p.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(p.getName()) + "</td>"
                    + "<td>" + PageHelper.esc(p.getCategory()) + "</td>"
                    + "<td>" + p.getStockquantity() + "</td>"
                    + "<td>" + PageHelper.esc(p.getStatus()) + "</td></tr>");
        }
        out.println("</table>");
        out.println("<p>" + low.size() + " product(s) need restocking. <a href='do.createpo'>Create a purchase order</a></p>");
    }

    // Sales summary: totals from sales, refunds and job_orders, plus the best sellers
    private void printSummary(PrintWriter out) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            int saleCount = 0;
            double sales = 0;
            double discounts = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS n, COALESCE(SUM(total), 0) AS total, COALESCE(SUM(discount), 0) AS discount FROM sales");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    saleCount = rs.getInt("n");
                    sales = rs.getDouble("total");
                    discounts = rs.getDouble("discount");
                }
            }

            int refundCount = 0;
            double refunds = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS n, COALESCE(SUM(refund_amount), 0) AS total FROM refunds");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    refundCount = rs.getInt("n");
                    refunds = rs.getDouble("total");
                }
            }

            int jobs = 0;
            int jobsDone = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS n, COALESCE(SUM(status = ?), 0) AS done FROM job_orders")) {
                ps.setString(1, JobOrder.SUCCESS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        jobs = rs.getInt("n");
                        jobsDone = rs.getInt("done");
                    }
                }
            }

            out.println("<h1>Sales summary</h1>");
            out.println("<table border='1'>");
            out.println("<tr><td>Sales</td><td>" + saleCount + "</td></tr>");
            out.println("<tr><td>Total sales (PHP)</td><td>" + PageHelper.money(sales) + "</td></tr>");
            out.println("<tr><td>Discounts given (PHP)</td><td>" + PageHelper.money(discounts) + "</td></tr>");
            out.println("<tr><td>Refunds</td><td>" + refundCount + "</td></tr>");
            out.println("<tr><td>Total refunds (PHP)</td><td>" + PageHelper.money(refunds) + "</td></tr>");
            out.println("<tr><td><b>Net sales (PHP)</b></td><td><b>" + PageHelper.money(sales - refunds) + "</b></td></tr>");
            out.println("<tr><td>PC build jobs completed</td><td>" + jobsDone + " of " + jobs + "</td></tr>");
            out.println("</table>");

            // Best sellers: most units sold (minus refunded units)
            out.println("<h3>Top 5 products</h3>");
            out.println("<table border='1'>");
            out.println("<tr><th>Code</th><th>Product</th><th>Units sold</th><th>Sales (PHP)</th></tr>");
            int shown = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.code, p.name, SUM(si.quantity) AS units, SUM(si.quantity * si.unit_price) AS amount "
                    + "FROM sale_items si JOIN products p ON si.product_id = p.product_id "
                    + "GROUP BY p.product_id, p.code, p.name ORDER BY units DESC, amount DESC LIMIT 5");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.println("<tr><td>" + PageHelper.esc(rs.getString("code")) + "</td>"
                            + "<td>" + PageHelper.esc(rs.getString("name")) + "</td>"
                            + "<td>" + rs.getInt("units") + "</td>"
                            + "<td>" + PageHelper.money(rs.getDouble("amount")) + "</td></tr>");
                    shown++;
                }
            }
            out.println("</table>");
            if (shown == 0) {
                out.println("<p>No sales yet.</p>");
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
