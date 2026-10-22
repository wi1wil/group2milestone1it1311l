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
// Lists transactions, filtered by type and who made them. URL: /do.transactions
@WebServlet(name = "viewtransactions", urlPatterns = {"/do.transactions"})
public class viewtransactions extends HttpServlet {

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
            out.println("<title>Transaction History</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Filters picked on the page
            String type = request.getParameter("type");
            String by = request.getParameter("by");

            // Stop if the page was opened directly, without the form data
            if (type == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='transactions.jsp'>Go to Transactions</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            if (by == null) {
                by = "";
            }
            by = by.trim();

            out.println("<h1>Transaction History</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Date</th><th>Type</th><th>Product</th><th>Qty</th><th>Amount (PHP)</th><th>By</th></tr>");
            int shown = 0;
            for (int i = 0; i < tempdata.TRANSACTIONS.length; i++) {
                String[] t = tempdata.TRANSACTIONS[i];
                // Keep a row only if it matches both the type and the "made by" filter
                boolean typeOk = type.equals("ALL") || t[2].equals(type);
                boolean byOk = by.equals("") || t[6].equalsIgnoreCase(by);
                if (typeOk && byOk) {
                    out.println("<tr><td>" + t[0] + "</td><td>" + t[1] + "</td><td>" + t[2] + "</td><td>" + t[3]
                            + "</td><td>" + t[4] + "</td><td>" + t[5] + "</td><td>" + t[6] + "</td></tr>");
                    shown++;
                }
            }
            out.println("</table>");
            if (shown == 0) {
                out.println("<p>No transactions found.</p>");
            }
            out.println("<p><a href='transactions.jsp'>Back to Transactions</a> | <a href='index.jsp'>Index</a></p>");
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
