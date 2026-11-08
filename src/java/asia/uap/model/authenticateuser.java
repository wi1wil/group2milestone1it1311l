/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
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
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Login Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

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
                // EDITED: a deactivated employee comes back as { "", reason }
                if (account != null && account[0].isEmpty()) {
                    problem = account[1];
                    account = null;
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
            out.println("<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Checks the username and password in the employees table, then the customers table.
    // Returns { role, full name, id, "employee" or "customer", username }, or null if it does not match.
    // EDITED: returns { "", reason } when the password is right but the employee is deactivated.
    //
    // EDITED: passwords are stored as SHA-256 hashes (sql/EDITED_hash_passwords.sql).
    // The database hashes what the user typed with SHA2(?, 256) and compares it with the stored hash.
    // The USERNAME is always checked too: checking only the password would let someone in as
    // whichever user happens to have the same password.
    private String[] findAccount(String usn, String pass) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // Staff: admin, cashier, inventory, technician
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT employee_id, role_name, username, first_name, last_name, status "
                    + "FROM employees WHERE username = ? AND password = SHA2(?, 256)")) {
                ps.setString(1, usn);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    // a row only comes back when the username AND the hashed password match
                    if (rs.next()) {
                        // only active staff may log in (status is "active" or "INACTIVE")
                        if (!"active".equalsIgnoreCase(rs.getString("status"))) {
                            return new String[]{"", "This account is deactivated. Please ask the admin."};
                        }
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
                    "SELECT customer_id, username, first_name, last_name "
                    + "FROM customers WHERE username = ? AND password = SHA2(?, 256)")) {
                ps.setString(1, usn);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
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
