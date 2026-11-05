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
            username = username.trim();

            // null checks
            HttpSession session = request.getSession(false);
            String me = null;
            if (session != null) {
                me = (String) session.getAttribute("user");
            }

            // problem stays empty when the user is removed
            String problem = "";

            if (username.isEmpty()) { // if no username is given 
                problem = "Username is required.";
            } else if (username.equalsIgnoreCase(me)) { // removing current logged in user
                problem = "You cannot remove the account you are logged in with.";
            } else {
                try { // try removing the account first
                    problem = removeAccount(username);
                } catch (SQLIntegrityConstraintViolationException e) {
                    // The user is used by other tables (sales, builds, promos, purchase orders)
                    problem = username + " has records in the system, so the account cannot be removed.";
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not remove from the database. Please try again.";
                }
            }

            if (problem.isEmpty()) {
                out.println("<h1>User removed</h1>");
                out.println("<p>Username: " + iosqlclass.esc(username) + "</p>");
            } else {
                out.println("<h1>User not removed</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            }
            out.println("<p><a href='users.jsp'>Back to Users</a> | <a href='admin.jsp'>Admin</a> | <a href='index.jsp'>Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Removes the account: the employees or customers row first, then its users row.
    private String removeAccount(String username) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            int uid = -1;
            String table = "employees"; // try selecting from employees tables first
            try (PreparedStatement ps = conn.prepareStatement("SELECT u_id FROM employees WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        uid = rs.getInt("u_id"); // sets the uid else goes down
                    }
                }
            }
            if (uid == -1) { // if not found in employees then  select from customers
                table = "customers";
                try (PreparedStatement ps = conn.prepareStatement("SELECT u_id FROM customers WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            uid = rs.getInt("u_id");
                        }
                    }
                }
            }
            if (uid == -1) {
                return "No account named " + username + " was found.";
            }

            // Remove everything or nothing
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE u_id = ?")) { // deleting customers or employees depending on which one was runned above
                    ps.setInt(1, uid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE u_id = ?")) { // ithen delete from users table
                    ps.setInt(1, uid);
                    ps.executeUpdate();
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
