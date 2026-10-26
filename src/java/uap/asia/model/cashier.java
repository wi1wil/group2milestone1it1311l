/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package uap.asia.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Andrei
 */
@WebServlet(name = "cashier", urlPatterns = {"/cashier.jsp"})
public class cashier extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    HttpSession session;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        session = request.getSession(false);

        String displayhtml = "";
        String cashierdetailshtml = "";
        HttpSession session = request.getSession(false);
        
        String cashier = "null, empty"; // cashier name
        Integer idnum = -1; 
        Integer transactionid = -1;        
        LocalDateTime transtimestamp = LocalDateTime.now();

        if (session != null) {
            cashier = (String) session.getAttribute("name");
            idnum = (Integer) session.getAttribute("e_id");
            transactionid = (Integer) session.getAttribute("sale_id");
            transtimestamp = (LocalDateTime) session.getAttribute("sale_date");
        }
        
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            
            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Cashier POS</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1><h1>Cashier POS System</h1></h1>");
            out.println("<h2>Welcome, " + cashier + "!</h2>");
            
            cashierdetailshtml += "<p class='cashierinfo'>Name: <b>" + cashier +
                    "</b> | Employee ID: <b>" + idnum +
                    "</b> | Transaction ID: <b>" + transactionid + 
                    "</b> | Timestamp: <b>" + transtimestamp + "</b></p>";
            
            out.println(cashierdetailshtml);
            
            // UI Display
            displayhtml += "<form action=\"do.donecashier\" method=\"POST\">";
            displayhtml += "<label>Input product ID: </label>\n" +
                    "<input type=\"text\" name=\"productid\"> <br> <br>";

            displayhtml += "<label>Input product price: </label>\n" +
                    "<input type=\"text\" name=\"productprice\"> <br> <br>";

            displayhtml += "<label>Input quantity: </label>\n" +
                    "<input type=\"text\" name=\"productquantity\"> <br> <br>";

            displayhtml += "<label>Discount (%): </label>\n" +
                    "<input type=\"text\" name=\"discount\" value=\"0\"> <br> <br>";

            displayhtml += "<label>Total: </label>\n" +
                    "<input type=\"text\" name=\"total\"> <br> <br>";

            displayhtml += "\n" +
                    "<input type=\"submit\" name=\"actionbtn\" value=\"Next\">\n" +
                    "<input type=\"submit\" name=\"actionbtn\" value=\"Done\">\n" + 
                    " &nbsp; | &nbsp; " +
                    "<input type=\"submit\" name=\"actionbtn\" value=\"Delete\">\n" +
                    "<input type=\"submit\" name=\"actionbtn\" value=\"Dummy\">\n" ;

            displayhtml += "<p>Tip: product IDs and prices are listed on the <a href=\"products.jsp\">Products</a> page.</p>\n" +
                    "<ul>\n" + 
                    "<li><a href=\"refund.jsp\">Returns and refunds</a></li>\n" + 
                    "<li><a href=\"transactions.jsp\">Transaction history</a></li>" +
                    "</ul>";
            // end of UI display
            
            out.println(displayhtml);
            
            out.println("<a href='index.jsp' class='indexbtn'>Back to index</a>");
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usrdebug = request.getParameter("debugmode");
        
        
        // /* DEBUGGING
        if ("thisisandreimartinwilson".equals(usrdebug)){
            session = request.getSession();
            session.setAttribute("name", "Bobby");
            session.setAttribute("e_id", 1);
            session.setAttribute("sale_id", 1);
            session.setAttribute("sale_date", LocalDateTime.now());
        }
        // */
        
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
