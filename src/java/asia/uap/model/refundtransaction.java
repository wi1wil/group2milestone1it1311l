/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.SqlcSales;
import asia.uap.functionalclasses.UserSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import asia.uap.classes.CartItem;
import asia.uap.classes.Sale;
import asia.uap.classes.SaleItem;

/**
 *
 * @author Group 2
 */
// Cashier refund for returned products. URL: /do.refund
//
// Where the items can be:
//   1. In the DATABASE  = already paid (sales + sale_items). They are refunded here:
//                         a refunds row is saved and the items can go back to stock.
//   2. In the SESSION   = - the last receipt of this cashier (lastSaleId): a shortcut button opens it here.
//                         - the unpaid cart (unpaidCart): nothing was paid yet, so there is nothing to
//                           refund. Those items are removed on the POS page instead (Remove / Delete).
//
// GET              -> search form (sale ID / receipt number)
// GET ?saleid=N    -> the sale with every item, how many were already refunded, and a refund form per item
// POST saleid, saleitemid, quantity, reason, restock
//                  -> SqlcSales.saveRefund (one transaction), then back to the sale (redirect)
//
// Refund amount = the unit price the customer paid (after the promo) x quantity returned.
// A line can never be refunded more times than it was sold.
@WebServlet(name = "refundtransaction", urlPatterns = {"/do.refund"})
public class refundtransaction extends HttpServlet {

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
        // The refund is saved under the logged-in employee (refunds.processed_by)
        HttpSession session = request.getSession(false);
        Integer employeeId = UserSession.employeeId(session);
        if (employeeId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // saleid=7 or the receipt number TX-0007
        Integer saleId = ParseThis.toId(request.getParameter("saleid"), "TX");

        // POST: refund one item, then show the sale again
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (saleId == null) {
                PageHelper.setMessage(session, PageHelper.ERROR, "No sale was selected.");
                response.sendRedirect("do.refund");
                return;
            }
            refundItem(request, session, saleId, employeeId);
            response.sendRedirect("do.refund?saleid=" + saleId);
            return;
        }

        // GET: show the page
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Returns and Refunds</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>Returns and Refunds</h1>");
            out.println(PageHelper.popMessage(session));

            // Find a sale
            out.println("<form action='do.refund' method='GET'>");
            out.println("<label>Sale ID or receipt no. (e.g. 7 or TX-0007): </label>");
            out.println("<input type='text' name='saleid' value='" + (saleId == null ? "" : saleId) + "'>");
            out.println("<input type='submit' value='Find sale'>");
            out.println("</form>");

            // Session shortcuts: last receipt and the unpaid cart
            out.println(sessionHtml(session));

