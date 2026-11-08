/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.UserSession;
import asia.uap.classes.CartItem;
import asia.uap.classes.Product;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.io.IOException;
import java.io.PrintWriter;
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
// Cashier logic: checks the items and manages the cart (buttons on cashier.jsp). URL: /do.processcashier
//
//   actionbtn = Next   -> check the picked product (radio button "productid") and quantity, then add it
//                         to the cart. q = the cashier's search text, kept when going back.
//               Remove -> take one line out of the cart (index = line number)
//               Delete -> cancel the whole transaction (empties the cart, the cashier stays logged in)
//               Done   -> check the cart again, then show the totals and the payment form
//
// Next / Remove / Delete save a message and go back to cashier.jsp (redirect).
// Done prints the checkout page; its form goes to cashierreceipt.java (/do.finishcashier).
//
// Cart rules:
//   - the price comes from the database (the cashier cannot type a price)
//   - the best promo for the product today is applied automatically (promos table)
//   - the same product stays on ONE line (sale_items allows one row per product per sale)
//   - the cart can never hold more than the stock
@WebServlet(name = "processcashier", urlPatterns = {"/do.processcashier"})
public class processcashier extends HttpServlet {

    // Biggest quantity for one line, to catch typing mistakes like 1000000
    private static final int MAX_QTY = 999;

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
        // Builds the checkout page, or changes the cart and goes back to cashier.jsp

        HttpSession session = request.getSession(false);
        if (UserSession.employeeId(session) == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("actionbtn");
        if (action == null) {
            response.sendRedirect("cashier.jsp");
            return;
        }
        // Only the forms (POST) may change the cart. A link (GET) may only open the checkout page.
        if (!"POST".equalsIgnoreCase(request.getMethod()) && !action.equals("Done")) {
            response.sendRedirect("cashier.jsp");
            return;
        }

        ArrayList<CartItem> cart = getCart(session);

        switch (action) {
            case "Next":
                addItem(request, session, cart);
                // go back with the same search, so the cashier still sees the same product cards
                String q = request.getParameter("q");
                if (ParseThis.isBlank(q)) {
                    response.sendRedirect("cashier.jsp");
                } else {
                    response.sendRedirect("cashier.jsp?q=" + URLEncoder.encode(q.trim(), "UTF-8"));
                }
                return;

            case "Remove":
                removeItem(request, session, cart);
                response.sendRedirect("cashier.jsp");
                return;

            case "Delete":
                // Cancel the transaction: only the cart is cleared, NOT the whole session
                // (session.invalidate() would also log the cashier out)
                session.removeAttribute("unpaidCart");
                session.removeAttribute("latestItem");
                session.removeAttribute("sale_date");
                PageHelper.setMessage(session, PageHelper.INFO, "Transaction cancelled. The cart is empty.");
                response.sendRedirect("cashier.jsp");
                return;

            case "Done":
                if (cart.isEmpty()) {
                    PageHelper.setMessage(session, PageHelper.ERROR, "The cart is empty. Add an item first.");
                    response.sendRedirect("cashier.jsp");
                    return;
                }
                // Prices and stock may have changed since the items were added
                String problem = verifyCart(session, cart);
                if (!problem.isEmpty()) {
                    PageHelper.setMessage(session, PageHelper.ERROR, problem);
                    response.sendRedirect("cashier.jsp");
                    return;
                }
                showCheckout(request, response, session, cart);
                return;

            default:
                response.sendRedirect("cashier.jsp");
        }
    }

