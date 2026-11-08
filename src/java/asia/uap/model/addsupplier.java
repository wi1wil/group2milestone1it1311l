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
// Adds a supplier (form on suppliers.jsp). Nothing is saved yet. URL: /do.addsupplier
@WebServlet(name = "addsupplier", urlPatterns = {"/do.addsupplier"})
public class addsupplier extends HttpServlet {

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
            out.println("<title>Add Supplier Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Read the form fields
            String name = request.getParameter("name");
            String contact = request.getParameter("contact");

            // Stop if the page was opened directly, without the form data
            if (name == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='suppliers.jsp'>Go to Suppliers</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            name = name.trim();
            // Contact is optional
            if (contact == null || contact.trim().isEmpty()) {
                contact = "no contact given";
            }
            contact = contact.trim();

            // problem stays empty when the supplier is saved
            String problem = "";

            // Name is required
            if (name.isEmpty()) {
                problem = "Supplier name is required.";
            } else if (name.length() > 100 || contact.length() > 20) {
                problem = "Supplier name can be 100 characters and contact 20 characters at most.";
            } else {
                try {
                    problem = saveSupplier(name, contact);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
            }

            if (problem.isEmpty()) {
                out.println("<h1>Supplier added</h1>");
                out.println("<p>Name: " + iosqlclass.esc(name) + "</p>");
                out.println("<p>Contact: " + iosqlclass.esc(contact) + "</p>");
            } else {
                out.println("<h1>Supplier not added</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            }
            out.println("<p><a href='suppliers.jsp'>Back to Suppliers</a> | <a href='inventory.jsp'>Inventory</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the supplier in the suppliers table. Returns "" when saved, otherwise the reason it was not saved.
    private String saveSupplier(String name, String contact) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // The supplier name must be new
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT supplier_id FROM suppliers WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (exists) {
                return "Supplier " + name + " already exists.";
            }

            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO suppliers (name, contact) VALUES (?, ?)")) {
                ps.setString(1, name);
                ps.setString(2, contact);
                ps.executeUpdate();
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
