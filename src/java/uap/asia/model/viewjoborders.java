/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package uap.asia.model;
 
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
 
/**
 *
 * @author Group 2
 */
// Lists job orders from the database (all, or one customer's). URL: /do.joborders
@WebServlet(name = "viewjoborders", urlPatterns = {"/do.joborders"})
public class viewjoborders extends HttpServlet {
 
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
            if (!all) {
                customer = customer.trim();
            }
 
            if (all) {
                out.println("<h1>All job orders</h1>");
            } else {
                out.println("<h1>My PC build orders</h1>");
            }
 
            // ASSUMPTION: the customers table is called "customers" with the columns customer_id and username.
            // If yours are named differently, change them in the two places marked below.
            // Description shows the build name. The status shown is the job order's status.
            String sql = "SELECT j.job_id, c.username AS customer, b.build_name AS description, j.status "   //username
                    + "FROM job_orders j "
                    + "JOIN builds b ON j.build_id = b.build_id "
                    + "JOIN customers c ON b.customer_id = c.customer_id "                                    //customers / customer_id
                    + (all ? "" : "WHERE c.username = ? ")                                                    //username
                    + "ORDER BY j.job_id";
 
            out.println("<table border='1'>");
            out.println("<tr><th>Job</th><th>Customer</th><th>Description</th><th>Status</th></tr>");
            int shown = 0;
 
            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (!all) {
                        ps.setString(1, customer);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        // Print a row for each matching job (same JOB-001 format the technician page uses)
                        while (rs.next()) {
                            out.println("<tr><td>" + String.format("JOB-%03d", rs.getInt("job_id"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("customer"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("description"))
                                    + "</td><td>" + iosqlclass.esc(rs.getString("status"))
                                    + "</td></tr>");
                            shown++;
                        }
                    }
                }
                out.println("</table>");
                if (shown == 0) {
                    out.println("<p>No job orders found.</p>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("</table>");
                out.println("<p>Could not load job orders from the database. Please try again.</p>");
            }
 
            out.println("<p><a href='technician.jsp'>Technician</a> | <a href='customer.jsp'>Customer</a> | <a href='index.jsp'>Index</a></p>");
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
