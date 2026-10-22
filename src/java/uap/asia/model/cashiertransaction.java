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

/**
 *
 * @author Group 2
 */
// POS sale: computes the total and prints a receipt (form on cashier.jsp). URL: /do.donecashier
@WebServlet(name = "cashiertransaction", urlPatterns = {"/do.donecashier"})
public class cashiertransaction extends HttpServlet {

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
            out.println("<title>POS Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Cancel button was pressed
            if (request.getParameter("cancelbtn") != null) {
                out.println("<h1>Transaction cancelled</h1>");
                out.println("<p><a href='cashier.jsp'>Back to Cashier</a></p>");
                out.println("</body>");
            out.println("</html>");
                return;
            }

            // Read the form fields
            String id = request.getParameter("productid");
            String priceText = request.getParameter("productprice");
            String qtyText = request.getParameter("productquantity");
            String discountText = request.getParameter("discount");
            String paidText = request.getParameter("paid");

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
            out.println("<p><a href='cashier.jsp'>New transaction</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
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
