package uap.asia.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Group 2
 */
// Checks every request BEFORE the page or servlet runs: who may open what.
// Logged-out visitors only reach the customer-facing pages; admin may open everything.
@WebFilter(filterName = "accessfilter", urlPatterns = {"/*"})
public class accessfilter implements Filter {

    // page or servlet -> who may use it
    // "public" = everyone, "login" = any logged-in user, otherwise a list of roles
    private static final Map<String, String> RULES = new HashMap<>();

    static {
        // Open to everyone (also logged out)
        String[] open = {"/", "/index.jsp", "/home.jsp", "/login.jsp", "/add.jsp", "/nav.jsp",
            "/products.jsp", "/pcbuilder.jsp", "/do.products", "/do.searchproduct", "/do.checkbuild",
            "/do.authenticate", "/do.register", "/do.logout"};
        for (String p : open) {
            RULES.put(p, "public");
        }

        // Customer pages
        RULES.put("/customer.jsp", "customer");
        RULES.put("/do.buyproduct", "customer");
        RULES.put("/transactions.jsp", "customer,cashier");
        RULES.put("/do.transactions", "customer,cashier");

        // Cashier pages
        RULES.put("/cashier.jsp", "cashier");
        RULES.put("/old_cashier.jsp", "cashier");
        RULES.put("/do.donecashier", "cashier");
        RULES.put("/refund.jsp", "cashier");
        RULES.put("/do.refund", "cashier");

        // Inventory pages
        String[] inventory = {"/inventory.jsp", "/stock.jsp", "/suppliers.jsp", "/verifyspec.jsp",
            "/do.addproduct", "/do.addsupplier", "/do.createpo", "/do.restock", "/do.stock",
            "/do.suppliers", "/do.purchaseorders", "/do.verifyspec"};
        for (String p : inventory) {
            RULES.put(p, "inventory");
        }

        // Technician pages (customers may also check their own job orders)
        RULES.put("/technician.jsp", "technician");
        RULES.put("/do.updatejob", "technician");
        RULES.put("/do.joborders", "technician,customer");

        // Admin only: users, reports, prices, promos
        String[] admin = {"/admin.jsp", "/users.jsp", "/addemployee.jsp", "/reports.jsp",
            "/do.users", "/do.removeuser", "/do.reports", "/do.editprice", "/do.setpromo"};
        for (String p : admin) {
            RULES.put(p, "admin");
        }
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // The page name without the project name, e.g. /inventory.jsp
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Styles and pictures are always allowed
        if (path.startsWith("/css/") || path.startsWith("/images/")) {
            chain.doFilter(req, res);
            return;
        }

        // Pages that are not in the list are treated as admin only
        String allowed = RULES.get(path);
        if (allowed == null) {
            allowed = "admin";
        }
        if (allowed.equals("public")) {
            chain.doFilter(req, res);
            return;
        }

        // Who is logged in? (role stays null when nobody is)
        HttpSession session = request.getSession(false);
        String role = null;
        if (session != null) {
            role = (String) session.getAttribute("role");
        }

        // Logged out: go to the login page
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // Any logged-in user, admin, or a role in the list
        boolean ok = allowed.equals("login") || role.equals("admin");
        for (String r : allowed.split(",")) {
            if (r.equals(role)) {
                ok = true;
            }
        }
        if (ok) {
            chain.doFilter(req, res);
            return;
        }

        // Logged in, but this role may not open it
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Access denied</title>");
            out.println("<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Access denied</h1>");
            out.println("<p>Your role (" + iosqlclass.esc(role) + ") cannot open this page.</p>");
            out.println("<p><a href='index.jsp'>Back to Index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    public void destroy() {
    }
}
