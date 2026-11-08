/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.SqlcPurchasing;
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
import asia.uap.classes.PurchaseOrder;
import asia.uap.classes.PurchaseOrderItem;

/**
 *
 * @author Group 2
 */
// Receiving stock from a purchase order, approved by the admin. URL: /do.restock
//
// GET  lists the purchase orders that are still Ordered (waiting for delivery / approval).
//      Inventory staff can see them; only the admin gets the Approve and Cancel buttons.
// POST action = receive -> (admin) stock goes up, new products become active, PO becomes Received
//               cancel  -> (admin) PO becomes Cancelled, stock is not changed
//      poid = the po_id (the old stock.jsp sent po=PO-001, which is also accepted)
// Every POST saves a message and goes back to this page (redirect).
@WebServlet(name = "receivestock", urlPatterns = {"/do.restock"})
public class receivestock extends HttpServlet {

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
        HttpSession session = request.getSession(false);
        if (UserSession.employeeId(session) == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        boolean admin = UserSession.isAdmin(session);

        // POST: approve or cancel, then come back to this page
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            handleAction(request, session, admin);
            response.sendRedirect("do.restock");
            return;
        }

        // GET: show the waiting purchase orders
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Receive Stock</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>Receive Stock</h1>");
            out.println(PageHelper.popMessage(session));

            if (admin) {
                out.println("<p>Check the delivery against the order, then approve it to add the stock.</p>");
            } else {
                out.println("<p>These orders are waiting for the delivery. The <b>admin</b> approves them when the stock arrives.</p>");
            }

            ArrayList<PurchaseOrder> waiting;
            try {
                waiting = SqlcPurchasing.getPurchaseOrders(PurchaseOrder.STATUS_ORDERED);
            } catch (SQLException e) {
                e.printStackTrace();
                waiting = null;
                out.println("<p>Could not load purchase orders from the database. Please try again.</p>");
            }

            if (waiting != null && waiting.isEmpty()) {
                out.println("<p>No purchase orders are waiting. <a href='do.createpo'>Create one</a>.</p>");
            }
            if (waiting != null) {
                for (PurchaseOrder po : waiting) {
                    out.println(orderHtml(po, admin));
                }
            }

            out.println("<p><a href='do.purchaseorders?status=all'>All purchase orders</a> | "
                    + "<a href='stock.jsp'>Back to Stock</a> | <a href='inventory.jsp'>Inventory</a></p>"
                    + "<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Approve (receive) or cancel one purchase order. Only the admin may do this.
    private void handleAction(HttpServletRequest request, HttpSession session, boolean admin) {
        if (!admin) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Only the admin can approve or cancel a delivery.");
            return;
        }

        // poid=7, or the old form's po=PO-007
        String poText = request.getParameter("poid");
        if (poText == null) {
            poText = request.getParameter("po");
        }
        Integer poId = ParseThis.toId(poText, "PO");
        if (poId == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "No purchase order was selected.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "receive"; // the old stock.jsp form only had a Receive button
        }

        try {
            if (action.equals("receive")) {
                PurchaseOrder po = SqlcPurchasing.receivePurchaseOrder(poId);
                int units = 0;
                for (PurchaseOrderItem item : po.getItems()) {
                    units += item.getQuantity();
                }
                PageHelper.setMessage(session, PageHelper.SUCCESS, po.getPoNumber() + " received: " + units
                        + " unit(s) of " + po.getItems().size() + " product(s) added to stock.");
            } else if (action.equals("cancel")) {
                SqlcPurchasing.cancelPurchaseOrder(poId);
                PageHelper.setMessage(session, PageHelper.INFO, String.format("PO-%03d", poId) + " was cancelled. Stock was not changed.");
            } else {
                PageHelper.setMessage(session, PageHelper.ERROR, "Unknown action.");
            }
        } catch (IllegalArgumentException e) {
            PageHelper.setMessage(session, PageHelper.ERROR, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not update the database. Nothing was changed.");
        }
    }

    // One purchase order: its details, its lines, and (for the admin) the buttons
    private String orderHtml(PurchaseOrder po, boolean admin) {
        String html = "<h3>" + po.getPoNumber() + " - " + PageHelper.esc(po.getSuppliername()) + "</h3>";
        html += "<p>Ordered on " + PageHelper.date(po.getOrderdate()) + " by " + PageHelper.esc(po.getCreatedbyname())
                + " | Status: <b>" + PageHelper.esc(po.getStatus()) + "</b></p>";

        html += "<table border='1'>";
        html += "<tr><th>Code</th><th>Product</th><th>Qty</th><th>Unit cost</th><th>Subtotal</th></tr>";
        for (PurchaseOrderItem item : po.getItems()) {
            html += "<tr><td>" + PageHelper.esc(item.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                    + "<td>" + item.getQuantity() + "</td>"
                    + "<td>" + PageHelper.money(item.getUnitcost()) + "</td>"
                    + "<td>" + PageHelper.money(item.getSubtotal()) + "</td></tr>";
        }
        html += "</table>";
        html += "<p>Total cost: PHP " + PageHelper.money(po.getTotalCost()) + "</p>";

        if (admin) {
            html += "<form action='do.restock' method='POST' style='display:inline'>"
                    + "<input type='hidden' name='poid' value='" + po.getPoid() + "'>"
                    + "<input type='hidden' name='action' value='receive'>"
                    + "<input type='submit' value='Approve and receive'"
                    + " onclick=\"return confirm('Add this delivery to the stock?');\">"
                    + "</form> &nbsp; ";
            html += "<form action='do.restock' method='POST' style='display:inline'>"
                    + "<input type='hidden' name='poid' value='" + po.getPoid() + "'>"
                    + "<input type='hidden' name='action' value='cancel'>"
                    + "<input type='submit' value='Cancel order'"
                    + " onclick=\"return confirm('Cancel this purchase order?');\">"
                    + "</form>";
        } else {
            html += "<p><i>Waiting for admin approval.</i></p>";
        }
        return html + "<hr>";
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
        return "Receive stock (admin approval)";
    }// </editor-fold>

}
