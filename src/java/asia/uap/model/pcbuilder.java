/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.SqlcJobs;
import asia.uap.functionalclasses.SqlcBuilds;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.CompatibilityChecker;
import asia.uap.functionalclasses.UserSession;
import asia.uap.functionalclasses.BuilderSession;
import asia.uap.classes.JobOrder;
import asia.uap.classes.BuildPart;
import asia.uap.classes.PcBuild;
import asia.uap.classes.BuildQuote;
import asia.uap.classes.BuildSlot;
import asia.uap.classes.CartItem;
import asia.uap.classes.CompatibilityResult;
import asia.uap.classes.Product;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
// PC Builder screen. URL: /pcbuilder.jsp (this servlet replaces the old pcbuilder.jsp file, like cashier.jsp)
//
// The user builds a PC one part at a time:
//   - The parts table has one row per slot (CPU, motherboard, RAM, ...), each with a "Choose" button.
//   - Only the slot being chosen opens the picker (GET ?open=cpu): a card grid of the store's
//     products with radio buttons, like the cashier. Cards that would not fit the parts
//     already chosen say why. Every part is paid when the build is ordered.
//   - The compatibility check runs on every page view (CompatibilityChecker).
//   - Every button here goes to checkbuild.java (/do.checkbuild).
//
// EDITED (in-store only, no online orders):
//   - anyone can pick parts and check compatibility on the store's device
//   - a logged-in customer SAVES the build as a quote ("My saved builds")
//   - at the counter, staff open the customer's saved build (or build for a typed username),
//     then "Place build order" takes the payment and creates the sale and job order
@WebServlet(name = "pcbuilder", urlPatterns = {"/pcbuilder.jsp"})
public class pcbuilder extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");

        // The build lives in the session, so a session is needed even when logged out
        HttpSession session = request.getSession();
        PcBuild build = BuilderSession.getBuild(session);

        // Which slot's picker is open (null = none)
        BuildSlot open = BuildSlot.find(request.getParameter("open"));

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>PC Builder</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>PC Builder</h1>");
            out.println(PageHelper.popMessage(session));
            out.println("<p>Choose a part for each row, then place the build order and pay. "
                    + "The compatibility check below updates as you go.</p>");

            // 1. Who the build is for
            out.println(customerHtml(session, build));

            // 2. The parts table
            out.println(partsTable(build, open));

            // 3. The picker (only for the slot being chosen)
            if (open != null) {
                out.println(pickerHtml(open, build));
            }

            // 4. Compatibility
            ArrayList<CompatibilityResult> results = CompatibilityChecker.check(build.getParts());
            out.println(compatibilityHtml(build, results));

            // 5. Order
            out.println(orderHtml(session, build, results));

            // 6. Saved builds (quotes)
            out.println(quotesHtml(session, build));

            // 7. The customer's build orders
            if (build.getCustomerid() != null) {
                out.println(myOrdersHtml(build.getCustomerid()));
            }

            out.println("<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Who the build is for. Staff can type a customer username.
    private String customerHtml(HttpSession session, PcBuild build) {
        String html = "<h3>Customer</h3>";
        if ("customer".equals(UserSession.role(session))) {
            html += "<p>Building for: <b>" + PageHelper.esc(build.getCustomername()) + "</b> (you). "
                    + "When the build is ready, save it, then tell the cashier your username to order and pay.</p>";
        } else if (UserSession.isEmployee(session)) {
            html += "<form action='do.checkbuild' method='POST'>";
            html += "<input type='hidden' name='action' value='customer'>";
            html += "<label>Customer username: </label><input type='text' name='username'> ";
            html += "<input type='submit' value='Build for this customer'>";
            html += "</form>";
            html += "<p>Building for: <b>" + (build.getCustomerid() == null ? "nobody yet"
                    : PageHelper.esc(build.getCustomername())) + "</b></p>";
        } else {
            html += "<p>You are not logged in. You can pick parts and check compatibility, but "
                    + "<a href='login.jsp'>log in</a> to save your build for the cashier "
                    + "(no account yet? <a href='add.jsp'>register</a>). "
                    + "<small>(Logging in starts a new session, so pick your parts after logging in.)</small></p>";
        }
        // the quote that is open right now
        if (build.getQuoteid() != null) {
            html += "<p>Open saved build: <b>" + String.format("Q-%03d", build.getQuoteid()) + " "
                    + PageHelper.esc(build.getQuotename()) + "</b></p>";
        }
        return html;
    }

    /**
     * Saved builds (quotes):
     *   customer -> their own, with Open and Delete
     *   staff    -> the chosen customer's, or every customer's when none is chosen; Open loads it
     *               (and its customer) so it can be ordered at the counter
     */
    private String quotesHtml(HttpSession session, PcBuild build) {
        boolean customer = BuilderSession.isCustomer(session);
        if (!customer && !UserSession.isEmployee(session)) {
            return "";
        }
        ArrayList<BuildQuote> quotes;
        try {
            quotes = SqlcBuilds.getQuotes(customer ? BuilderSession.customerId(session) : build.getCustomerid());
        } catch (SQLException e) {
            e.printStackTrace();
            return "<p>Could not load the saved builds.</p>";
        }

        String html = "<h3>" + (customer ? "My saved builds" : (build.getCustomerid() == null
                ? "Saved builds of all customers" : "Saved builds of " + PageHelper.esc(build.getCustomername()))) + "</h3>";
        if (quotes.isEmpty()) {
            return html + "<p>No saved builds" + (customer ? " yet." : ".") + "</p>";
        }
        html += "<table border='1'>";
        html += "<tr><th>Quote</th><th>Name</th>" + (customer ? "" : "<th>Customer</th>")
                + "<th>Saved</th><th>Parts</th><th>Price when saved</th><th></th></tr>";
        for (BuildQuote q : quotes) {
            html += "<tr><td>" + q.getQuoteNumber() + "</td>"
                    + "<td>" + PageHelper.esc(q.getBuildname()) + "</td>"
                    + (customer ? "" : "<td>" + PageHelper.esc(q.getCustomername()) + " (" + PageHelper.esc(q.getUsername()) + ")</td>")
                    + "<td>" + PageHelper.dateTime(q.getSavedat()) + "</td>"
                    + "<td>" + q.getPartcount() + "</td>"
                    + "<td>" + PageHelper.money(q.getTotal()) + "</td>"
                    + "<td><form action='do.checkbuild' method='POST' class='pb-inline'>"
                    + "<input type='hidden' name='action' value='loadquote'>"
                    + "<input type='hidden' name='quoteid' value='" + q.getBuildid() + "'>"
                    + "<input type='submit' value='" + (customer ? "Open" : "Open to order") + "'></form>"
                    + "<form action='do.checkbuild' method='POST' class='pb-inline'>"
                    + "<input type='hidden' name='action' value='deletequote'>"
                    + "<input type='hidden' name='quoteid' value='" + q.getBuildid() + "'>"
                    + "<input type='submit' class='btn-ghost' value='Delete' onclick=\"return confirm('Delete this saved build?');\"></form>"
                    + "</td></tr>";
        }
        html += "</table>";
        return html;
    }

    // One row per slot: what is chosen, and the Choose / Remove buttons
    private String partsTable(PcBuild build, BuildSlot open) {
        String html = "<h3>Parts</h3>";
        html += "<table border='1'>";
        html += "<tr><th>Part</th><th>Chosen</th><th>Qty</th><th>Price</th><th>Subtotal</th><th></th></tr>";
        for (BuildSlot slot : BuildSlot.SLOTS) {
            BuildPart part = build.get(slot.getKey());
            String label = PageHelper.esc(slot.getLabel()) + (slot.isRequired() ? " *" : " <small>(optional)</small>");
            String chooseText = (part == null ? "Choose " : "Change ") + slot.getLabel();
            String chooseButton = (open != null && open.getKey().equals(slot.getKey()))
                    ? "<b>Choosing...</b>"
                    // a GET form, so it only opens the picker and changes nothing
                    : "<form action='pcbuilder.jsp#picker' method='GET' class='pb-inline'>"
                    + "<input type='hidden' name='open' value='" + slot.getKey() + "'>"
                    + "<input type='submit' value='" + PageHelper.esc(chooseText) + "'></form>";

            if (part == null) {
                html += "<tr><td>" + label + "</td><td><i>none</i></td><td></td><td></td><td></td>"
                        + "<td>" + chooseButton + "</td></tr>";
            } else {
                html += "<tr><td>" + label + "</td>"
                        + "<td>" + PageHelper.esc(part.getName()) + " <small>(" + PageHelper.esc(part.getCode()) + ")</small></td>"
                        + "<td>" + part.getQuantity() + "</td>"
                        + "<td>" + PageHelper.money(part.getUnitprice()) + "</td>"
                        + "<td>" + PageHelper.money(part.getSubtotal()) + "</td>"
                        + "<td>" + chooseButton + " "
                        + "<form action='do.checkbuild' method='POST' class='pb-inline'>"
                        + "<input type='hidden' name='action' value='remove'>"
                        + "<input type='hidden' name='slot' value='" + slot.getKey() + "'>"
                        + "<input type='submit' value='Remove'></form></td></tr>";
            }
        }
        html += "</table>";
        html += "<p><small>* required</small></p>";
        return html;
    }

    // The card grid for ONE slot (the store's products of that category)
    private String pickerHtml(BuildSlot slot, PcBuild build) {
        String html = "<div class='pb-picker' id='picker'>";
        html += "<h3>Choose a " + PageHelper.esc(slot.getLabel()) + " <small><a href='pcbuilder.jsp'>(close)</a></small></h3>";

        // specs of every product in this category (one query), for the "won't fit" notes
        HashMap<Integer, HashMap<String, String>> specs = Sqlc.getSpecsForCategory(slot.getCategory());

        html += "<form action='do.checkbuild' method='POST'>";
        html += "<input type='hidden' name='action' value='select'>";
        html += "<input type='hidden' name='slot' value='" + slot.getKey() + "'>";
        int cards = 0;

        ArrayList<Product> products = Sqlc.getProductsByCategory(slot.getCategory());
        HashMap<Integer, Double> promos = Sqlc.getActivePromos();
        if (products.isEmpty()) {
            html += "<p>No " + PageHelper.esc(slot.getLabel()) + " products in the store yet.</p>";
        } else {
            html += "<div class='pos-grid'>";
            for (Product p : products) {
                boolean pickable = p.isSellable();
                double promo = Sqlc.bestPromo(promos, p.getProductid());
                double price = new CartItem(p.getProductid(), p.getPrice(), 1, promo / 100.0).getPrice();

                html += "<label class='pos-card" + (pickable ? "" : " pos-disabled") + "'>";
                html += "<input type='radio' name='pick' value='" + p.getProductid() + "'"
                        + (pickable ? " required" : " disabled") + ">";
                html += PageHelper.productImage(p, null); // EDITED: the product's picture (placeholder when none)
                html += "<span class='pos-name'>" + PageHelper.esc(p.getName()) + "</span>";
                html += "<span class='pos-code'>" + PageHelper.esc(p.getCode())
                        + (p.getBrand() == null ? "" : " | " + PageHelper.esc(p.getBrand())) + "</span>";
                if (promo > 0) {
                    html += "<span class='pos-price'><s>" + PageHelper.money(p.getPrice()) + "</s> <b>PHP "
                            + PageHelper.money(price) + "</b></span>";
                    html += "<span class='pos-promo'>" + PageHelper.money(promo) + "% off</span>";
                } else {
                    html += "<span class='pos-price'><b>PHP " + PageHelper.money(price) + "</b></span>";
                }
                html += specLine(specs.get(p.getProductid()));
                html += "<span class='pos-stock'>" + (pickable ? "Stock: " + p.getStockquantity() : "<b>Out of stock</b>") + "</span>";
                html += fitNotes(build, slot, p, specs);
                html += "</label>";
                if (pickable) {
                    cards++;
                }
            }
            html += "</div>";
        }

        // quantity (only RAM and storage can be more than 1) and the button
        if (cards > 0) {
            html += "<div class='pos-addbar'>";
            if (slot.getMaxqty() > 1) {
                html += "<label>Quantity (1 to " + slot.getMaxqty() + "): </label>"
                        + "<input type='text' name='quantity' value='1'> ";
            } else {
                html += "<input type='hidden' name='quantity' value='1'>";
            }
            html += "<button type='submit'>Use this " + PageHelper.esc(slot.getLabel()) + "</button> ";
            html += "<a href='pcbuilder.jsp'>Cancel</a>";
            html += "</div>";
        }
        html += "</form>";
        html += "</div>";
        return html;
    }

    /**
     * The notes on a card:
     *   red   "Won't fit: ..." = clashes with a part already chosen (e.g. wrong socket)
     *   amber "Needs: ..."     = works only if another part is added (e.g. a graphics card)
     */
    private String fitNotes(PcBuild build, BuildSlot slot, Product p, HashMap<Integer, HashMap<String, String>> specs) {
        BuildPart candidate = new BuildPart(slot.getKey(), p.getProductid(), p.getCode(), p.getName(),
                p.getPrice(), p.getPrice(), 0.0, 1, specs.get(p.getProductid()));
        String why = CompatibilityChecker.whyNot(build.getParts(), candidate);
        if (why != null) {
            return "<span class='pb-nofit'>Won't fit: " + PageHelper.esc(why) + "</span>";
        }
        String needs = CompatibilityChecker.needs(build.getParts(), candidate);
        if (needs != null) {
            return "<span class='pb-needs'>Needs: " + PageHelper.esc(needs) + "</span>";
        }
        return "";
    }

    // Short list of a product's specs for its card, e.g. "socket: AM5 | tdp: 65W"
    private String specLine(HashMap<String, String> specs) {
        if (specs == null || specs.isEmpty()) {
            return "<span class='pos-code'><i>no specs listed</i></span>";
        }
        String line = "";
        for (String name : specs.keySet()) {
            line += (line.isEmpty() ? "" : " | ") + PageHelper.esc(name.replace("_", " ")) + ": " + PageHelper.esc(specs.get(name));
        }
        return "<span class='pos-code'>" + line + "</span>";
    }

    // The compatibility results as a table, plus the overall result
    private String compatibilityHtml(PcBuild build, ArrayList<CompatibilityResult> results) {
        String html = "<h3>Compatibility check</h3>";
        if (build.isEmpty()) {
            return html + "<p class='pb-result wait'>Choose parts to see the compatibility check.</p>";
        }
        if (!results.isEmpty()) {
            html += "<table border='1'>";
            html += "<tr><th>Check</th><th>Result</th><th>Details</th></tr>";
            for (CompatibilityResult r : results) {
                String css = r.isFail() ? "pb-fail" : (CompatibilityResult.WARN.equals(r.getStatus()) ? "pb-warn" : "pb-pass");
                html += "<tr><td>" + PageHelper.esc(r.getRule()) + "</td>"
                        + "<td class='" + css + "'>" + r.getStatus() + "</td>"
                        + "<td>" + PageHelper.esc(r.getMessage()) + "</td></tr>";
            }
            html += "</table>";
        }

        String missing = build.getMissingRequired();
        if (!CompatibilityChecker.isCompatible(results)) {
            html += "<p class='pb-result bad'>INCOMPATIBLE: change the parts marked FAIL.</p>";
        } else if (!missing.isEmpty()) {
            html += "<p class='pb-result wait'>No problems so far. Still needed: " + PageHelper.esc(missing) + ".</p>";
        } else {
            html += "<p class='pb-result ok'>COMPATIBLE: every part works together.</p>";
        }
        return html;
    }

    // Totals and the "Place build order" / "Clear" buttons
    private String orderHtml(HttpSession session, PcBuild build, ArrayList<CompatibilityResult> results) {
        if (build.isEmpty()) {
            return "";
        }
        boolean staff = UserSession.isEmployee(session);
        String html = "<h3>" + (staff ? "Order" : "Save") + "</h3>";
        html += "<p>Estimated total: <b>PHP " + PageHelper.money(build.getTotal()) + "</b></p>";

        // what still blocks saving (customers) or ordering (staff)
        String blocker = "";
        if (staff && !BuilderSession.canOrder(session, build)) {
            blocker = "Type the customer's username above, or open one of their saved builds below.";
        } else if (!staff && !BuilderSession.canSave(session, build)) {
            blocker = "Log in as a customer to save this build for the cashier.";
        } else if (!build.getMissingRequired().isEmpty()) {
            blocker = "Choose the missing parts first: " + build.getMissingRequired() + ".";
        } else if (!CompatibilityChecker.isCompatible(results)) {
            blocker = "Fix the failed compatibility checks first.";
        }

        if (!blocker.isEmpty()) {
            html += "<p><i>" + PageHelper.esc(blocker) + "</i></p>";
        } else if (staff) {
            // EDITED: only staff place the order and take the payment, at the counter
            html += "<form action='do.checkbuild' method='POST' class='pb-inline'>"
                    + "<input type='hidden' name='action' value='checkout'>"
                    + "<button type='submit'>Place build order</button></form> ";
        } else {
            // customers save the build as a quote for the cashier
            String name = (build.getQuotename() == null) ? "My PC Build" : build.getQuotename();
            html += "<form action='do.checkbuild' method='POST'>"
                    + "<input type='hidden' name='action' value='savequote'>"
                    + "<label>Build name: </label><input type='text' name='quotename' maxlength='100' value='" + PageHelper.esc(name) + "'> "
                    + "<button type='submit'>" + (build.getQuoteid() == null ? "Save build for the cashier" : "Update saved build") + "</button>"
                    + "</form>"
                    + "<p><small>Saving does not reserve the parts or charge anything. "
                    + "The cashier checks today's prices and stock when you order.</small></p>";
        }
        html += "<form action='do.checkbuild' method='POST' class='pb-inline'>"
                + "<input type='hidden' name='action' value='clear'>"
                + "<input type='submit' class='btn-ghost' value='Clear build' onclick=\"return confirm('Remove every part?');\"></form>";
        return html;
    }

    // The customer's build orders and their status
    private String myOrdersHtml(int customerId) {
        String html = "<h3>Build orders</h3>";
        ArrayList<JobOrder> jobs;
        try {
            jobs = SqlcJobs.getJobOrdersForCustomer(customerId);
        } catch (SQLException e) {
            e.printStackTrace();
            return html + "<p>Could not load the build orders.</p>";
        }
        if (jobs.isEmpty()) {
            return html + "<p>No build orders yet.</p>";
        }
        html += "<table border='1'>";
        html += "<tr><th>Job</th><th>Build</th><th>Technician</th><th>Status</th><th>Ordered</th><th>Finished</th></tr>";
        for (JobOrder j : jobs) {
            html += "<tr><td>" + j.getJobNumber() + "</td>"
                    + "<td>" + PageHelper.esc(j.getBuildname()) + "</td>"
                    + "<td>" + PageHelper.esc(j.getTechnicianname()) + "</td>"
                    + "<td>" + PageHelper.esc(j.getStatus()) + "</td>"
                    + "<td>" + PageHelper.dateTime(j.getCreatedat()) + "</td>"
                    + "<td>" + PageHelper.dateTime(j.getCompletedat()) + "</td></tr>";
        }
        html += "</table>";
        return html;
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
        return "PC builder screen";
    }// </editor-fold>

}
