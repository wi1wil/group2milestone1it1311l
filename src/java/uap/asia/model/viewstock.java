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
// Lists stock levels with low-stock alerts. URL: /do.stock
@WebServlet(name = "viewstock", urlPatterns = {"/do.stock"})
public class viewstock extends HttpServlet {

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
            out.println("<title>Stock Levels</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // "all" shows every product, "low" shows only low stock
            String filter = request.getParameter("filter");

            // Stop if the page was opened directly, without the form data
            if (filter == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='stock.jsp'>Go to Stock</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            out.println("<h1>Stock levels</h1>");
            out.println("<p>Low stock alert at 5 or fewer.</p>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Alert</th></tr>");
            int count = 0;
            for (int i = 0; i < tempdata.PRODUCTS.length; i++) {
                String[] p = tempdata.PRODUCTS[i];
                // Low stock means 5 or fewer
                boolean low = Integer.parseInt(p[4]) <= 5;
                if (filter.equals("all") || low) {
                    out.println("<tr><td>" + p[0] + "</td><td>" + p[1] + "</td><td>" + p[2] + "</td><td>" + p[3]
                            + "</td><td>" + p[4] + "</td><td>" + (low ? "LOW STOCK" : "") + "</td></tr>");
                    count++;
                }
            }
            out.println("</table>");
            out.println("<p>" + count + " product(s) shown.</p>");
            out.println("<p><a href='stock.jsp'>Back to Stock</a> | <a href='inventory.jsp'>Inventory</a> | <a href='index.jsp'>Index</a></p>");
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
