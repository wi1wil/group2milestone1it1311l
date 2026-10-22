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
// Registers a new account (form on add.jsp). URL: /do.register
@WebServlet(name = "registeruser", urlPatterns = {"/do.register"})
public class registeruser extends HttpServlet {

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
            out.println("<title>Register Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String role = request.getParameter("role");
            String idx = request.getParameter("idx");

            // Stop if the page was opened directly, without the form data
            if (username == null || password == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='add.jsp'>Go to Registration</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            if (role == null) {
                role = "customer";
            }
            if (idx == null) {
                idx = "none";
            }

            // The sample usernames cannot be registered again
            boolean reserved = username.equals("admin") || username.equals("cashier") || username.equals("inventory")
                    || username.equals("technician") || username.equals("customer");

            // Validate, then save the account in the application scope
            if (username.trim().isEmpty() || password.isEmpty()) {
                out.println("<h1>Registration failed</h1>");
                out.println("<p>Username and password are required.</p>");
            } else if (reserved || getServletContext().getAttribute("pass_" + username) != null) {
                out.println("<h1>Registration failed</h1>");
                out.println("<p>Username " + username + " is already taken.</p>");
            } else {
                // remember the account while the server is running
                getServletContext().setAttribute("pass_" + username, password);
                getServletContext().setAttribute("role_" + username, role);
                out.println("<h1>Registration successful</h1>");
                out.println("<p>Username: " + username + "</p>");
                out.println("<p>Role: " + role + "</p>");
                out.println("<p>Avatar chosen: " + idx + "</p>");
                out.println("<a href='login.jsp'>Go to Login</a>");
            }
            out.println("<p><a href='add.jsp'>Back to Registration</a> | <a href='index.jsp'>Index</a></p>");
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
