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
// Login: checks the username and password (form on login.jsp). URL: /do.authenticate
@WebServlet(name = "authenticateuser", urlPatterns = {"/do.authenticate"})
public class authenticateuser extends HttpServlet {

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
        
        String usninput = request.getParameter("usn");
        String passinput = request.getParameter("pass");
        boolean isLoginValid = true;
        String htmloutput = "";
        
        if (usninput == null || passinput == null || usninput.isEmpty() || passinput.isEmpty()) {
            isLoginValid = false;
        }
        
        /*

        // Check the sample accounts first (username and password are the same)
        String role = "";
        if (usn.equals("admin") && pass.equals("admin")) {
            role = "admin";
        } else if (usn.equals("cashier") && pass.equals("cashier")) {
            role = "cashier";
        } else if (usn.equals("inventory") && pass.equals("inventory")) {
            role = "inventory";
        } else if (usn.equals("technician") && pass.equals("technician")) {
            role = "technician";
        } else if (usn.equals("customer") && pass.equals("customer")) {
            role = "customer";
        } else {
            // accounts made on the registration page
            Object savedPass = getServletContext().getAttribute("pass_" + usn);
            Object savedRole = getServletContext().getAttribute("role_" + usn);
            if (savedPass != null && savedPass.toString().equals(pass)) {
                role = savedRole.toString();
            }
        }

        // Empty role means the login failed
        if (role.equals("")) {
            out.println("<h1>Login failed</h1>");
            out.println("<p>Wrong username or password.</p>");
            out.println("<a href='login.jsp'>Try again</a>");
        } else {
            // Remember the logged-in user and role in the session
            HttpSession session = request.getSession();
            session.setAttribute("user", usn);
            session.setAttribute("role", role);
            out.println("<h1>Login successful</h1>");
            out.println("<p>Welcome, " + usn + "!</p>");
            out.println("<p>Your role: " + role + "</p>");
            out.println("<p><a href='" + role + ".jsp'>Go to my " + role + " page</a></p>");
            out.println("<a href='home.jsp'>Go to Home</a>");
        }
        */
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Login Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<a href='index.jsp' class='indexbtn'>Back to index</a>");
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