            // The chosen sale
            if (saleId != null) {
                try {
                    Sale sale = SqlcSales.getSale(saleId);
                    if (sale == null) {
                        out.println("<p>Sale " + String.format("TX-%04d", saleId) + " was not found.</p>");
                    } else {
                        printSale(out, sale);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    out.println("<p>Could not load the sale from the database. Please try again.</p>");
                }
            }

            out.println("<p><a href='cashier.jsp'>Back to Cashier</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // POST: validate the form, then save the refund
    private void refundItem(HttpServletRequest request, HttpSession session, int saleId, int employeeId) {
        // Read the form fields
        Integer saleItemId = ParseThis.toInt(request.getParameter("saleitemid"));
        String qtyText = request.getParameter("quantity");
        String reason = request.getParameter("reason");
        // the checkbox only sends "yes" when it is ticked
        boolean restock = ParseThis.toBoolean(request.getParameter("restock"));

        if (saleItemId == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "No item was selected.");
            return;
        }
        if (!ParseThis.isInt(qtyText) || ParseThis.toInt(qtyText) <= 0) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be a whole number of at least 1.");
            return;
        }
        int qty = ParseThis.toInt(qtyText);

        // Reason is optional, but refunds.reason is only 100 characters
        if (ParseThis.isBlank(reason)) {
            reason = "no reason given";
        }
        reason = reason.trim();
        if (reason.length() > 100) {
            reason = reason.substring(0, 100);
        }

        try {
            // result = { refund_id, product name, amount }
            String[] result = SqlcSales.saveRefund(saleId, saleItemId, employeeId, qty, reason, restock);
            PageHelper.setMessage(session, PageHelper.SUCCESS, "Refund " + String.format("RF-%04d", Integer.parseInt(result[0]))
                    + " processed: " + qty + " x " + result[1] + ". Give back PHP " + PageHelper.money(Double.parseDouble(result[2]))
                    + (restock ? ". Items returned to stock." : ". Items NOT returned to stock (damaged)."));
        } catch (IllegalArgumentException e) {
            PageHelper.setMessage(session, PageHelper.ERROR, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not save the refund. Nothing was changed. Please try again.");
        }
    }

    // What is in this cashier's session: the last receipt (refundable) and the unpaid cart (not refundable)
    private String sessionHtml(HttpSession session) {
        String html = "";
        Integer lastSaleId = (Integer) session.getAttribute("lastSaleId");
        if (lastSaleId != null) {
            html += "<p>Last receipt of this session: <a href='do.refund?saleid=" + lastSaleId + "'>"
                    + String.format("TX-%04d", lastSaleId) + "</a></p>";
        }
        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        if (cart != null && !cart.isEmpty()) {
            html += "<p><i>There are " + cart.size() + " unpaid item(s) in the current cart. They were not paid yet, "
                    + "so remove them on the <a href='cashier.jsp'>POS page</a> instead of refunding them.</i></p>";
        }
        return html;
    }

    // The sale with a refund form for every item that can still be returned
    private void printSale(PrintWriter out, Sale sale) {
        out.println("<h2>Sale " + sale.getSaleNumber() + "</h2>");
        out.println("<p>Date: " + PageHelper.dateTime(sale.getSaledate()) + " | Cashier: " + PageHelper.esc(sale.getCashiername())
                + " | Customer: " + PageHelper.esc(sale.getCustomername()) + "</p>");
        out.println("<p>Total paid: PHP " + PageHelper.money(sale.getTotal()) + " (" + PageHelper.esc(sale.getPaymentmethod()) + ")</p>");

        out.println("<table border='1'>");
        out.println("<tr><th>Code</th><th>Product</th><th>Unit price paid</th><th>Sold</th><th>Refunded</th><th>Refund</th></tr>");
        for (SaleItem item : sale.getItems()) {
            String row = "<tr><td>" + PageHelper.esc(item.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                    + "<td>" + PageHelper.money(item.getUnitprice()) + "</td>"
                    + "<td>" + item.getQuantity() + "</td>"
                    + "<td>" + item.getRefundedquantity() + "</td>";
            if (item.getRefundableQuantity() > 0) {
                row += "<td><form action='do.refund' method='POST'>"
                        + "<input type='hidden' name='saleid' value='" + sale.getSaleid() + "'>"
                        + "<input type='hidden' name='saleitemid' value='" + item.getSaleitemid() + "'>"
                        + "Qty: <input type='text' name='quantity' value='1' size='3'> "
                        + "(max " + item.getRefundableQuantity() + ") "
                        + "Reason: <input type='text' name='reason' maxlength='100'> "
                        + "<label><input type='checkbox' name='restock' value='yes' checked> Return to stock</label> "
                        + "<input type='submit' value='Refund'"
                        + " onclick=\"return confirm('Refund this item?');\">"
                        + "</form></td>";
            } else {
                row += "<td>Fully refunded</td>";
            }
            out.println(row + "</tr>");
        }
        out.println("</table>");
        out.println("<p><small>Untick \"Return to stock\" for damaged items that cannot be sold again.</small></p>");
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
        return "Returns and refunds";
    }// </editor-fold>

}
