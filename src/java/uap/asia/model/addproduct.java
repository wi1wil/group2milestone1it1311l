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
// Adds a new product (form on inventory.jsp). Nothing is saved yet. URL: /do.addproduct
@WebServlet(name = "addproduct", urlPatterns = {"/do.addproduct"})
public class addproduct extends HttpServlet {

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
            out.println("<title>Add Product Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String id = request.getParameter("productid");
            String name = request.getParameter("name");
            String category = request.getParameter("category");
            String priceText = request.getParameter("price");
            String stockText = request.getParameter("stock");

            // Stop if the page was opened directly, without the form data
            if (id == null || name == null || priceText == null || stockText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='inventory.jsp'>Go to Inventory</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Convert price and stock to numbers (-1 means invalid)
            double price = -1;
            int stock = -1;
            try {
                price = Double.parseDouble(priceText.trim());
                stock = Integer.parseInt(stockText.trim());
            } catch (NumberFormatException e) {
                price = -1;
                stock = -1;
            }

            // Validate the input, then show the result
            if (id.trim().isEmpty() || name.trim().isEmpty()) {
                out.println("<h1>Product not added</h1>");
                out.println("<p>Product ID and name are required.</p>");
            } else if (price < 0 || stock < 0) {
                out.println("<h1>Product not added</h1>");
                out.println("<p>Price must be a number and stock must be a whole number (0 or more).</p>");
            } else {
                out.println("<h1>Product added</h1>");
                out.println("<p>ID: " + id + "</p>");
                out.println("<p>Name: " + name + "</p>");
                out.println("<p>Category: " + category + "</p>");
                out.println("<p>Price: PHP " + String.format("%.2f", price) + "</p>");
                out.println("<p>Stock: " + stock + "</p>");
                // Low stock alert is 5 or fewer
                if (stock <= 5) {
                    out.println("<p><b>LOW STOCK ALERT:</b> 5 or fewer items.</p>");
                }
            }
            out.println("<p><a href='inventory.jsp'>Back to Inventory</a> | <a href='index.jsp'>Index</a></p>");
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
