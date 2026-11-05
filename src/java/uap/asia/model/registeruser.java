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
            String firstName = request.getParameter("firstname");
            String lastName = request.getParameter("lastname");
            String contact = request.getParameter("contact");
            String address = request.getParameter("address");

            // Stop if the page was opened directly, without the form data
            if (username == null || password == null || firstName == null || lastName == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='add.jsp'>Go to Registration</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            // Public registration always makes a customer.
            // Only a logged-in admin (from addemployee.jsp) may choose another role.
            HttpSession session = request.getSession(false);
            String me = null;
            if (session != null) {
                me = (String) session.getAttribute("role");
            }
            boolean isAdmin = "admin".equals(me);
            if (role == null || role.trim().isEmpty() || !isAdmin) {
                // Admin left the role blank: staff default to cashier; everyone else is a customer
                role = isAdmin && "employee".equals(request.getParameter("kind")) ? "cashier" : "customer";
            }
            role = role.trim();
            // Contact number and address are optional
            if (contact == null) {
                contact = "";
            }
            if (address == null) {
                address = "";
            }
            username = username.trim();
            firstName = firstName.trim();
            lastName = lastName.trim();
            contact = contact.trim();
            address = address.trim();

            // problem stays empty when everything works
            String problem = "";

            // Validate the input first
            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                problem = "Username, password, first name and last name are required.";
            } else if (username.length() > 50 || password.length() > 50 || firstName.length() > 50 || lastName.length() > 50) {
                problem = "Username, password and names can be 50 characters at most.";
            } else if (contact.length() > 20 || address.length() > 100) {
                problem = "Contact number can be 20 characters and address 100 characters at most.";
            } else {
                // Save the account in the database
                try {
                    problem = saveAccount(username, password, role, firstName, lastName, contact, address);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
            }

            if (problem.isEmpty()) {
                out.println("<h1>Registration successful</h1>");
                out.println("<p>Username: " + iosqlclass.esc(username) + "</p>");
                out.println("<p>Role: " + iosqlclass.esc(role) + "</p>");
                out.println("<a href='login.jsp'>Go to Login</a>");
            } else {
                out.println("<h1>Registration failed</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            }
            if (isAdmin) {
                out.println("<p><a href='addemployee.jsp'>Back to Add Employee</a> | <a href='users.jsp'>Users</a> | <a href='index.jsp'>Index</a></p>");
            } else {
                out.println("<p><a href='add.jsp'>Back to Registration</a> | <a href='index.jsp'>Index</a></p>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the account: a users row, then an employees row (staff) or a customers row.
    // Returns "" when saved, otherwise the reason it was not saved.
    private String saveAccount(String username, String password, String role, String firstName,
            String lastName, String contact, String address) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // The username must not be used by any employee or customer
            boolean taken = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT username FROM employees WHERE username = ? "
                    + "UNION SELECT username FROM customers WHERE username = ?")) {
                ps.setString(1, username);
                ps.setString(2, username);
                try (ResultSet rs = ps.executeQuery()) {
                    taken = rs.next();
                }
            }
            if (taken) {
                return "Username " + username + " is already taken.";
            }

            // Find the role id (admin, cashier, inventory, technician, customer)
            int roleId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT role_id FROM roles WHERE role_name = ?")) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("role_id");
                    }
                }
            }
            if (roleId == -1) {
                return "Unknown role: " + role;
            }

            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                // Step 1: the users row (the database gives it a new u_id)
                int uid = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (role) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, roleId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            uid = keys.getInt(1);
                        }
                    }
                }
                if (uid == -1) {
                    throw new SQLException("No u_id was returned for the new user.");
                }

                // Step 2: the customers row or the employees row
                if (role.equals("customer")) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO customers (u_id, username, password, first_name, last_name, contact_num, address) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, uid);
                        ps.setString(2, username);
                        ps.setString(3, password);
                        ps.setString(4, firstName);
                        ps.setString(5, lastName);
                        // Blank optional fields are saved as NULL
                        ps.setString(6, contact.isEmpty() ? null : contact);
                        ps.setString(7, address.isEmpty() ? null : address);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO employees (u_id, role_name, username, password, first_name, last_name, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, 'active')")) {
                        ps.setInt(1, uid);
                        ps.setString(2, role);
                        ps.setString(3, username);
                        ps.setString(4, password);
                        ps.setString(5, firstName);
                        ps.setString(6, lastName);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return "";
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
    