/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.UserSession;
import asia.uap.classes.CartItem;
import asia.uap.classes.Product;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
// Cashier POS screen. URL: /cashier.jsp
//
// The store was meant to use a barcode scanner. Without the hardware, the cashier PICKS the
// product instead: every product is a card (picture, name, price, stock) with a radio button,
// so only ONE product can be chosen at a time. Then the cashier types the quantity and
// clicks "Add to cart". The form goes to processcashier.java (/do.processcashier), which
// checks it again (the page can be out of date if another cashier sold the same item).
//
//   GET ?q=text  -> only show products whose name, code, brand or category contains the text
//
// Cards that cannot be picked (greyed out, radio disabled):
//   - out of stock
//   - every unit is already in this cart
//
// Session attributes read here:
//   name, e_id      = the logged-in cashier (saved at login)
//   unpaidCart      = ArrayList<CartItem> of the items not paid yet
//   sale_date       = when this transaction started (kept until it is paid or cancelled)
//   sysmsgclass/desc= the one-time notification (see PageHelper)
//   lastSaleId      = sale_id of the last paid sale (for the reprint link)
@WebServlet(name = "cashier", urlPatterns = {"/cashier.jsp"})
public class cashier extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");

        // The sale is saved under the logged-in employee, so a login is needed
        HttpSession session = request.getSession(false);
        Integer idnum = UserSession.employeeId(session);
        if (idnum == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        String cashier = UserSession.name(session); // cashier name

        // The cart (empty when nothing was added yet)
        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // When this transaction started: saved once, so it stays the same until it is paid or cancelled
        LocalDateTime transtimestamp = (LocalDateTime) session.getAttribute("sale_date");
        if (transtimestamp == null) {
            transtimestamp = LocalDateTime.now();
            session.setAttribute("sale_date", transtimestamp);
        }

        // Search text (blank = show every product)
        String search = request.getParameter("q");
        if (search == null) {
            search = "";
        }
        search = search.trim();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Cashier POS</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>Cashier POS System</h1>");
            out.println("<h2>Welcome, " + PageHelper.esc(cashier) + "!</h2>");

            // The transaction number is only known after the sale is saved (AUTO_INCREMENT)
            String cashierdetailshtml = "<p class='cashierinfo'>Name: <b>" + PageHelper.esc(cashier)
                    + "</b> | Employee ID: <b>" + idnum
                    + "</b> | Transaction ID: <b>assigned after payment</b>"
                    + " | Started: <b>" + PageHelper.dateTime(transtimestamp) + "</b></p>";
            out.println(cashierdetailshtml);

            // Notification display (shows once, then it is removed from the session)
            out.println(PageHelper.popMessage(session));

            // Search box (GET, so it only filters the cards and changes nothing)
            String searchhtml = "<h3>Pick a product</h3>";
            searchhtml += "<form action='cashier.jsp' method='GET'>";
            searchhtml += "<input type='text' name='q' value='" + PageHelper.esc(search) + "' placeholder='Search name, code, brand or category'> ";
            searchhtml += "<input type='submit' value='Search'>";
            if (!search.isEmpty()) {
                searchhtml += " <a href='cashier.jsp'>Show all</a>";
            }
            searchhtml += "</form>";
            out.println(searchhtml);

            // UI Display: the product cards + quantity, all in ONE form so only one radio can be chosen
            String displayhtml = "<form action='do.processcashier' method='POST'>";
            displayhtml += "<input type='hidden' name='q' value='" + PageHelper.esc(search) + "'>"; // keeps the search after adding
            ArrayList<Product> products = Sqlc.getPosProducts(search);
            displayhtml += productGrid(products, cart, search);
            if (!products.isEmpty()) {
                displayhtml += "<div class='pos-addbar'>";
                displayhtml += "<label>Quantity: </label>";
                displayhtml += "<input type='text' name='productquantity' value='1'> ";
                displayhtml += "<button type='submit' name='actionbtn' value='Next'>Add to cart</button>";
                displayhtml += "</div>";
            }
            displayhtml += "</form>";
            out.println(displayhtml);

            // The cart
            out.println(cartTable(cart));

            // Checkout / cancel (only when there is something in the cart)
            if (!cart.isEmpty()) {
                out.println("<form action='do.processcashier' method='POST'>");
                out.println("<button type='submit' name='actionbtn' value='Done'>Checkout</button> ");
                out.println("<button type='submit' name='actionbtn' value='Delete' class='btn-ghost'"
                        + " onclick=\"return confirm('Cancel this transaction and empty the cart?');\">Cancel transaction</button>");
                out.println("</form>");
            }

            // Links
            String links = "<ul>\n";
            Integer lastSaleId = (Integer) session.getAttribute("lastSaleId");
            if (lastSaleId != null) {
                links += "<li><a href=\"do.finishcashier\">Reprint last receipt (" + String.format("TX-%04d", lastSaleId) + ")</a></li>\n";
            }
            links += "<li><a href=\"add.jsp\">Register a new customer</a> (every customer needs an account)</li>\n"
                    + "<li><a href=\"pcbuilder.jsp\">Saved PC build quotes</a></li>\n"
                    + "<li><a href=\"do.refund\">Returns and refunds</a></li>\n"
                    + "<li><a href=\"transactions.jsp\">Transaction history</a></li>"
                    + "</ul>";
            out.println(links);

            out.println("<a href='index.jsp' class='indexbtn'>Back to menu</a>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * The product cards. Each card is a <label>, so clicking the picture or the name
     * also picks its radio button. All radios share name="productid", so the browser
     * only allows ONE to be chosen. The value is the product_id.
     */
    private String productGrid(ArrayList<Product> products, ArrayList<CartItem> cart, String search) {
        if (products.isEmpty()) {
            return search.isEmpty()
                    ? "<p>No products for sale yet.</p>"
                    : "<p>No products match \"" + PageHelper.esc(search) + "\".</p>";
        }

        // every promo that is on today, read once for the whole grid
        HashMap<Integer, Double> promos = Sqlc.getActivePromos();

        String html = "<div class='pos-grid'>";
        for (Product p : products) {
            // how many can still be added (stock minus what is already in this cart)
            int inCart = 0;
            for (CartItem item : cart) {
                if (item.getProductid().equals(p.getProductid())) {
                    inCart = item.getItemquantity();
                }
            }
            int available = p.getStockquantity() - inCart;
            boolean pickable = p.isSellable() && available > 0;

            // price after the promo: worked out by CartItem, so the card shows exactly what the cart will charge
            double promo = Sqlc.bestPromo(promos, p.getProductid());
            double price = new CartItem(p.getProductid(), p.getPrice(), 1, promo / 100.0).getPrice();

            String stockText;
            if (!p.isSellable()) {
                stockText = "<b>Out of stock</b>";
            } else if (available <= 0) {
                stockText = "<b>All " + p.getStockquantity() + " in cart</b>";
            } else {
                stockText = "Stock: " + available + (inCart > 0 ? " (+" + inCart + " in cart)" : "");
            }

            html += "<label class='pos-card" + (pickable ? "" : " pos-disabled") + "'>";
            html += "<input type='radio' name='productid' value='" + p.getProductid() + "'"
                    + (pickable ? " required" : " disabled") + ">";
            html += PageHelper.productImage(p, null); // EDITED: the product's picture (placeholder when none)
            html += "<span class='pos-name'>" + PageHelper.esc(p.getName()) + "</span>";
            html += "<span class='pos-code'>" + PageHelper.esc(p.getCode()) + " | " + PageHelper.esc(p.getCategory()) + "</span>";
            if (promo > 0) {
                html += "<span class='pos-price'><s>" + PageHelper.money(p.getPrice()) + "</s> <b>PHP "
                        + PageHelper.money(price) + "</b></span>";
                html += "<span class='pos-promo'>" + PageHelper.money(promo) + "% off</span>";
            } else {
                html += "<span class='pos-price'><b>PHP " + PageHelper.money(price) + "</b></span>";
            }
            html += "<span class='pos-stock'>" + stockText + "</span>";
            html += "</label>";
        }
        html += "</div>";
        return html;
    }

    // The cart as an HTML table, with a Remove button on every line
    private String cartTable(ArrayList<CartItem> cart) {
        if (cart.isEmpty()) {
            return "<h3>Cart</h3><p>The cart is empty.</p>";
        }
        String html = "<h3>Cart</h3>";
        html += "<table border='1'>";
        html += "<tr><th>#</th><th>Code</th><th>Product</th><th>Price</th><th>Discount</th>"
                + "<th>Qty</th><th>Subtotal</th><th></th></tr>";
        double total = 0;
        double discount = 0;
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            total += item.getSubtotal();
            discount += item.getTotalDiscount();
            html += "<tr><td>" + (i + 1) + "</td>"
                    + "<td>" + PageHelper.esc(item.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                    + "<td>" + PageHelper.money(item.getOriginalprice()) + "</td>"
                    + "<td>" + (item.getItemdiscount() > 0 ? "-" + PageHelper.money(item.getItemdiscount()) + " each" : "") + "</td>"
                    + "<td>" + item.getItemquantity() + "</td>"
                    + "<td>" + PageHelper.money(item.getSubtotal()) + "</td>"
                    // each Remove button is its own small form that sends the line number
                    + "<td><form action='do.processcashier' method='POST'>"
                    + "<input type='hidden' name='index' value='" + i + "'>"
                    + "<input type='submit' name='actionbtn' value='Remove'>"
                    + "</form></td></tr>";
        }
        html += "</table>";
        html += "<p>Discount: PHP " + PageHelper.money(discount) + "</p>";
        html += "<p><b>Total: PHP " + PageHelper.money(total) + "</b></p>";
        return html;
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
        return "Cashier POS screen";
    }// </editor-fold>

}
