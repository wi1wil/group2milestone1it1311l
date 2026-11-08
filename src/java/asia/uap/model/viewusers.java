/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import asia.uap.functionalclasses.PageHelper;
 
/**
 *
 * @author Group 2
 */
// Lists user accounts from the database, filtered by role. URL: /do.users
@WebServlet(name = "viewusers", urlPatterns = {"/do.users"})
public class viewusers extends HttpServlet {
 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>User Accounts</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
 
            // Role picked in the dropdown ("all" shows everyone)
            String role = request.getParameter("role");
 
            // Stop if the page was opened directly, without the form data
            if (role == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='users.jsp'>Go to Users</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            role = role.trim();
 
            // The username is stored in employees or customers (linked by u_id), the role name in roles.
            boolean showAll = role.equals("all") || role.isEmpty();
            String sql = "SELECT COALESCE(e.username, c.username) AS username, r.role_name "
                    + "FROM users u "
                    + "JOIN roles r ON u.role = r.role_id "
                    + "LEFT JOIN employees e ON e.u_id = u.u_id "
                    + "LEFT JOIN customers c ON c.u_id = u.u_id "
                    + (showAll ? "" : "WHERE r.role_name = ? ")
                    + "ORDER BY r.role_name, username";
 
            out.println("<h1>User Accounts</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>Username</th><th>Role</th><th>Action</th></tr>");
            int count = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (!showAll) {
                        ps.setString(1, role);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String username = iosqlclass.esc(rs.getString("username"));
                            // Remove button: sends this username to /do.removeuser
                            out.println("<tr><td>" + username + "</td><td>"
                                    + iosqlclass.esc(rs.getString("role_name")) + "</td><td>"
                                    + "<form action='do.removeuser' method='POST'>"
                                    + "<input type='hidden' name='username' value='" + username + "'>"
                                    + "<input type='submit' value='Remove'>"
                                    + "</form></td></tr>");
                            count++;
                        }
                    }
                }
                out.println("</table>");
                out.println("<p>" + count + " user(s) found.</p>");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load users from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='users.jsp'>Back to Users</a> | <a href='admin.jsp'>Admin</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    // GET and POST both run processRequest()
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
 
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
 
}
