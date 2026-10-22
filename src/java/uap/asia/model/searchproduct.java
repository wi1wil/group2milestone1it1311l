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
// Searches products by text (form on products.jsp). URL: /do.searchproduct
@WebServlet(name = "searchproduct", urlPatterns = {"/do.searchproduct"})
public class searchproduct extends HttpServlet {

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
            out.println("<title>Search Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Temporary product data (same list as tempdata.java), replace with a database later
            String[] ids = {"CPU-001", "CPU-002", "MB-001", "MB-002", "RAM-001", "RAM-002", "GPU-001", "SSD-001", "PSU-001", "CASE-001", "COOL-001"};
            String[] names = {"AMD Ryzen 5 7600", "Intel Core i5-12400F", "ASRock B650M Pro RS", "MSI PRO B660M-A DDR4", "Kingston Fury 16GB DDR5-5600", "Corsair Vengeance 16GB DDR4-3200", "RTX 4060 8GB", "Samsung 980 1TB", "Corsair CV650 650W", "Tecware Forge M2 (mATX)", "Deepcool AK400"};
            String[] categories = {"CPU", "CPU", "Motherboard", "Motherboard", "RAM", "RAM", "GPU", "Storage", "Power Supply", "Case", "CPU Cooler"};
            double[] prices = {12500, 8900, 8200, 6900, 3200, 2300, 19500, 4200, 3800, 2900, 1500};
            int[] stocks = {15, 3, 8, 4, 20, 2, 6, 12, 9, 5, 10};

            // Text typed in the search box
            String q = request.getParameter("q");
            // Stop if the page was opened directly, without the form data
            if (q == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='products.jsp'>Go to Products</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            out.println("<h1>Search results for: " + q + "</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Price (PHP)</th><th>Stock</th></tr>");
            int count = 0;
            for (int i = 0; i < ids.length; i++) {
                // Match the text against ID, name and category (ignores upper/lower case)
                String line = (ids[i] + " " + names[i] + " " + categories[i]).toLowerCase();
                if (line.contains(q.trim().toLowerCase())) {
                    out.println("<tr><td>" + ids[i] + "</td><td>" + names[i] + "</td><td>" + categories[i]
                            + "</td><td>" + prices[i] + "</td><td>" + stocks[i] + "</td></tr>");
                    count++;
                }
            }
            out.println("</table>");
            out.println("<p>" + count + " product(s) found.</p>");
            out.println("<p><a href='products.jsp'>Back to Products</a> | <a href='index.jsp'>Index</a></p>");
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
