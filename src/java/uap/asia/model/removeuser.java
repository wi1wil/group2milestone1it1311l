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
// Removes a user account (form on users.jsp). URL: /do.removeuser
@WebServlet(name = "removeuser", urlPatterns = {"/do.removeuser"})
public class removeuser extends HttpServlet {

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
            out.println("<title>Remove User Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form field
            String username = request.getParameter("username");

            // Stop if the page was opened directly, without the form data
            if (username == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='users.jsp'>Go to Users</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // The sample usernames are fixed and cannot be removed
            boolean sample = username.equals("admin") || username.equals("cashier") || username.equals("inventory")
                    || username.equals("technician") || username.equals("customer");

            if (username.trim().isEmpty()) {
                out.println("<h1>User not removed</h1>");
                out.println("<p>Username is required.</p>");
            } else if (sample) {
                out.println("<h1>User not removed</h1>");
                out.println("<p>" + username + " is a sample account and cannot be removed.</p>");
            } else if (getServletContext().getAttribute("pass_" + username) == null) {
                out.println("<h1>User not removed</h1>");
                out.println("<p>No account named " + username + " was found.</p>");
            } else {
                // forget the account that registeruser saved in the application scope
                getServletContext().removeAttribute("pass_" + username);
                getServletContext().removeAttribute("role_" + username);
                out.println("<h1>User removed</h1>");
                out.println("<p>Username: " + username + "</p>");
            }
            out.println("<p><a href='users.jsp'>Back to Users</a> | <a href='admin.jsp'>Admin</a> | <a href='index.jsp'>Index</a></p>");
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
