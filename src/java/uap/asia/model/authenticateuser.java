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
import java.sql.*;

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
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Login Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String usn = request.getParameter("usn");
            String pass = request.getParameter("pass");

            // Stop if the page was opened directly, without the form data
            if (usn == null || pass == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='login.jsp'>Go to Login</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            usn = usn.trim();

            // account stays null when the login fails
            String[] account = null;
            String problem = "";

            if (usn.isEmpty() || pass.isEmpty()) { // null checks
                problem = "Username and password are required.";
            } else {
                // Look for the account in the database
                try {
                    account = findAccount(usn, pass);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not reach the database. Please try again.";
                }
                if (account == null && problem.isEmpty()) {
                    problem = "Wrong username or password.";
                }
            }

            if (account == null) {
                out.println("<h1>Login failed</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
                out.println("<a href='login.jsp'>Try again</a>");
            } else {
                // account = { role, full name, id, "employee" or "customer", username }
                String role = account[0];

                // Start a fresh session for the logged-in user
                HttpSession old = request.getSession(false);
                if (old != null) {
                    old.invalidate();
                }
                
                HttpSession session = request.getSession();
                session.setAttribute("user", account[4]);
                session.setAttribute("role", role);
                session.setAttribute("name", account[1]);
                
                if (account[3].equals("employee")) {
                    // the other pages (cashier, promos, purchase orders) use e_id
                    session.setAttribute("e_id", Integer.valueOf(account[2]));
                } else {
                    session.setAttribute("c_id", Integer.valueOf(account[2]));
                }

                out.println("<h1>Login successful</h1>");
                out.println("<p>Welcome, " + iosqlclass.esc(account[1]) + "!</p>");
                out.println("<p>Your role: " + iosqlclass.esc(role) + "</p>");
                out.println("<p><a href='" + role + ".jsp'>Go to my " + iosqlclass.esc(role) + " page</a></p>");
                out.println("<a href='home.jsp'>Go to Home</a>");
            }
            out.println("<p><a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Checks the username and password in the employees table, then the customers table.
    // Returns { role, full name, id, "employee" or "customer", username }, or null if it does not match.
    private String[] findAccount(String usn, String pass) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // Staff: admin, cashier, inventory, technician
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT employee_id, role_name, username, password, first_name, last_name "
                    + "FROM employees WHERE username = ?")) {
                ps.setString(1, usn);
                try (ResultSet rs = ps.executeQuery()) {
                    // equals() checks the password with the exact upper and lower case
                    if (rs.next() && rs.getString("password").equals(pass)) {
                        return new String[]{
                            rs.getString("role_name"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            String.valueOf(rs.getInt("employee_id")),
                            "employee",
                            rs.getString("username")};
                    }
                }
            }

            // Customers
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT customer_id, username, password, first_name, last_name "
                    + "FROM customers WHERE username = ?")) {
                ps.setString(1, usn);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString("password").equals(pass)) {
                        return new String[]{
                            "customer",
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            String.valueOf(rs.getInt("customer_id")),
                            "customer",
                            rs.getString("username")};
                    }
                }
            }
        }
        return null;
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
