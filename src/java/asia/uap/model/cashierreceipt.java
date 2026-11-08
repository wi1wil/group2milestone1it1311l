/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.SqlcSales;
import asia.uap.functionalclasses.UserSession;
import asia.uap.classes.CartItem;
import asia.uap.classes.Sale;
import asia.uap.classes.SaleItem;
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

/**
 *
 * @author Andrei
 */
// Payment: checks the payment, saves the sale and prints the receipt (form on the checkout page). URL: /do.finishcashier
//
// POST (the Pay button):
//   1. The cart must not be empty (a second click on Pay finds an empty cart, so it cannot be paid twice)
//   2. Payment method must be cash, card or cashless
//   3. Cash must be at least the total. Not enough = the sale does NOT push through.
//      Card / cashless are charged the exact total.
//   4. Customer: the typed username (required: every customer has an account)
//   5. SqlcSales.saveSale saves sales + sale_items and lowers the stock in one transaction
//   6. The cart and the other unpaid session attributes are removed; lastSaleId is kept for reprinting
//   7. The receipt is printed (read back from the database)
//
// GET: reprints the last receipt of this session (lastSaleId), or ?saleid=N for any sale.
@WebServlet(name = "cashierreceipt", urlPatterns = {"/do.finishcashier"})
public class cashierreceipt extends HttpServlet {

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
        Integer employeeId = UserSession.employeeId(session);
        if (employeeId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Receipt</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            if ("POST".equalsIgnoreCase(request.getMethod())) {
                pay(request, session, employeeId, out);
            } else {
                reprint(request, session, out);
            }

            out.println("<p><a href='cashier.jsp'>New transaction</a> | <a href='do.refund'>Refunds</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // POST: verify the payment, save the sale, print the receipt
    private void pay(HttpServletRequest request, HttpSession session, int employeeId, PrintWriter out) {
        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");

        // 1. Something to pay for?
        if (cart == null || cart.isEmpty()) {
            out.println("<h1>Nothing to pay</h1>");
            out.println("<p>The cart is empty. If you clicked Pay twice, the sale was already saved.</p>");
            Integer lastSaleId = (Integer) session.getAttribute("lastSaleId");
            if (lastSaleId != null) {
                out.println("<p><a href='do.finishcashier'>Show the last receipt (" + String.format("TX-%04d", lastSaleId) + ")</a></p>");
            }
            return;
        }

        // Read the form fields
        String mop = request.getParameter("mop");
        String paymentText = request.getParameter("payment");
        String customer = request.getParameter("customer");

        // Total due (the discount is already inside each line's subtotal)
        double total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        total = PageHelper.round2(total);

        // 2. Payment method: only the ones in the dropdown
        String method;
        if ("cash".equals(mop)) {
            method = "Cash";
        } else if ("card".equals(mop)) {
            method = "Card";
        } else if ("cashless".equals(mop)) {
            method = "Cashless";
        } else {
            paymentFailed(out, "Choose a payment method.");
            return;
        }

        // 3. Amount paid
        double paid;
        if (method.equals("Cash")) {
            if (ParseThis.isBlank(paymentText) || !ParseThis.isDouble(paymentText)) {
                paymentFailed(out, "Type the amount of cash received.");
                return;
            }
            paid = PageHelper.round2(ParseThis.toDouble(paymentText));
            if (paid < total) {
                paymentFailed(out, "Total is PHP " + PageHelper.money(total) + " but only PHP " + PageHelper.money(paid)
                        + " was paid. Short by PHP " + PageHelper.money(total - paid) + ".");
                return;
            }
        } else {
            // card and cashless are charged exactly the total, so there is no change
            paid = total;
        }

        // 4. Customer: every customer must have an account (EDITED: no walk-in fallback any more)
        if (ParseThis.isBlank(customer)) {
            paymentFailed(out, "Type the customer's username. Every customer needs an account.");
            out.println("<p><a href='add.jsp'>Register a new customer</a></p>");
            return;
        }
        String username = customer.trim();
        Integer customerId = Sqlc.findCustomerId(username);
        if (customerId == null) {
            paymentFailed(out, "Customer " + username + " was not found. Register them first.");
            out.println("<p><a href='add.jsp'>Register a new customer</a></p>");
            return;
        }

        // 5. Save the sale (throws IllegalArgumentException when the stock ran out in the meantime).
        // The cart is taken out of the session first, so a fast second click on Pay finds no cart.
        session.removeAttribute("unpaidCart");
        int saleId;
        try {
            saleId = SqlcSales.saveSale(employeeId, customerId, cart, paid, method);
        } catch (IllegalArgumentException e) {
            session.setAttribute("unpaidCart", cart); // not paid: put the cart back
            paymentFailed(out, e.getMessage());
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("unpaidCart", cart);
            paymentFailed(out, "Could not save the sale to the database. Nothing was charged. Please try again.");
            return;
        }

        // 6. The items are paid: remove the other unpaid things from the session
        session.removeAttribute("latestItem");
        session.removeAttribute("sale_date");
        session.removeAttribute("sysmsgclass");
        session.removeAttribute("sysmsgdesc");
        session.setAttribute("lastSaleId", saleId);

        // 7. Receipt (read back from the database so it shows exactly what was saved)
        out.println("<h1>Payment successful</h1>");
        printReceipt(out, saleId);
    }

    // Payment not accepted: nothing is saved, the cart stays in the session
    private void paymentFailed(PrintWriter out, String reason) {
        out.println("<h1>Payment failed</h1>");
        out.println("<p>" + PageHelper.esc(reason) + "</p>");
        out.println("<p>Nothing was saved. The items are still in the cart.</p>");
        out.println("<p><a href='do.processcashier?actionbtn=Done'>Back to payment</a></p>");
    }

    // GET: print the last receipt again (or the one in ?saleid=)
    private void reprint(HttpServletRequest request, HttpSession session, PrintWriter out) {
        Integer saleId = ParseThis.toId(request.getParameter("saleid"), "TX");
        if (saleId == null) {
            saleId = (Integer) session.getAttribute("lastSaleId");
        }
        if (saleId == null) {
            out.println("<h1>No receipt yet</h1>");
            out.println("<p>No sale was made in this session.</p>");
            return;
        }
        out.println("<h1>Receipt (reprint)</h1>");
        printReceipt(out, saleId);
    }

    // Prints the receipt of one saved sale
    private void printReceipt(PrintWriter out, int saleId) {
        Sale sale;
        try {
            sale = SqlcSales.getSale(saleId);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Sale " + String.format("TX-%04d", saleId) + " was saved, but the receipt could not be loaded.</p>");
            return;
        }
        if (sale == null) {
            out.println("<p>Sale " + String.format("TX-%04d", saleId) + " was not found.</p>");
            return;
        }

        out.println("<h2>RECEIPT</h2>");
        out.println("<p>Transaction no: <b>" + sale.getSaleNumber() + "</b></p>");
        out.println("<p>Date: " + PageHelper.dateTime(sale.getSaledate()) + "</p>");
        out.println("<p>Cashier: " + PageHelper.esc(sale.getCashiername()) + " | Customer: " + PageHelper.esc(sale.getCustomername()) + "</p>");

        out.println("<table border='1'>");
        out.println("<tr><th>Code</th><th>Product</th><th>Qty</th><th>Unit price</th><th>Subtotal</th></tr>");
        for (SaleItem item : sale.getItems()) {
            out.println("<tr><td>" + PageHelper.esc(item.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                    + "<td>" + item.getQuantity()
                    + (item.getRefundedquantity() > 0 ? " (" + item.getRefundedquantity() + " refunded)" : "") + "</td>"
                    + "<td>" + PageHelper.money(item.getUnitprice()) + "</td>"
                    + "<td>" + PageHelper.money(item.getSubtotal()) + "</td></tr>");
        }
        out.println("</table>");

        out.println("<p>Sub total: PHP " + PageHelper.money(sale.getSubtotal()) + "</p>");
        out.println("<p>Discount: -PHP " + PageHelper.money(sale.getDiscount()) + "</p>");
        out.println("<p><b>Total: PHP " + PageHelper.money(sale.getTotal()) + "</b></p>");
        out.println("<p>Paid (" + PageHelper.esc(sale.getPaymentmethod()) + "): PHP " + PageHelper.money(sale.getAmountpaid()) + "</p>");
        out.println("<p>Change: PHP " + PageHelper.money(sale.getChange()) + "</p>");
        out.println("<p>Thank you for shopping!</p>");
        out.println("<p><button onclick='window.print()'>Print</button></p>");
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
        return "Cashier payment and receipt";
    }// </editor-fold>

}
