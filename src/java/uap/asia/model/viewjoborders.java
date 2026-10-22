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
// Lists job orders (all, or one customer's). URL: /do.joborders
@WebServlet(name = "viewjoborders", urlPatterns = {"/do.joborders"})
public class viewjoborders extends HttpServlet {

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
            out.println("<title>Job Orders</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // optional filter: customer page sends its username, technician page sends nothing
            // TEMP: replace with the logged-in user from the session later
            String customer = request.getParameter("customer");
            boolean all = (customer == null || customer.trim().equals(""));

            if (all) {
                out.println("<h1>All job orders</h1>");
            } else {
                out.println("<h1>My PC build orders</h1>");
            }
            out.println("<table border='1'>");
            out.println("<tr><th>Job</th><th>Customer</th><th>Description</th><th>Status</th></tr>");
            int shown = 0;
            // Print a row for each matching job
            for (int i = 0; i < tempdata.JOBS.length; i++) {
                String[] j = tempdata.JOBS[i];
                if (all || j[1].equalsIgnoreCase(customer.trim())) {
                    out.println("<tr><td>" + j[0] + "</td><td>" + j[1] + "</td><td>" + j[2] + "</td><td>" + j[3] + "</td></tr>");
                    shown++;
                }
            }
            out.println("</table>");
            if (shown == 0) {
                out.println("<p>No job orders found.</p>");
            }
            out.println("<p><a href='technician.jsp'>Technician</a> | <a href='customer.jsp'>Customer</a> | <a href='index.jsp'>Index</a></p>");
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
