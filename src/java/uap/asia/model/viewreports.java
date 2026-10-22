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
// Prints the sales summary or the low-stock report. URL: /do.reports
@WebServlet(name = "viewreports", urlPatterns = {"/do.reports"})
public class viewreports extends HttpServlet {

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
            out.println("<title>Report Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Report picked on the page: summary or lowstock
            String type = request.getParameter("type");

            // Stop if the page was opened directly, without the form data
            if (type == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='reports.jsp'>Go to Reports</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Low stock report: items with 5 or fewer in stock
            if (type.equals("lowstock")) {
                out.println("<h1>Low stock items</h1>");
                out.println("<ul>");
                for (int i = 0; i < tempdata.PRODUCTS.length; i++) {
                    String[] p = tempdata.PRODUCTS[i];
                    if (Integer.parseInt(p[4]) <= 5) {
                        out.println("<li>" + p[0] + " - " + p[1] + " (stock: " + p[4] + ")</li>");
                    }
                }
                out.println("</ul>");
            } else {
                // Add up the sales and refunds from the transactions
                double sales = 0;
                double refunds = 0;
                for (int i = 0; i < tempdata.TRANSACTIONS.length; i++) {
                    String[] t = tempdata.TRANSACTIONS[i];
                    double amt = Double.parseDouble(t[5]);
                    if (t[2].equals("SALE")) {
                        sales += amt;
                    } else {
                        refunds += -amt;
                    }
                }
                // Count the completed PC build jobs
                int done = 0;
                for (int i = 0; i < tempdata.JOBS.length; i++) {
                    if (tempdata.JOBS[i][3].equals("Success")) {
                        done++;
                    }
                }

                out.println("<h1>Sales summary</h1>");
                out.println("<table border='1'>");
                out.println("<tr><td>Total transactions</td><td>" + tempdata.TRANSACTIONS.length + "</td></tr>");
                out.println("<tr><td>Total sales (PHP)</td><td>" + String.format("%.2f", sales) + "</td></tr>");
                out.println("<tr><td>Total refunds (PHP)</td><td>" + String.format("%.2f", refunds) + "</td></tr>");
                out.println("<tr><td>Net sales (PHP)</td><td>" + String.format("%.2f", sales - refunds) + "</td></tr>");
                out.println("<tr><td>PC build jobs completed</td><td>" + done + " of " + tempdata.JOBS.length + "</td></tr>");
                out.println("</table>");
            }
            out.println("<p><a href='reports.jsp'>Back to Reports</a> | <a href='transactions.jsp'>Transactions</a> | <a href='index.jsp'>Index</a></p>");
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
