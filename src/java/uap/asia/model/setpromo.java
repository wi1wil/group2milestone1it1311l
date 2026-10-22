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
// Admin creates a promo/discount (form on admin.jsp). URL: /do.setpromo
@WebServlet(name = "setpromo", urlPatterns = {"/do.setpromo"})
public class setpromo extends HttpServlet {

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
            out.println("<title>Promo Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Temporary product data (same list as tempdata.java), replace with a database later
            String[] ids = {"CPU-001", "CPU-002", "MB-001", "MB-002", "RAM-001", "RAM-002", "GPU-001", "SSD-001", "PSU-001", "CASE-001", "COOL-001"};
            String[] names = {"AMD Ryzen 5 7600", "Intel Core i5-12400F", "ASRock B650M Pro RS", "MSI PRO B660M-A DDR4", "Kingston Fury 16GB DDR5-5600", "Corsair Vengeance 16GB DDR4-3200", "RTX 4060 8GB", "Samsung 980 1TB", "Corsair CV650 650W", "Tecware Forge M2 (mATX)", "Deepcool AK400"};
            String[] categories = {"CPU", "CPU", "Motherboard", "Motherboard", "RAM", "RAM", "GPU", "Storage", "Power Supply", "Case", "CPU Cooler"};
            double[] prices = {12500, 8900, 8200, 6900, 3200, 2300, 19500, 4200, 3800, 2900, 1500};
            int[] stocks = {15, 3, 8, 4, 20, 2, 6, 12, 9, 5, 10};

            // Read the form fields
            String promoName = request.getParameter("promoname");
            String percentText = request.getParameter("percent");
            String id = request.getParameter("target");

            // Stop if the page was opened directly, without the form data
            if (promoName == null || percentText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='admin.jsp'>Go to Admin</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            if (id == null) {
                id = "";
            }

            // Convert the percent to a number (0 if invalid)
            double percent = 0;
            try {
                percent = Double.parseDouble(percentText.trim());
            } catch (NumberFormatException e) {
                percent = 0;
            }

            // Find the product ID in the list (-1 means not found)
            int found = -1;
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equalsIgnoreCase(id.trim())) {
                    found = i;
                }
            }

            // Validate, then show the promo (not saved yet)
            if (promoName.trim().isEmpty() || percent <= 0 || percent > 100) {
                out.println("<h1>Promo not created</h1>");
                out.println("<p>Give the promo a name and a percent from 1 to 100.</p>");
            } else if (!id.trim().isEmpty() && found == -1) {
                out.println("<h1>Promo not created</h1>");
                out.println("<p>Product ID " + id + " was not found.</p>");
            } else {
                out.println("<h1>Promo created</h1>");
                out.println("<p>Promo: " + promoName + "</p>");
                out.println("<p>Discount: " + percent + "% off</p>");
                // Blank product ID means the promo applies to all products
                if (id.trim().isEmpty()) {
                    out.println("<p>Applies to: ALL products</p>");
                } else {
                    double promoPrice = prices[found] - (prices[found] * percent / 100);
                    out.println("<p>Applies to: " + names[found] + "</p>");
                    out.println("<p>Regular price: PHP " + String.format("%.2f", prices[found]) + "</p>");
                    out.println("<p>Promo price: PHP " + String.format("%.2f", promoPrice) + "</p>");
                }
            }
            out.println("<p><a href='admin.jsp'>Back to Admin</a> | <a href='index.jsp'>Index</a></p>");
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
