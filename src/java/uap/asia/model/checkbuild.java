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
// PC Builder: totals the parts and checks compatibility (form on pcbuilder.jsp). URL: /do.checkbuild
@WebServlet(name = "checkbuild", urlPatterns = {"/do.checkbuild"})
public class checkbuild extends HttpServlet {

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
            out.println("<title>Build Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");

            // Read the part selected in each dropdown
            String cpuText = request.getParameter("cpu");
            String mbText = request.getParameter("motherboard");
            String ramText = request.getParameter("ram");
            String gpuText = request.getParameter("gpu");
            String storageText = request.getParameter("storage");
            String psuText = request.getParameter("psu");
            String caseText = request.getParameter("pccase");
            String coolerText = request.getParameter("cooler");
            String action = request.getParameter("action");

            // Stop if the page was opened directly, without the form data
            if (cpuText == null || mbText == null || ramText == null || gpuText == null
                    || storageText == null || psuText == null || caseText == null || coolerText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='pcbuilder.jsp'>Go to PC Builder</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // each option value looks like "name|spec|spec|price"
            String[] cpu = cpuText.split("\\|");        // name | socket | tdp | price
            String[] mb = mbText.split("\\|");          // name | socket | ram type | form factor | price
            String[] ram = ramText.split("\\|");        // name | type | price
            String[] gpu = gpuText.split("\\|");        // name | tdp | price
            String[] storage = storageText.split("\\|"); // name | price
            String[] psu = psuText.split("\\|");        // name | watts | price
            String[] pcCase = caseText.split("\\|");    // name | largest board | price
            String[] cooler = coolerText.split("\\|");  // name | price

            // Add up the prices of all parts
            int total = Integer.parseInt(cpu[3]) + Integer.parseInt(mb[4]) + Integer.parseInt(ram[2])
                    + Integer.parseInt(gpu[2]) + Integer.parseInt(storage[1]) + Integer.parseInt(psu[2])
                    + Integer.parseInt(pcCase[2]) + Integer.parseInt(cooler[1]);

            // compatibility checks
            // CPU socket must match the motherboard socket
            boolean socketOk = cpu[1].equals(mb[1]);
            // RAM type must match the motherboard
            boolean ramOk = mb[2].equals(ram[1]);

            // PSU must cover the CPU + GPU power plus about 100W for the rest
            int neededWatts = Integer.parseInt(cpu[2]) + Integer.parseInt(gpu[1]) + 100;
            boolean psuOk = Integer.parseInt(psu[1]) >= neededWatts;

            // Case must be the same size or bigger than the board (ITX < mATX < ATX)
            int boardSize = 3;                       // ATX
            if (mb[3].equals("ITX")) {
                boardSize = 1;
            } else if (mb[3].equals("mATX")) {
                boardSize = 2;
            }
            int caseSize = 3;
            if (pcCase[1].equals("ITX")) {
                caseSize = 1;
            } else if (pcCase[1].equals("mATX")) {
                caseSize = 2;
            }
            boolean formOk = caseSize >= boardSize;

            // Build is compatible only if every check passes
            boolean allOk = socketOk && ramOk && psuOk && formOk;

            // Print the selected parts, the total and the check results
            out.println("<h1>PC Build Summary</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>Part</th><th>Selected</th></tr>");
            out.println("<tr><td>CPU</td><td>" + cpu[0] + "</td></tr>");
            out.println("<tr><td>Motherboard</td><td>" + mb[0] + "</td></tr>");
            out.println("<tr><td>RAM</td><td>" + ram[0] + "</td></tr>");
            out.println("<tr><td>Graphics card</td><td>" + gpu[0] + "</td></tr>");
            out.println("<tr><td>Storage</td><td>" + storage[0] + "</td></tr>");
            out.println("<tr><td>Power supply</td><td>" + psu[0] + "</td></tr>");
            out.println("<tr><td>Case</td><td>" + pcCase[0] + "</td></tr>");
            out.println("<tr><td>CPU cooler</td><td>" + cooler[0] + "</td></tr>");
            out.println("</table>");
            out.println("<h3>Estimated total: PHP " + total + "</h3>");

            out.println("<h2>Compatibility Check</h2>");
            out.println("<ul>");
            out.println("<li>CPU socket vs motherboard socket (" + cpu[1] + " / " + mb[1] + "): " + (socketOk ? "PASS" : "FAIL") + "</li>");
            out.println("<li>RAM type vs motherboard (" + ram[1] + " / " + mb[2] + "): " + (ramOk ? "PASS" : "FAIL") + "</li>");
            out.println("<li>Power supply (needs about " + neededWatts + "W, has " + psu[1] + "W): " + (psuOk ? "PASS" : "FAIL") + "</li>");
            out.println("<li>Motherboard size vs case (" + mb[3] + " board, case fits up to " + pcCase[1] + "): " + (formOk ? "PASS" : "FAIL") + "</li>");
            out.println("<li>PSU connectors and physical clearance: not checked yet</li>");
            out.println("</ul>");
            out.println("<h2>Result: " + (allOk ? "COMPATIBLE" : "INCOMPATIBLE") + "</h2>");

            // Explain each failed check
            if (!socketOk) {
                out.println("<p>The CPU uses socket " + cpu[1] + " but the motherboard uses " + mb[1] + ", so they cannot be installed together.</p>");
            }
            if (!ramOk) {
                out.println("<p>The motherboard supports " + mb[2] + " memory, but the selected RAM is " + ram[1] + ".</p>");
            }
            if (!psuOk) {
                out.println("<p>The build needs about " + neededWatts + "W but the power supply only gives " + psu[1] + "W.</p>");
            }
            if (!formOk) {
                out.println("<p>A " + mb[3] + " motherboard will not fit in a case that only fits up to " + pcCase[1] + ".</p>");
            }

            // "Place build order" button: only allowed if the build is compatible
            if (action != null && action.equals("order")) {
                out.println("<h2>Build Order</h2>");
                if (allOk) {
                    out.println("<p>Build order placed. Job order JO-" + (System.currentTimeMillis() % 1000) + " sent to the technicians (status: Pending).</p>");
                } else {
                    out.println("<p>Build order NOT placed because the build is incompatible.</p>");
                }
            }
            out.println("<p><a href='pcbuilder.jsp'>Back to PC Builder</a> | <a href='index.jsp'>Index</a></p>");
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
