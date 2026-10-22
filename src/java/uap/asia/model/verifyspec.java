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
// Compares a stored spec with the manufacturer's spec (form on verifyspec.jsp). URL: /do.verifyspec
@WebServlet(name = "verifyspec", urlPatterns = {"/do.verifyspec"})
public class verifyspec extends HttpServlet {

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
            out.println("<title>Spec Verification Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String product = request.getParameter("product");
            String field = request.getParameter("field");
            String stored = request.getParameter("stored");
            String manufacturer = request.getParameter("manufacturer");

            // Stop if the page was opened directly, without the form data
            if (product == null || stored == null || manufacturer == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='verifyspec.jsp'>Go to Spec Verification</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            out.println("<h1>Specification Verification</h1>");
            out.println("<p>Product: " + product + "</p>");
            out.println("<p>Field: " + field + "</p>");
            out.println("<p>Stored in system: " + stored + "</p>");
            out.println("<p>Manufacturer spec: " + manufacturer + "</p>");

            // Compare the two specs: missing, match or mismatch
            if (stored.trim().isEmpty() || manufacturer.trim().isEmpty()) {
                out.println("<h2>Result: MISSING INFORMATION</h2>");
                out.println("<p>One of the values is empty.</p>");
            } else if (stored.trim().equalsIgnoreCase(manufacturer.trim())) {
                out.println("<h2>Result: MATCH</h2>");
                out.println("<p>The stored specification agrees with the manufacturer.</p>");
            } else {
                out.println("<h2>Result: MISMATCH</h2>");
                out.println("<p>The stored " + field + " (" + stored + ") is different from the manufacturer's (" + manufacturer + ").</p>");
                out.println("<p>Please correct the product record.</p>");
            }
            out.println("<p><a href='verifyspec.jsp'>Back</a> | <a href='index.jsp'>Index</a></p>");
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
