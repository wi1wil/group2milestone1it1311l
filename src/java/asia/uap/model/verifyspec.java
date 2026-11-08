/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import java.io.IOException;
import java.io.PrintWriter;
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
// AI-powered build / spec checking (form on verifyspec.jsp). URL: /do.verifyspec
//
// UNDER CONSTRUCTION: this page only shows what was sent and a notice.
// Planned: read the product's specs from product_specs, ask an AI service to compare them
// with the manufacturer's specs, and show MATCH / MISMATCH per field.
@WebServlet(name = "verifyspec", urlPatterns = {"/do.verifyspec"})
public class verifyspec extends HttpServlet {

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
            out.println("<title>Spec Verification</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            out.println("<h1>AI Spec Verification</h1>");
            out.println("<div class='notification' style='position:static; width:auto;'>"
                    + "<span><b>Under construction.</b> AI-powered build and spec checking is not available yet.</span></div>");

            // Read the form fields (only shown back for now)
            String product = request.getParameter("product");
            String field = request.getParameter("field");
            String stored = request.getParameter("stored");
            String manufacturer = request.getParameter("manufacturer");

            if (product != null) {
                out.println("<h3>Received</h3>");
                out.println("<p>Product: " + PageHelper.esc(product) + "</p>");
                out.println("<p>Field: " + PageHelper.esc(field) + "</p>");
                out.println("<p>Stored in system: " + PageHelper.esc(stored) + "</p>");
                out.println("<p>Manufacturer spec: " + PageHelper.esc(manufacturer) + "</p>");
            }
            out.println("<p>For now, use the <a href='pcbuilder.jsp'>PC Builder compatibility check</a>.</p>");

            out.println("<p><a href='verifyspec.jsp'>Back</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
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
        return "AI spec verification (under construction)";
    }// </editor-fold>

}
