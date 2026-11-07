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
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 *
 * @author Group 2
 */
// Lists transactions (sales and refunds) from the database, filtered by type and who made them.
// URL: /do.transactions
@WebServlet(name = "viewtransactions", urlPatterns = {"/do.transactions"})
public class viewtransactions extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Transaction History</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
 
            // Filters picked on the page
            String type = request.getParameter("type");
            String by = request.getParameter("by");
 
            // Stop if the page was opened directly, without the form data
            if (type == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='transactions.jsp'>Go to Transactions</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            type = type.trim();
            if (by == null) {
                by = "";
            }
            by = by.trim();
 
            // Which kinds of transaction to include (ALL = both)
            boolean withSales = type.equals("ALL") || type.equals("SALE");
            boolean withRefunds = type.equals("ALL") || type.equals("REFUND");
            boolean byFilter = !by.isEmpty();
            // "Made by" is matched against the employee's username
            String byClause = byFilter ? "WHERE e.username = ? " : "";
 
            // One row per sold item (sale_items) and one row per refund. Both have the same columns.
            String salesPart = "SELECT s.sale_id AS id, s.sale_date AS tdate, 'SALE' AS ttype, "
                    + "p.name AS product, si.quantity AS qty, si.unit_price * si.quantity AS amount, "
                    + "e.username AS made_by "
                    + "FROM sale_items si "
                    + "JOIN sales s ON si.sale_id = s.sale_id "
                    + "JOIN products p ON si.product_id = p.product_id "
                    + "JOIN employees e ON s.employee_id = e.employee_id "
                    + byClause;
            String refundsPart = "SELECT r.refund_id AS id, r.refund_date AS tdate, 'REFUND' AS ttype, "
                    + "p.name AS product, r.quantity AS qty, r.refund_amount AS amount, "
                    + "e.username AS made_by "
                    + "FROM refunds r "
                    + "JOIN sale_items si ON r.sale_item_id = si.sale_item_id "
                    + "JOIN products p ON si.product_id = p.product_id "
                    + "JOIN employees e ON r.processed_by = e.employee_id "
                    + byClause;
 
            out.println("<h1>Transaction History</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Date</th><th>Type</th><th>Product</th><th>Qty</th><th>Amount (PHP)</th><th>By</th></tr>");
            int shown = 0;
 
            try {
                if (withSales || withRefunds) {
                    String sql;
                    int parts;
                    if (withSales && withRefunds) {
                        sql = "(" + salesPart + ") UNION ALL (" + refundsPart + ")";
                        parts = 2;
                    } else if (withSales) {
                        sql = salesPart;
                        parts = 1;
                    } else {
                        sql = refundsPart;
                        parts = 1;
                    }
                    // Newest first
                    sql = "SELECT * FROM (" + sql + ") t ORDER BY tdate DESC, id DESC";
 
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    iosqlclass db = new iosqlclass();
                    try (Connection conn = db.getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        if (byFilter) {
                            // the username is used once for each part of the query
                            for (int i = 1; i <= parts; i++) {
                                ps.setString(i, by);
                            }
                        }
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                boolean isSale = rs.getString("ttype").equals("SALE");
                                String code = String.format(isSale ? "SALE-%03d" : "REF-%03d", rs.getInt("id"));
                                out.println("<tr><td>" + code
                                        + "</td><td>" + dateFormat.format(rs.getTimestamp("tdate"))
                                        + "</td><td>" + iosqlclass.esc(rs.getString("ttype"))
                                        + "</td><td>" + iosqlclass.esc(rs.getString("product"))
                                        + "</td><td>" + rs.getInt("qty")
                                        + "</td><td>" + String.format("%.2f", rs.getDouble("amount"))
                                        + "</td><td>" + iosqlclass.esc(rs.getString("made_by"))
                                        + "</td></tr>");
                                shown++;
                            }
                        }
                    }
                }
                out.println("</table>");
                if (shown == 0) {
                    out.println("<p>No transactions found.</p>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load transactions from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='transactions.jsp'>Back to Transactions</a> | <a href='index.jsp'>Index</a></p>");
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