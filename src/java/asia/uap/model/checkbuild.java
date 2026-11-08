/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.SqlcBuilds;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.ParseThis;
import asia.uap.functionalclasses.CompatibilityChecker;
import asia.uap.functionalclasses.UserSession;
import asia.uap.functionalclasses.BuilderSession;
import asia.uap.classes.BuildPart;
import asia.uap.classes.PcBuild;
import asia.uap.classes.BuildSlot;
import asia.uap.classes.CompatibilityResult;
import asia.uap.classes.Product;
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

/**
 *
 * @author Group 2
 */
// PC Builder logic: every button of pcbuilder.java comes here. URL: /do.checkbuild
//
//   action = select   (slot, pick = product_id, quantity) -> put the part in the build
//            remove   (slot)                              -> take the part out
//            clear                                        -> empty the build
//            customer (username)  staff only              -> who the build is for
//            savequote (quotename)    customer only       -> save the build as a quote for the cashier
//            loadquote (quoteid)      customer or staff   -> open a saved quote (today's prices)
//            deletequote (quoteid)    customer or staff   -> delete a saved quote
//            checkout -> staff only: check everything again, show the order summary and the payment form
//            order    (buildname, mop, payment) -> staff only: SqlcBuilds.placeBuildOrder, then the confirmation
//
// select / remove / clear / customer / quote actions save a message and go back to pcbuilder.jsp (redirect).
//
// EDITED: the store has no online orders. Customers plan a build on the store's device and save it
// as a quote. At the counter, the cashier loads the quote and takes the payment (cash, card or cashless).
// The sale is recorded under the staff member who placed the order.
//
// EDITED: the old version read hard-coded "name|spec|price" dropdowns and printed a made-up
// job order number. It now uses the products, product_specs, sales, builds and job_orders tables.
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
        // Only the forms (POST) change the build. Opened directly = back to the builder.
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendRedirect("pcbuilder.jsp");
            return;
        }

        HttpSession session = request.getSession();
        PcBuild build = BuilderSession.getBuild(session);
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "select":
                selectPart(request, session, build);
                break;
            case "remove": {
                BuildSlot slot = BuildSlot.find(request.getParameter("slot"));
                if (slot != null && build.get(slot.getKey()) != null) {
                    build.remove(slot.getKey());
                    PageHelper.setMessage(session, PageHelper.SUCCESS, slot.getLabel() + " removed.");
                }
                break;
            }
            case "clear":
                build.clear();
                PageHelper.setMessage(session, PageHelper.INFO, "The build is empty.");
                break;
            case "customer":
                chooseCustomer(request, session, build);
                break;
            case "savequote":
                saveQuote(request, session, build);
                break;
            case "loadquote":
                build = loadQuote(request, session, build);
                break;
            case "deletequote":
                deleteQuote(request, session, build);
                break;
            case "checkout":
                showCheckout(request, response, session, build);
                return;
            case "order":
                placeOrder(request, response, session, build);
                return;
            default:
                PageHelper.setMessage(session, PageHelper.ERROR, "Unknown action.");
        }
        session.setAttribute("pcBuild", build);
        response.sendRedirect("pcbuilder.jsp");
    }

    // action=select: put the picked card in its slot
    private void selectPart(HttpServletRequest request, HttpSession session, PcBuild build) {
        BuildSlot slot = BuildSlot.find(request.getParameter("slot"));
        Integer productId = ParseThis.toInt(request.getParameter("pick")); // the radio button's product_id
        if (slot == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Unknown part type.");
            return;
        }
        if (productId == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Select a " + slot.getLabel() + " first.");
            return;
        }

        // quantity: 1 to the slot's maximum (blank = 1)
        String qtyText = request.getParameter("quantity");
        int qty = 1;
        if (!ParseThis.isBlank(qtyText)) {
            Integer q = ParseThis.toInt(qtyText);
            if (q == null || q < 1 || q > slot.getMaxqty()) {
                PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be from 1 to " + slot.getMaxqty() + ".");
                return;
            }
            qty = q;
        }

        // the product must exist and belong to this slot's category (the form could be changed by hand)
        Product p = Sqlc.getProduct(productId);
        if (p == null || !slot.getCategory().equalsIgnoreCase(p.getCategory())) {
            PageHelper.setMessage(session, PageHelper.ERROR, "That product is not a " + slot.getLabel() + ".");
            return;
        }

        // it must be for sale and in stock
        if (!p.isSellable()) {
            PageHelper.setMessage(session, PageHelper.ERROR, p.getName() + " is out of stock.");
            return;
        }
        if (p.getStockquantity() < qty) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Only " + p.getStockquantity() + " of " + p.getName() + " in stock.");
            return;
        }

        build.put(SqlcBuilds.storePart(slot.getKey(), productId, qty));
        // tell the user right away if the new part does not fit, or needs another part
        CompatibilityResult problem = null;
        for (CompatibilityResult r : CompatibilityChecker.check(build.getParts())) {
            if (r.isFail() && r.involves(slot.getKey()) && (problem == null || problem.isNeedsPart())) {
                problem = r; // a real clash is reported before a "needs a part" note
            }
        }
        if (problem == null) {
            PageHelper.setMessage(session, PageHelper.SUCCESS, slot.getLabel() + ": " + p.getName() + " added.");
        } else if (problem.isNeedsPart()) {
            PageHelper.setMessage(session, PageHelper.INFO, slot.getLabel() + ": " + p.getName() + " added. " + problem.getMessage());
        } else {
            PageHelper.setMessage(session, PageHelper.ERROR, slot.getLabel() + " added, but it does not fit: " + problem.getMessage());
        }
    }

    // action=customer: staff choose who the build is for
    private void chooseCustomer(HttpServletRequest request, HttpSession session, PcBuild build) {
        if (!UserSession.isEmployee(session)) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Only staff can build for another customer.");
            return;
        }
        String username = request.getParameter("username");
        if (ParseThis.isBlank(username)) {
            build.setCustomer(null, null);
            PageHelper.setMessage(session, PageHelper.INFO, "No customer chosen.");
            return;
        }
        String[] customer = Sqlc.findCustomer(username);
        if (customer == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Customer " + username.trim() + " was not found.");
            return;
        }
        Integer id = Integer.valueOf(customer[0]);
        if (!id.equals(build.getCustomerid())) {
            build.setQuote(null, null); // another customer's quote does not belong to this one
        }
        build.setCustomer(id, customer[1]);
        PageHelper.setMessage(session, PageHelper.SUCCESS, "Building for " + customer[1] + ".");
    }

    // action=savequote: a customer saves the build so the cashier can order it later
    private void saveQuote(HttpServletRequest request, HttpSession session, PcBuild build) {
        if (!BuilderSession.canSave(session, build)) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Log in as a customer to save a build.");
            return;
        }
        // name (builds.build_name is 100 characters); blank = "My PC Build"
        String name = request.getParameter("quotename");
        if (ParseThis.isBlank(name)) {
            name = "My PC Build";
        }
        name = name.trim();
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        try {
            int quoteId = SqlcBuilds.saveQuote(build, name);
            PageHelper.setMessage(session, PageHelper.SUCCESS, "Saved as " + String.format("Q-%03d", quoteId)
                    + ". Tell the cashier your username to order and pay.");
        } catch (IllegalArgumentException e) {
            PageHelper.setMessage(session, PageHelper.ERROR, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not save the build. Please try again.");
        }
    }

    // action=loadquote: open a saved quote in the builder (customers only their own)
    private PcBuild loadQuote(HttpServletRequest request, HttpSession session, PcBuild build) {
        Integer quoteId = ParseThis.toInt(request.getParameter("quoteid"));
        boolean customer = BuilderSession.isCustomer(session);
        if (quoteId == null || (!customer && !UserSession.isEmployee(session))) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Log in to open a saved build.");
            return build;
        }
        try {
            ArrayList<String> skipped = new ArrayList<>();
            PcBuild loaded = SqlcBuilds.loadQuote(quoteId, customer ? BuilderSession.customerId(session) : null, skipped);
            if (loaded == null) {
                PageHelper.setMessage(session, PageHelper.ERROR, "That saved build was not found.");
                return build;
            }
            String text = "Opened " + String.format("Q-%03d", quoteId) + " (" + loaded.getQuotename() + ") for "
                    + loaded.getCustomername() + ". Prices are today's prices.";
            if (!skipped.isEmpty()) {
                text += " Not available any more, choose again: " + String.join(", ", skipped) + ".";
            }
            PageHelper.setMessage(session, skipped.isEmpty() ? PageHelper.SUCCESS : PageHelper.INFO, text);
            return loaded;
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not open the saved build. Please try again.");
            return build;
        }
    }

    // action=deletequote: delete a saved quote (customers only their own)
    private void deleteQuote(HttpServletRequest request, HttpSession session, PcBuild build) {
        Integer quoteId = ParseThis.toInt(request.getParameter("quoteid"));
        boolean customer = BuilderSession.isCustomer(session);
        if (quoteId == null || (!customer && !UserSession.isEmployee(session))) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Log in to delete a saved build.");
            return;
        }
        try {
            if (SqlcBuilds.deleteQuote(quoteId, customer ? BuilderSession.customerId(session) : null)) {
                if (quoteId.equals(build.getQuoteid())) {
                    build.setQuote(null, null); // the open build is no longer linked to a quote
                }
                PageHelper.setMessage(session, PageHelper.SUCCESS, String.format("Q-%03d", quoteId) + " deleted.");
            } else {
                PageHelper.setMessage(session, PageHelper.ERROR, "That saved build was not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not delete the saved build. Please try again.");
        }
    }

    /**
     * Everything that must be true before ordering. Returns "" when the build can be ordered.
     * Prices are refreshed here (the price or promo may have changed since picking).
     */
    private String readyToOrder(HttpSession session, PcBuild build) {
        if (!UserSession.isEmployee(session)) {
            return "Orders are placed and paid at the cashier. Save your build and tell the cashier your username.";
        }
        if (!BuilderSession.canOrder(session, build)) {
            return "Type the customer's username first.";
        }
        if (!build.getMissingRequired().isEmpty()) {
            return "The build still needs: " + build.getMissingRequired() + ".";
        }
        for (BuildPart part : build.getPartList()) {
            BuildPart fresh = SqlcBuilds.storePart(part.getSlotkey(), part.getProductid(), part.getQuantity());
            if (fresh == null) {
                return part.getName() + " is no longer sold. Choose another part.";
            }
            build.put(fresh);
        }
        if (!CompatibilityChecker.isCompatible(CompatibilityChecker.check(build.getParts()))) {
            return "The parts are not compatible. Fix the failed checks first.";
        }
        return "";
    }

    // action=checkout: the order summary and the payment form
    private void showCheckout(HttpServletRequest request, HttpServletResponse response, HttpSession session, PcBuild build)
            throws IOException {
        String problem = readyToOrder(session, build);
        if (!problem.isEmpty()) {
            PageHelper.setMessage(session, PageHelper.ERROR, problem);
            response.sendRedirect("pcbuilder.jsp");
            return;
        }
        session.setAttribute("pcBuild", build);

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            printHead(out, "Place Build Order", request);
            out.println("<h1>Place Build Order</h1>");
            out.println("<p>Customer: <b>" + PageHelper.esc(build.getCustomername()) + "</b>"
                    + (build.getQuoteid() == null ? "" : " | From saved build " + String.format("Q-%03d", build.getQuoteid())) + "</p>");
            out.println(partsSummary(build));

            // the quote's name is suggested as the build name
            String suggested = (build.getQuotename() == null) ? "My PC Build" : build.getQuotename();
            out.println("<form action='do.checkbuild' method='POST'>");
            out.println("<input type='hidden' name='action' value='order'>");
            out.println("<p><label>Build name: </label><input type='text' name='buildname' value='"
                    + PageHelper.esc(suggested) + "' maxlength='100'></p>");

            out.println("<h3>Payment: PHP " + PageHelper.money(build.getTotal()) + "</h3>");
            out.println("<p><label>Payment method: </label><select name='mop'>");
            out.println("<option value='cash'>Cash</option>");
            out.println("<option value='card'>Card</option>");
            out.println("<option value='cashless'>Cashless</option>");
            out.println("</select></p>");
            out.println("<p><label>Amount paid: </label><input type='text' name='payment'> "
                    + "<small>(required for cash; card and cashless are charged the exact total)</small></p>");
            out.println("<p><input type='submit' value='Place order'"
                    + " onclick=\"return confirm('Place this build order?');\"></p>");
            out.println("</form>");
            out.println("<p><a href='pcbuilder.jsp'>Back to the builder</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // action=order: check the payment, save everything, show the confirmation
    private void placeOrder(HttpServletRequest request, HttpServletResponse response, HttpSession session, PcBuild build)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            printHead(out, "Build Order", request);

            if (build.isEmpty()) {
                out.println("<h1>Nothing to order</h1>");
                out.println("<p>The build is empty. If you clicked Place order twice, the order was already saved.</p>");
                printFooter(out);
                return;
            }
            String problem = readyToOrder(session, build);
            if (!problem.isEmpty()) {
                orderFailed(out, problem);
                return;
            }

            // Build name (builds.build_name is 100 characters)
            String buildName = request.getParameter("buildname");
            if (ParseThis.isBlank(buildName)) {
                buildName = "My PC Build";
            }
            buildName = buildName.trim();
            if (buildName.length() > 100) {
                buildName = buildName.substring(0, 100);
            }

            // Payment, taken by the staff member at the counter
            double total = build.getTotal();
            double paid;
            String method;
            String mop = request.getParameter("mop");
            if ("cash".equals(mop)) {
                method = "Cash";
                String paymentText = request.getParameter("payment");
                if (ParseThis.isBlank(paymentText) || !ParseThis.isDouble(paymentText)) {
                    orderFailed(out, "Type the amount of cash received.");
                    return;
                }
                paid = PageHelper.round2(ParseThis.toDouble(paymentText));
                if (paid < total) {
                    orderFailed(out, "Total is PHP " + PageHelper.money(total) + " but only PHP " + PageHelper.money(paid)
                            + " was paid. Short by PHP " + PageHelper.money(total - paid) + ".");
                    return;
                }
            } else if ("card".equals(mop) || "cashless".equals(mop)) {
                method = "card".equals(mop) ? "Card" : "Cashless";
                paid = total;
            } else {
                orderFailed(out, "Choose a payment method.");
                return;
            }

            // Save. The build leaves the session first, so a second click finds nothing to order.
            session.removeAttribute("pcBuild");
            String[] result; // { build_id, job_id, sale_id, technician }
            try {
                result = SqlcBuilds.placeBuildOrder(build, buildName, UserSession.employeeId(session), paid, method);
            } catch (IllegalArgumentException e) {
                session.setAttribute("pcBuild", build); // not saved: keep the build
                orderFailed(out, e.getMessage());
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                session.setAttribute("pcBuild", build);
                orderFailed(out, "Could not save the order. Nothing was charged. Please try again.");
                return;
            }

            // Confirmation
            out.println("<h1>Build order placed</h1>");
            out.println("<p>Build: <b>" + PageHelper.esc(buildName) + "</b> (build #" + result[0] + ") for "
                    + PageHelper.esc(build.getCustomername()) + "</p>");
            out.println("<p>Job order: <b>" + String.format("JO-%03d", Integer.parseInt(result[1])) + "</b> | Technician: "
                    + PageHelper.esc(result[3]) + " | Status: Pending</p>");
            out.println(partsSummary(build));
            out.println("<p>Receipt no: <b>" + String.format("TX-%04d", Integer.parseInt(result[2])) + "</b> | Paid ("
                    + PageHelper.esc(method) + "): PHP " + PageHelper.money(paid)
                    + " | Change: PHP " + PageHelper.money(paid - total) + "</p>");
            out.println("<p>The technician will build the PC. Check the status on the PC Builder page under Build orders.</p>");
            printFooter(out);
        }
    }

    // The chosen parts and the total
    private String partsSummary(PcBuild build) {
        String html = "<table border='1'>";
        html += "<tr><th>Part</th><th>Product</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr>";
        for (BuildPart part : build.getPartList()) {
            BuildSlot slot = BuildSlot.find(part.getSlotkey());
            html += "<tr><td>" + PageHelper.esc(slot.getLabel()) + "</td>"
                    + "<td>" + PageHelper.esc(part.getName()) + "</td>"
                    + "<td>" + part.getQuantity() + "</td>"
                    + "<td>" + PageHelper.money(part.getUnitprice()) + "</td>"
                    + "<td>" + PageHelper.money(part.getSubtotal()) + "</td></tr>";
        }
        html += "</table>";
        html += "<p><b>Total to pay: PHP " + PageHelper.money(build.getTotal()) + "</b></p>";
        return html;
    }

    private void orderFailed(PrintWriter out, String reason) {
        out.println("<h1>Order not placed</h1>");
        out.println("<p>" + PageHelper.esc(reason) + "</p>");
        out.println("<p>Nothing was saved or charged. Your parts are still in the builder.</p>");
        printFooter(out);
    }

    private void printHead(PrintWriter out, String title, HttpServletRequest request) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println(PageHelper.VIEWPORT);
        out.println("<title>" + title + "</title>");
        out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
    }

    private void printFooter(PrintWriter out) {
        out.println("<p><a href='pcbuilder.jsp'>Back to the PC Builder</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
        out.println("</body>");
        out.println("</html>");
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
        return "PC builder actions and build orders";
    }// </editor-fold>

}
