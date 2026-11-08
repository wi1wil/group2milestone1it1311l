/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.SqlcJobs;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.UserSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import asia.uap.classes.BuildItem;
import asia.uap.classes.JobOrder;
import asia.uap.classes.Product;

/**
 *
 * @author Group 2
 */
// Technician edits a job order: 1. change the parts of the build, 2. change the status. URL: /do.updatejob
//
// GET                  -> list of every job order (with an Open link)
// GET ?jobid=N         -> one job: customer, build, parts, status form, add part form
// POST jobid=N, action = status     (status)               -> change the job status
//                        additem    (productid, quantity)  -> add a part (adds to the line if already there)
//                        updateqty  (builditemid, quantity)-> change a part's quantity (0 removes it)
//                        removeitem (builditemid)          -> remove a part
// Every POST saves a message and goes back to the job page (redirect).
// The old technician.jsp form (jobid=JOB-001, status=...) still works: it is treated as action=status.
//
// Rules (checked again in SqlcJobs inside a database transaction):
//   - parts can only be changed while the job is Pending or In Progress
//   - Pending -> In Progress / Cancelled, In Progress -> Pending / Success / Cancelled
//   - In Progress assigns the job to the technician who started it
//   - Success takes the parts added by a technician out of stock (needs enough stock for them).
//     Parts paid with a PC builder order cannot be changed here.
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
        HttpSession session = request.getSession(false);
        Integer employeeId = UserSession.employeeId(session);
        if (employeeId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // jobid=5, or JO-005 / JOB-005 from the old form
        Integer jobId = readJobId(request.getParameter("jobid"));

        // POST: change the job, then come back to its page
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (jobId == null) {
                PageHelper.setMessage(session, PageHelper.ERROR, "No job order was selected.");
                response.sendRedirect("do.updatejob");
                return;
            }
            handleAction(request, session, jobId, employeeId);
            response.sendRedirect("do.updatejob?jobid=" + jobId);
            return;
        }

        // GET: the list, or one job
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
            out.println(PageHelper.popMessage(session));

            try {
                if (jobId == null) {
                    printJobList(out);
                } else {
                    JobOrder job = SqlcJobs.getJobOrder(jobId);
                    if (job == null) {
                        out.println("<h1>Job order not found</h1>");
                        out.println("<p>There is no job order " + String.format("JO-%03d", jobId) + ".</p>");
                    } else {
                        printJob(out, job);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("<p>Could not load job orders from the database. Please try again.</p>");
            }

            out.println("<p><a href='do.updatejob'>All job orders</a> | <a href='technician.jsp'>Technician</a></p>"
                    + "<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Reads the job ID with or without its prefix (5, JO-005, JOB-005)
    private Integer readJobId(String text) {
        if (text == null) {
            return null;
        }
        String value = text.trim().toUpperCase();
        if (value.startsWith("JOB")) {
            return ParseThis.toId(value, "JOB");
        }
        return ParseThis.toId(value, "JO");
    }

    // POST actions
    private void handleAction(HttpServletRequest request, HttpSession session, int jobId, int employeeId) {
        String action = request.getParameter("action");
        if (action == null && request.getParameter("status") != null) {
            action = "status"; // old technician.jsp form
        }
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "status": {
                    String status = request.getParameter("status");
                    SqlcJobs.updateJobStatus(jobId, status, employeeId);
                    String text = "Status changed to " + status + ".";
                    if (JobOrder.SUCCESS.equals(status)) {
                        text += " The parts were taken out of stock. The customer can pick up the PC.";
                    }
                    PageHelper.setMessage(session, PageHelper.SUCCESS, text);
                    break;
                }
                case "additem": {
                    Product p = Sqlc.findProduct(request.getParameter("productid"));
                    if (p == null) {
                        PageHelper.setMessage(session, PageHelper.ERROR, "Product not found.");
                        return;
                    }
                    Integer qty = readQty(request.getParameter("quantity"), 1);
                    if (qty == null || qty <= 0) {
                        PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be a whole number of at least 1.");
                        return;
                    }
                    SqlcJobs.addBuildItem(jobId, p.getProductid(), qty);
                    PageHelper.setMessage(session, PageHelper.SUCCESS, "Added " + qty + " x " + p.getName() + " to the build.");
                    break;
                }
                case "updateqty": {
                    Integer itemId = ParseThis.toInt(request.getParameter("builditemid"));
                    Integer qty = readQty(request.getParameter("quantity"), null);
                    if (itemId == null || qty == null || qty < 0) {
                        PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be a whole number (0 removes the part).");
                        return;
                    }
                    SqlcJobs.updateBuildItemQty(jobId, itemId, qty);
                    PageHelper.setMessage(session, PageHelper.SUCCESS, (qty == 0) ? "Part removed." : "Quantity changed to " + qty + ".");
                    break;
                }
                case "removeitem": {
                    Integer itemId = ParseThis.toInt(request.getParameter("builditemid"));
                    if (itemId == null) {
                        PageHelper.setMessage(session, PageHelper.ERROR, "No part was selected.");
                        return;
                    }
                    SqlcJobs.removeBuildItem(jobId, itemId);
                    PageHelper.setMessage(session, PageHelper.SUCCESS, "Part removed.");
                    break;
                }
                default:
                    PageHelper.setMessage(session, PageHelper.ERROR, "Unknown action.");
            }
        } catch (IllegalArgumentException e) {
            PageHelper.setMessage(session, PageHelper.ERROR, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not update the database. Nothing was changed.");
        }
    }

    // Reads a quantity; blank gives blankValue. Returns null when it is not a whole number.
    private Integer readQty(String text, Integer blankValue) {
        if (ParseThis.isBlank(text)) {
            return blankValue;
        }
        Integer qty = ParseThis.toInt(text);
        if (qty != null && qty > 999) {
            return null;
        }
        return qty;
    }

    // Every job order in a table
    private void printJobList(PrintWriter out) throws SQLException {
        out.println("<h1>All job orders</h1>");
        ArrayList<JobOrder> jobs = SqlcJobs.getJobOrders();
        if (jobs.isEmpty()) {
            out.println("<p>No job orders found.</p>");
            return;
        }
        out.println("<table border='1'>");
        out.println("<tr><th>Job</th><th>Customer</th><th>Build</th><th>Technician</th><th>Status</th><th>Created</th><th></th></tr>");
        for (JobOrder j : jobs) {
            out.println("<tr><td>" + j.getJobNumber() + "</td>"
                    + "<td>" + PageHelper.esc(j.getCustomername()) + "</td>"
                    + "<td>" + PageHelper.esc(j.getBuildname()) + "</td>"
                    + "<td>" + PageHelper.esc(j.getTechnicianname()) + "</td>"
                    + "<td>" + PageHelper.esc(j.getStatus()) + "</td>"
                    + "<td>" + PageHelper.dateTime(j.getCreatedat()) + "</td>"
                    + "<td><a href='do.updatejob?jobid=" + j.getJobid() + "'>Open</a></td></tr>");
        }
        out.println("</table>");
    }

    // One job: details, parts, and the forms
    private void printJob(PrintWriter out, JobOrder job) {
        out.println("<h1>Job order " + job.getJobNumber() + "</h1>");
        out.println("<p>Customer: <b>" + PageHelper.esc(job.getCustomername()) + "</b> | Build: <b>"
                + PageHelper.esc(job.getBuildname()) + "</b> (build #" + job.getBuildid() + ", "
                + PageHelper.esc(job.getBuildstatus()) + ")</p>");
        out.println("<p>Technician: " + PageHelper.esc(job.getTechnicianname()) + " | Status: <b>"
                + PageHelper.esc(job.getStatus()) + "</b> | Created: " + PageHelper.dateTime(job.getCreatedat())
                + (job.getCompletedat() != null ? " | Completed: " + PageHelper.dateTime(job.getCompletedat()) : "") + "</p>");

        // 1. Status
        out.println("<h3>Status</h3>");
        String options = "";
        for (String s : JobOrder.STATUSES) {
            if (JobOrder.canChange(job.getStatus(), s)) {
                options += "<option>" + s + "</option>";
            }
        }
        if (options.isEmpty()) {
            out.println("<p>This job is " + PageHelper.esc(job.getStatus()) + ". It can no longer be changed.</p>");
        } else {
            out.println("<form action='do.updatejob' method='POST'>");
            out.println("<input type='hidden' name='jobid' value='" + job.getJobid() + "'>");
            out.println("<input type='hidden' name='action' value='status'>");
            out.println("<label>Change to: </label><select name='status'>" + options + "</select>");
            out.println("<input type='submit' value='Update status'>");
            out.println("</form>");
            out.println("<p><small>Success takes the parts added by a technician out of stock, so they need enough stock. "
                    + "Parts paid with the build order already left the stock.</small></p>");
        }

        // 2. Parts
        out.println("<h3>Parts</h3>");
        if (job.getItems().isEmpty()) {
            out.println("<p>No parts in this build yet.</p>");
        } else {
            out.println("<table border='1'>");
            out.println("<tr><th>Code</th><th>Part</th><th>Source</th><th>Unit price</th><th>Qty</th><th>Subtotal</th><th>Stock</th>"
                    + (job.isEditable() ? "<th>Change</th>" : "") + "</tr>");
            for (BuildItem item : job.getItems()) {
                String row = "<tr><td>" + PageHelper.esc(item.getCode()) + "</td>"
                        + "<td>" + PageHelper.esc(item.getName()) + "</td>"
                        + "<td>" + item.getSourceLabel() + "</td>"
                        + "<td>" + PageHelper.money(item.getUnitprice()) + "</td>"
                        + "<td>" + item.getQuantity() + "</td>"
                        + "<td>" + PageHelper.money(item.getSubtotal()) + "</td>"
                        + "<td>" + (item.isCustomerPart() ? "-" : item.getStock() + (item.isInStock() ? "" : " <b>NOT ENOUGH</b>")) + "</td>";
                if (job.isEditable() && item.isCustomerPart()) {
                    row += "<td><small>Paid by customer</small></td>";
                } else if (job.isEditable()) {
                    row += "<td><form action='do.updatejob' method='POST' style='display:inline'>"
                            + "<input type='hidden' name='jobid' value='" + job.getJobid() + "'>"
                            + "<input type='hidden' name='action' value='updateqty'>"
                            + "<input type='hidden' name='builditemid' value='" + item.getBuilditemid() + "'>"
                            + "<input type='text' name='quantity' value='" + item.getQuantity() + "' size='3'>"
                            + "<input type='submit' value='Set qty'></form> "
                            + "<form action='do.updatejob' method='POST' style='display:inline'>"
                            + "<input type='hidden' name='jobid' value='" + job.getJobid() + "'>"
                            + "<input type='hidden' name='action' value='removeitem'>"
                            + "<input type='hidden' name='builditemid' value='" + item.getBuilditemid() + "'>"
                            + "<input type='submit' value='Remove'></form></td>";
                }
                out.println(row + "</tr>");
            }
            out.println("</table>");
            out.println("<p><b>Build total: PHP " + PageHelper.money(job.getTotal()) + "</b></p>");
        }

        // add a part (only while the job is open)
        if (job.isEditable()) {
            out.println("<h4>Add or swap a part</h4>");
            out.println("<form action='do.updatejob' method='POST'>");
            out.println("<input type='hidden' name='jobid' value='" + job.getJobid() + "'>");
            out.println("<input type='hidden' name='action' value='additem'>");
            out.println("Product ID or code: <input type='text' name='productid'>");
            out.println("Quantity: <input type='text' name='quantity' value='1' size='3'>");
            out.println("<input type='submit' value='Add part'>");
            out.println("</form>");
            out.println("<p><small>To swap a part, remove the old one and add the new one. "
                    + "<a href='pcbuilder.jsp'>Check compatibility</a></small></p>");
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
        return "Update job order";
    }// </editor-fold>

}
