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
import javax.servlet.http.HttpSession;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.UserSession;

/**
 *
 * @author Group 2
 */
// Lists job orders from the database. URL: /do.joborders
//
// EDITED: who sees what comes from the login, not from the URL:
//   customer           -> only their OWN job orders (the session's c_id)
//   technician / admin -> every job order; ?customer=username narrows it to one customer
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
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Job Orders</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Who is asking? (accessfilter only lets technicians, customers and admin in)
            HttpSession session = request.getSession(false);
            boolean isCustomer = "customer".equals(UserSession.role(session));
            Integer myCustomerId = (session == null) ? null : (Integer) session.getAttribute("c_id");
            if (isCustomer && myCustomerId == null) {
                out.println("<p>Please log in again.</p>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Staff may narrow the list to one customer's username (blank = everyone)
            String customer = isCustomer ? null : request.getParameter("customer");
            boolean oneCustomer = (customer != null && !customer.trim().isEmpty());
            if (oneCustomer) {
                customer = customer.trim();
            }

            if (isCustomer) {
                out.println("<h1>My PC build orders</h1>");
            } else if (oneCustomer) {
                out.println("<h1>Job orders of " + iosqlclass.esc(customer) + "</h1>");
            } else {
                out.println("<h1>All job orders</h1>");
            }

            // Description shows the build name. The status shown is the job order's status.
            String sql = "SELECT j.job_id, c.username AS customer, b.build_name AS description, j.status "
                    + "FROM job_orders j "
                    + "JOIN builds b ON j.build_id = b.build_id "
                    + "JOIN customers c ON b.customer_id = c.customer_id "
                    + (isCustomer ? "WHERE c.customer_id = ? " : (oneCustomer ? "WHERE c.username = ? " : ""))
                    + "ORDER BY j.job_id";

            out.println("<table border='1'>");
            out.println("<tr><th>Job</th><th>Customer</th><th>Description</th><th>Status</th></tr>");
            int shown = 0;

            try {
                iosqlclass db = new iosqlclass();
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (isCustomer) {
                        ps.setInt(1, myCustomerId);
                    } else if (oneCustomer) {
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
 
            // links for the page this user came from
            if (isCustomer) {
                out.println("<p><a href='customer.jsp'>Back to Customer</a> | <a href='pcbuilder.jsp'>PC Builder</a></p>");
            } else {
                out.println("<p><a href='technician.jsp'>Back to Technician</a> | <a href='do.updatejob'>Update job orders</a></p>");
            }
            out.println("<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
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
