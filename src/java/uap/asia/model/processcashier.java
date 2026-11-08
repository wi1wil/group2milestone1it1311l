/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package uap.asia.model;

import java.sql.*;
import uap.asia.functionalclasses.*;
import uap.asia.classes.CartItem;
import java.util.ArrayList;
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
@WebServlet(name = "processcashier", urlPatterns = {"/do.processcashier"})
public class processcashier extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    // -------------------------------------------------------------------------------------

    /*
    // Next: validate, look up the product, save it in a CartItem, put it in the cart
    private void addToCart(HttpServletRequest request, HttpSession session, ArrayList<CartItem> cart) {
        String idText = request.getParameter("productid");
        String qtyText = request.getParameter("productquantity");
        String discountText = request.getParameter("discount");

        // 1. Validate the form fields
        if (!ParseThis.isInt(idText) || ParseThis.toInt(idText) <= 0) {
            throw new IllegalArgumentException("Invalid product ID.");
        }
        if (!ParseThis.isInt(qtyText) || ParseThis.toInt(qtyText) <= 0) {
            throw new IllegalArgumentException("Quantity must be a whole number of at least 1.");
        }
        double discount = 0;                       // nothing typed means no discount
        if (discountText != null && !discountText.trim().isEmpty()) {
            if (!ParseThis.isDouble(discountText)) {
                throw new IllegalArgumentException("Discount must be a number from 0 to 100.");
            }
            discount = ParseThis.toDouble(discountText);
        }
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be a number from 0 to 100.");
        }
        int productId = ParseThis.toInt(idText);
        int qty = ParseThis.toInt(qtyText);

        // 2. Look up the product in the database: {product_id, code, name, price, stock}
        String[] p = Sqlc.findProduct(productId);
        if (p == null) {
            throw new IllegalArgumentException("Product ID " + productId + " was not found.");
        }
        double price = Double.parseDouble(p[3]);
        int stock = Integer.parseInt(p[4]);

        // The same product stays on ONE line (SALE_ITEMS allows one row per product per sale)
        int line = -1;
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProductid() == productId) {
                line = i;
            }
        }
        int inCart = (line == -1) ? 0 : cart.get(line).getItemquantity();
        if (inCart + qty > stock) {
            throw new IllegalArgumentException("Only " + stock + " of " + p[2] + " in stock"
                    + (inCart > 0 ? " (" + inCart + " already in the cart)" : "") + ".");
        }

        // 3. Save the details in a CartItem object
        CartItem item = new CartItem(productId, p[1], p[2], price, inCart + qty, discount);

        // 4. Put the object in the ArrayList (replace the old line when the product was already there)
        if (line == -1) {
            cart.add(item);
        } else {
            cart.set(line, item);
        }

        // 5. The ArrayList is already in the session. Save the status for cashier.jsp.
        session.setAttribute("latestItem", item);
        message(session, "status-success", "Added " + qty + " x " + p[2] + " to the cart.");
    }

    // Remove: takes one line out of the cart
    private void removeLine(HttpServletRequest request, HttpSession session, ArrayList<CartItem> cart) {
        String indexText = request.getParameter("index");
        if (!ParseThis.isInt(indexText)) {
            throw new IllegalArgumentException("Invalid line.");
        }
        int index = ParseThis.toInt(indexText);
        if (index < 0 || index >= cart.size()) {
            throw new IllegalArgumentException("That line is not in the cart.");
        }
        CartItem removed = cart.remove(index);
        session.removeAttribute("latestItem");
        message(session, "status-success", "Removed " + removed.getProductcode() + " from the cart.");
    }

    // Done: check the payment, then save the sale (SALES, SALE_ITEMS, stock) in one transaction
    private void finishSale(HttpServletRequest request, HttpSession session, ArrayList<CartItem> cart) throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty. Add an item first.");
        }

        // The employee who makes the sale (saved in the session at login)
        int employeeId = UserSession.employeeId(session);
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Please log in as an employee first.");
        }

        // The customer. Blank = the walk-in account.
        String customerName = request.getParameter("customer");
        boolean walkin = (customerName == null || customerName.trim().isEmpty());
        String lookup = walkin ? WALKIN : customerName.trim();
        String[] customer = Sqlc.selectOne("SELECT customer_id FROM customers WHERE username = ?", lookup);
        if (customer == null) {
            if (walkin) {
                throw new IllegalArgumentException("The walk-in customer account (\"" + WALKIN
                        + "\") is missing. Run sample-data.sql, or type a customer username.");
            }
            throw new IllegalArgumentException("Customer " + lookup + " was not found.");
        }

        // Total to pay
        double total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        total = Math.round(total * 100.0) / 100.0;

        // Payment (nothing typed means exact payment)
        double paid = total;
        String paidText = request.getParameter("paid");
        if (paidText != null && !paidText.trim().isEmpty()) {
            if (!ParseThis.isDouble(paidText)) {
                throw new IllegalArgumentException("Amount paid must be a number.");
            }
            paid = ParseThis.toDouble(paidText);
        }
        if (Math.round(paid * 100) < Math.round(total * 100)) {
            throw new IllegalArgumentException("Payment is short by PHP " + Html.money(total - paid) + ".");
        }

        // Payment method: only the ones in the dropdown
        String method = request.getParameter("method");
        if (method == null || !(method.equals("Cash") || method.equals("Card") || method.equals("GCash"))) {
            method = "Cash";
        }

        // Save the sale. Throws IllegalArgumentException when the stock ran out in the meantime.
        int saleId = Sqlc.saveSale(employeeId, Integer.parseInt(customer[0]), cart, paid, method);

        cart.clear();                               // the next customer starts with an empty cart
        session.removeAttribute("latestItem");
        session.setAttribute("lastSaleId", saleId);
        message(session, "status-success", "Sale #" + saleId + " saved. Total PHP " + Html.money(total)
                + " | Paid PHP " + Html.money(paid) + " | Change PHP " + Html.money(paid - total));
    }
    */
    // ------------------------------------------------------------------------------------------------------
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Builds the whole HTML page; used by both GET and POST
        
        HttpSession session = request.getSession(false);
        
        String action = request.getParameter("actionbtn");
        boolean isValid = true;
        if (session.getAttribute("unpaidCart") == null) {
            isValid = false;
            
            // Debugging: Create a dummy session
            if (action.equals("Dummy")) {
                isValid = true;
            }
        }
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>POS Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Transaction Details</h1>");

            if (isValid) {
                switch(action) {
                    case "Next":
                        if (!(addItem(request, session))) {
                            response.sendRedirect("cashier.jsp");
                            return;
                        }
                        response.sendRedirect("cashier.jsp");
                        break;
                        
                    case "Done":
                        break;
                        
                    case "Delete":
                        if (session.getAttribute("unpaidCart") == null) session.invalidate();
                        response.sendRedirect("cashier.jsp");
                        return;
                    case "Dummy":
                        log("creating dummy session!");
                        session = request.getSession();
                        log("redirecting now!");
                        response.sendRedirect("cashier.jsp?debugmode=thisisandreimartinwilson");
                        return;
                        
                    default:
                        response.sendRedirect("login.jsp");
                        return;
                }               
            }
            /*
            // Cancel button was pressed
            if (request.getParameter("cancelbtn") != null) {
                out.println("<h1>Transaction cancelled</h1>");
                out.println("<p><a href='cashier.jsp'>Back to Cashier</a></p>");
                out.println("</body>");
            out.println("</html>");
                return;
            }
            

            // Stop if the page was opened directly, without the form data
            if (id == null || priceText == null || qtyText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='cashier.jsp'>Go to Cashier</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Convert the text fields to numbers
            double price = 0;
            int qty = 0;
            double discount = 0;
            double paid = -1;
            try {
                price = Double.parseDouble(priceText.trim());
                qty = Integer.parseInt(qtyText.trim());
                if (discountText != null && !discountText.trim().isEmpty()) {
                    discount = Double.parseDouble(discountText.trim());
                }
                if (paidText != null && !paidText.trim().isEmpty()) {
                    paid = Double.parseDouble(paidText.trim());
                }
            } catch (NumberFormatException e) {
                out.println("<h1>Invalid input</h1>");
                out.println("<p>Price, quantity, discount and amount paid must be numbers.</p>");
                out.println("<a href='cashier.jsp'>Back to Cashier</a>");
                out.println("</body>");
            out.println("</html>");
                return;
            }

            // Validate the numbers
            if (price < 0 || qty <= 0 || discount < 0 || discount > 100) {
                out.println("<h1>Invalid input</h1>");
                out.println("<p>Quantity must be at least 1 and discount must be 0 to 100.</p>");
                out.println("<a href='cashier.jsp'>Back to Cashier</a>");
                out.println("</body>");
            out.println("</html>");
                return;
            }

            // Compute subtotal, discount and total
            double subtotal = price * qty;
            double discountAmount = subtotal * discount / 100;
            double total = subtotal - discountAmount;
            if (paid < 0) {
                paid = total;   // nothing typed means exact payment
            }

            // Not enough payment = failed, otherwise print the receipt
            if (paid < total) {
                out.println("<h1>Payment failed</h1>");
                out.println("<p>Total is " + String.format("%.2f", total) + " but only " + String.format("%.2f", paid) + " was paid.</p>");
                out.println("<p>Short by " + String.format("%.2f", total - paid) + "</p>");
            } else {
                out.println("<h1>RECEIPT</h1>");
                out.println("<p>Transaction no: TX-" + (System.currentTimeMillis() % 100000) + "</p>");
                out.println("<p>Date: " + new java.util.Date() + "</p>");
                out.println("<p>Product ID: " + id + "</p>");
                out.println("<p>Price: " + String.format("%.2f", price) + " x Quantity: " + qty + "</p>");
                out.println("<p>Subtotal: " + String.format("%.2f", subtotal) + "</p>");
                out.println("<p>Discount (" + discount + "%): -" + String.format("%.2f", discountAmount) + "</p>");
                out.println("<p><b>Total: " + String.format("%.2f", total) + "</b></p>");
                out.println("<p>Paid: " + String.format("%.2f", paid) + "</p>");
                out.println("<p>Change: " + String.format("%.2f", paid - total) + "</p>");
                out.println("<p>Thank you for shopping!</p>");
            }            
            */

            String cartdetails = "";
            Double totalPrice = getTotalPrice(request, session);
            Double totalDiscount = getTotalDiscount(request, session);
            
            cartdetails += "<p><b>Total: </b>" + totalPrice + "</p>";
            cartdetails += "<p><b>Discount: </b>" + totalDiscount + "</p>";
            cartdetails += "<p><b>Sub total: </b>" + totalPrice  + "</p>";
            
            out.println(cartdetails);
            
            String paymenthtml = "";
            
            paymenthtml += "<form action='do.donecashier' method='POST'>";
            paymenthtml += "<p><label>Payment method: </label></p>";
            paymenthtml += "<select name='mop' class='mopdropdown'>";
            paymenthtml += "<option value='cash'>Cash</option>";
            paymenthtml += "<option value='card'>Card</option>";
            paymenthtml += "<option value='cashless'>Cashless</option>";
            paymenthtml += "<p><label>Amount paid: </label></p>";
            paymenthtml += "<input type='text' name='amount'>";
            paymenthtml += "</form>";            
            
            out.println(paymenthtml);            
            out.println("<p><a href='cashier.jsp'>New transaction</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    
        
            // create java object (productsOnCart) for each item
            
            /*
            ArrayList (UNPAID) of said OBJECT
            OBJECT receives: PRODUCT ID, PRICE, QUANTITY BOUGHT, DISCOUNT
            OBJECT stored in ArrayList (UNPAID)
            RECEIVE payment
            ONCE SECURED, transfer obect from ArrayList (UNPAID) to ArrayList (PAID)
                INCLUDES:
                    decreament stock/quantity
                    add to sales, sales_items
                    
            */
    
    private boolean addItem(HttpServletRequest request, HttpSession session) {       
        
        // validity here
        boolean isValid = true;
        
        // Read the form fields
        String id = request.getParameter("productid");
        String priceText = request.getParameter("productprice");
        String qtyText = request.getParameter("productquantity");
        String discountText = request.getParameter("discount");
        String paidText = request.getParameter("paid");
        
        
        if (id == null || id.isEmpty() &&
                priceText == null || priceText.isEmpty() &&
                qtyText == null || qtyText.isEmpty())
        {
            isValid = false;            
            message(session, "success-notification", "Input error. Try again.");
            return false;            
        }        
                
        Integer runningTotal = (Integer) session.getAttribute("total");        
        Double productPrice = Sqlc.checkPrice(ParseThis.toInt(id));

        // checks if the product id is valid. else
        if (Sqlc.checkProductId(ParseThis.toInt(id))) {            
            CartItem item = new CartItem (ParseThis.toInt(id), productPrice, ParseThis.toInt(qtyText), ParseThis.toDouble(discountText));
            ArrayList<CartItem> list = (ArrayList<CartItem>) getCart(session);
            list.add(item);
            session.setAttribute("unpaidCart", list);
            message(session, "success-notification", "Successfully added " + Sqlc.checkProductName(ParseThis.toInt(id)) + " $" + productPrice);
        }
        else {
            isValid = false;
            message(session, "error-notification", "Product " + id + "not found. Try again.");
            return false;
        }
                
        return true;
    }
    
    private Double getTotalPrice(HttpServletRequest request, HttpSession session) {
           
        Double totalPrice = 0.0;
        
        ArrayList<CartItem> unpaidCart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        
        for (CartItem i: unpaidCart){
            totalPrice += i.getPrice();
        }
        
        return totalPrice;
    }
    
    private Double getTotalDiscount(HttpServletRequest request, HttpSession session) {
           
        Double totalDiscount = 0.0;
        
        ArrayList<CartItem> unpaidCart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        
        for (CartItem i: unpaidCart){
            totalDiscount += i.getItemdiscount();
        }
        
        return totalDiscount;
    }
   
    
    private ArrayList<CartItem> getCart(HttpSession session) {
        ArrayList<CartItem> cart = (ArrayList<CartItem>) session.getAttribute("unpaidCart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("unpaidCart", cart);
        }
        return cart;
    }

    // The notification that cashier.jsp shows once (class = css class of the message)
    private void message(HttpSession session, String cssClass, String text) {
        if (session.getAttribute("unpaidCart") == null) {
            return;
        }
        else {
            log("message function");
            session.setAttribute("sysmsgclass", cssClass);
            session.setAttribute("sysmsgdesc", text);
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
