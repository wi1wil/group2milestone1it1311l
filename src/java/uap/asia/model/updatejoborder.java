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
// Technician updates a job order status (form on technician.jsp). URL: /do.updatejob
@WebServlet(name = "updatejoborder", urlPatterns = {"/do.updatejob"})
public class updatejoborder extends HttpServlet {

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
            out.println("<title>Job Update Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the form fields
            String jobId = request.getParameter("jobid");
            String status = request.getParameter("status");

            // Stop if the page was opened directly, without the form data
            if (jobId == null || status == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='technician.jsp'>Go to Job Orders</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // Pick a message for the chosen status (not saved yet)
            String message = "";
            if (status.equals("Pending")) {
                message = "The job is waiting for a technician.";
            } else if (status.equals("In Progress")) {
                message = "A technician is building the PC now.";
            } else if (status.equals("Success")) {
                message = "The build is finished. The customer can pick up the PC.";
            } else {
                message = "Unknown status.";
            }

            out.println("<h1>Job order updated</h1>");
            out.println("<p>Job: " + jobId + "</p>");
            out.println("<p>New status: <b>" + status + "</b></p>");
            out.println("<p>" + message + "</p>");
            out.println("<p><a href='technician.jsp'>Back to Job Orders</a> | <a href='index.jsp'>Index</a></p>");
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