    // Next: validate, look up the product, save it in a CartItem, put it in the cart.
    // Returns true when the item was added. The result is saved as the notification.
    private boolean addItem(HttpServletRequest request, HttpSession session, ArrayList<CartItem> cart) {

        // Read the form fields
        String id = request.getParameter("productid");
        String qtyText = request.getParameter("productquantity");

        // 1. Validate the form fields
        if (ParseThis.isBlank(id)) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Select a product first.");
            return false;
        }
        int qty = 1; // nothing typed means 1
        if (!ParseThis.isBlank(qtyText)) {
            if (!ParseThis.isInt(qtyText) || ParseThis.toInt(qtyText) <= 0) {
                PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be a whole number of at least 1.");
                return false;
            }
            qty = ParseThis.toInt(qtyText);
        }
        if (qty > MAX_QTY) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Quantity is too big (" + MAX_QTY + " at most).");
            return false;
        }

        // 2. Look up the product in the database (by product_id or by code)
        Product p = Sqlc.findProduct(id);
        if (p == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Product " + id.trim() + " was not found. It may have been removed; pick another.");
            return false;
        }
        if (!Product.STATUS_ACTIVE.equalsIgnoreCase(p.getStatus())) {
            PageHelper.setMessage(session, PageHelper.ERROR, p.getName() + " cannot be sold right now (" + p.getStatus() + ").");
            return false;
        }

        // 3. The same product stays on ONE line: find it if it is already in the cart
        CartItem line = null;
        for (CartItem item : cart) {
            if (item.getProductid().equals(p.getProductid())) {
                line = item;
            }
        }
        int inCart = (line == null) ? 0 : line.getItemquantity();
        if (inCart + qty > p.getStockquantity()) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Only " + p.getStockquantity() + " of " + p.getName() + " in stock"
                    + (inCart > 0 ? " (" + inCart + " already in the cart)" : "") + ".");
            return false;
        }

        // 4. Save it in the cart
        if (line == null) {
            // promo percent (0-100) -> decimal form for CartItem (10% -> 0.10)
            double promo = Sqlc.getPromoPercent(p.getProductid());
            line = new CartItem(p.getProductid(), p.getCode(), p.getName(), p.getPrice(), qty, promo / 100.0);
            cart.add(line);
        } else {
            line.setItemquantity(inCart + qty);
        }
        session.setAttribute("unpaidCart", cart);
        session.setAttribute("latestItem", line);

        String text = "Added " + qty + " x " + p.getName() + " (PHP " + PageHelper.money(line.getPrice()) + " each";
        if (line.getItemdiscount() > 0) {
            text += ", promo -" + PageHelper.money(line.getItemdiscount());
        }
        text += ").";
        PageHelper.setMessage(session, PageHelper.SUCCESS, text);
        return true;
    }

    // Remove: takes one line out of the cart
    private void removeItem(HttpServletRequest request, HttpSession session, ArrayList<CartItem> cart) {
        Integer index = ParseThis.toInt(request.getParameter("index"));
        if (index == null || index < 0 || index >= cart.size()) {
            PageHelper.setMessage(session, PageHelper.ERROR, "That line is not in the cart.");
            return;
        }
        CartItem removed = cart.remove((int) index);
        session.setAttribute("unpaidCart", cart);
        session.removeAttribute("latestItem");
        PageHelper.setMessage(session, PageHelper.SUCCESS, "Removed " + removed.getName() + " from the cart.");
    }

    /**
     * Done: checks every line again against the database before payment.
     *   - product missing, not for sale, or not enough stock -> returns the problem
     *   - price or promo changed -> the line is updated to the new price (noted in the message)
     * Returns "" when everything is OK.
     */
    private String verifyCart(HttpSession session, ArrayList<CartItem> cart) {
        String changed = "";
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            Product p = Sqlc.getProduct(item.getProductid());
            if (p == null) {
                return item.getName() + " no longer exists. Remove it from the cart.";
            }
            if (!Product.STATUS_ACTIVE.equalsIgnoreCase(p.getStatus())) {
                return p.getName() + " can no longer be sold (" + p.getStatus() + "). Remove it from the cart.";
            }
            if (item.getItemquantity() > p.getStockquantity()) {
                return "Only " + p.getStockquantity() + " of " + p.getName() + " left in stock. Remove it and add it again.";
            }
            // rebuild the line with today's price and promo
            double promo = Sqlc.getPromoPercent(p.getProductid());
            CartItem fresh = new CartItem(p.getProductid(), p.getCode(), p.getName(), p.getPrice(), item.getItemquantity(), promo / 100.0);
            if (!fresh.getPrice().equals(item.getPrice())) {
                cart.set(i, fresh);
                changed += p.getName() + " is now PHP " + PageHelper.money(fresh.getPrice()) + ". ";
            }
        }
        session.setAttribute("unpaidCart", cart);
        if (!changed.isEmpty()) {
            PageHelper.setMessage(session, PageHelper.INFO, "Price updated: " + changed);
        }
        return "";
    }

    // Done: prints the totals and the payment form
    private void showCheckout(HttpServletRequest request, HttpServletResponse response, HttpSession session, ArrayList<CartItem> cart) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Checkout</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>Transaction Details</h1>");
            out.println(PageHelper.popMessage(session));

            // The items
            String cartdetails = "<table border='1'>";
            cartdetails += "<tr><th>Code</th><th>Product</th><th>Qty</th><th>Price</th><th>Discount</th><th>Subtotal</th></tr>";
            for (CartItem item : cart) {
                cartdetails += "<tr><td>" + PageHelper.esc(item.getCode()) + "</td>"
                        + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                        + "<td>" + item.getItemquantity() + "</td>"
                        + "<td>" + PageHelper.money(item.getOriginalprice()) + "</td>"
                        + "<td>" + PageHelper.money(item.getTotalDiscount()) + "</td>"
                        + "<td>" + PageHelper.money(item.getSubtotal()) + "</td></tr>";
            }
            cartdetails += "</table>";

            // The totals
            Double totalPrice = getTotalPrice(cart);
            Double totalDiscount = getTotalDiscount(cart);
            cartdetails += "<p><b>Sub total: </b>PHP " + PageHelper.money(totalPrice + totalDiscount) + "</p>";
            cartdetails += "<p><b>Discount: </b>PHP " + PageHelper.money(totalDiscount) + "</p>";
            cartdetails += "<p><b>Total due: </b>PHP " + PageHelper.money(totalPrice) + "</p>";
            out.println(cartdetails);

            // The payment form (cashierreceipt.java checks it)
            String paymenthtml = "";
            paymenthtml += "<h3>Payment</h3>";
            paymenthtml += "<form action='do.finishcashier' method='POST'>";
            paymenthtml += "<p><label>Payment method: </label>";
            paymenthtml += "<select name='mop' class='mopdropdown'>";
            paymenthtml += "<option value='cash'>Cash</option>";
            paymenthtml += "<option value='card'>Card</option>";
            paymenthtml += "<option value='cashless'>Cashless</option>";
            paymenthtml += "</select></p>";
            paymenthtml += "<p><label>Amount paid: </label>";
            paymenthtml += "<input type='text' name='payment'> ";
            paymenthtml += "<small>(required for cash; card and cashless are charged the exact total)</small></p>";
            paymenthtml += "<p><label>Customer username: </label>";
            paymenthtml += "<input type='text' name='customer' required> ";
            paymenthtml += "<small>(required; <a href='add.jsp'>register a new customer</a>)</small></p>";
            paymenthtml += "<p><input type='submit' value='Pay'></p>";
            paymenthtml += "</form>";
            out.println(paymenthtml);

            out.println("<p><a href='cashier.jsp'>Back to the cart</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Amount due: every line's subtotal (already after the discount)
    private Double getTotalPrice(ArrayList<CartItem> cart) {
        double totalPrice = 0.0;
        for (CartItem i : cart) {
            totalPrice += i.getSubtotal();
        }
        return PageHelper.round2(totalPrice);
    }

    // Total discount of every line
    private Double getTotalDiscount(ArrayList<CartItem> cart) {
        double totalDiscount = 0.0;
        for (CartItem i : cart) {
            totalDiscount += i.getTotalDiscount();
        }
        return PageHelper.round2(totalDiscount);
    }

    // The cart in the session (a new empty one is saved when there is none yet)
    private ArrayList<CartItem> getCart(HttpSession session) {
        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("unpaidCart", cart);
        }
        return cart;
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
    // (GET is allowed so the "Back to payment" link of cashierreceipt can reopen the checkout)
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
        return "Cashier cart logic";
    }// </editor-fold>

}
