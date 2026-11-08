/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import asia.uap.classes.Product;
import asia.uap.model.iosqlclass;

/**
 * Small helpers shared by the servlets that print HTML:
 * money format, safe text, dates, the one-time notification box and the navigation bar.
 *
 * @author Andrei
 */
public class PageHelper {

    // Tells phones to use the real screen width (same meta tag as index.jsp). Print it inside <head>.
    public static final String VIEWPORT = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";

    /*
     * The navigation bar links: { page, label, who may see it }.
     * Same links and roles as web/nav.jsp ("all" = everyone; admin sees every link).
     */
    private static final String[][] NAV_LINKS = {
        {"home.jsp", "Home", "all"},
        {"products.jsp", "Products", "all"},
        {"pcbuilder.jsp", "PC Builder", "all"},
        {"customer.jsp", "Customer", "customer"},
        {"transactions.jsp", "Transactions", "customer,cashier"},
        {"cashier.jsp", "Cashier", "cashier"},
        {"refund.jsp", "Refund", "cashier"},
        {"inventory.jsp", "Inventory", "inventory"},
        {"stock.jsp", "Stock", "inventory"},
        {"suppliers.jsp", "Suppliers", "inventory"},
        {"technician.jsp", "Technician", "technician"},
        {"admin.jsp", "Admin", "admin"},
        {"users.jsp", "Users", "admin"},
        {"reports.jsp", "Reports", "admin"}
    };

    /*
     * Servlet URLs -> the nav link that should look "active" on that page,
     * e.g. the purchase order page belongs to Inventory.
     */
    private static final String[][] NAV_SECTIONS = {
        {"do.processcashier", "cashier.jsp"}, {"do.finishcashier", "cashier.jsp"},
        {"do.refund", "refund.jsp"},
        {"do.checkbuild", "pcbuilder.jsp"},
        {"do.createpo", "inventory.jsp"}, {"do.purchaseorders", "inventory.jsp"}, {"do.addproduct", "inventory.jsp"},
        {"do.verifyspec", "inventory.jsp"},
        {"do.restock", "stock.jsp"}, {"do.stock", "stock.jsp"},
        {"do.suppliers", "suppliers.jsp"}, {"do.addsupplier", "suppliers.jsp"},
        {"do.updatejob", "technician.jsp"}, {"do.joborders", "technician.jsp"},
        {"do.transactions", "transactions.jsp"},
        {"do.products", "products.jsp"}, {"do.searchproduct", "products.jsp"}, {"do.buyproduct", "products.jsp"},
        {"do.users", "users.jsp"}, {"do.removeuser", "users.jsp"},
        {"do.reports", "reports.jsp"}, {"do.editprice", "admin.jsp"}, {"do.setpromo", "admin.jsp"}
    };

    /**
     * The navigation bar for pages made by servlets: the same HTML as web/nav.jsp
     * (servlets cannot include nav.jsp without a RequestDispatcher).
     * Print it right after <body>.
     */
    public static String nav(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String role = (session == null) ? null : (String) session.getAttribute("role");
        String name = (session == null) ? null : (String) session.getAttribute("name");

        // which link is "active": the page name, or the section a servlet URL belongs to
        String uri = request.getRequestURI();
        String page = uri.substring(uri.lastIndexOf('/') + 1);
        for (String[] section : NAV_SECTIONS) {
            if (section[0].equals(page)) {
                page = section[1];
            }
        }

        String html = "<nav>";
        html += "<a class=\"brand\" href=\"index.jsp\">Computer<span>Store</span></a>";
        html += "<div class=\"links\">";
        for (String[] link : NAV_LINKS) {
            if (canSee(role, link[2])) {
                html += "<a href=\"" + link[0] + "\"" + (link[0].equals(page) ? " class=\"active\"" : "") + ">"
                        + link[1] + "</a>";
            }
        }
        html += "</div>";

        html += "<div class=\"account\">";
        if (role == null) {
            html += "<a href=\"login.jsp\">Login</a>";
            html += "<a class=\"btn\" href=\"add.jsp\">Sign up</a>";
        } else {
            html += "<span>" + esc(name) + " <span class=\"badge\">" + esc(role) + "</span></span>";
            html += "<a class=\"btn btn-ghost\" href=\"do.logout\">Logout</a>";
        }
        html += "</div>";
        html += "</nav>";
        return html;
    }

    // Can this role see a link meant for "who" ("all", or a comma list of roles)? Admin sees everything.
    private static boolean canSee(String role, String who) {
        if (who.equals("all") || "admin".equals(role)) {
            return true;
        }
        if (role == null) {
            return false;
        }
        for (String r : who.split(",")) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }

    // css classes of the notification box (see css/style.css)
    public static final String SUCCESS = "success-notification";
    public static final String ERROR = "error-notification";
    public static final String INFO = "notification";

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * EDITED: the <img> tag of a product picture (products page, cashier, PC builder).
     * imageFile = products.image (a file name in images/productimg/), or null.
     * If the file is not in the folder, the browser switches to the "No image available" picture
     * (onerror), so a missing file never shows as a broken image.
     */
    public static String productImage(String imageFile, String altText, String cssClass) {
        String src = Product.isValidImageName(imageFile) ? Product.IMAGE_FOLDER + imageFile : Product.NO_IMAGE;
        return "<img src='" + esc(src) + "' alt='" + esc(altText) + "'"
                + (cssClass == null ? "" : " class='" + esc(cssClass) + "'")
                + " loading='lazy'"
                + " onerror=\"this.onerror=null;this.src='" + Product.NO_IMAGE + "'\">";
    }

    // Same as above for a Product object
    public static String productImage(Product p, String cssClass) {
        return productImage(p.getImage(), p.getName(), cssClass);
    }

    // makes text safe to print in HTML (same as iosqlclass.esc)
    public static String esc(String text) {
        return iosqlclass.esc(text);
    }

    // rounds money to 2 decimal places (centavos)
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // 12500.5 -> "12,500.50"
    public static String money(double value) {
        return String.format("%,.2f", value);
    }

    public static String dateTime(LocalDateTime value) {
        return (value == null) ? "-" : value.format(DATE_TIME);
    }

    public static String date(LocalDate value) {
        return (value == null) ? "-" : value.format(DATE);
    }

    /**
     * Saves a message that the NEXT page shows once (after a redirect).
     * cssClass = PageHelper.SUCCESS, ERROR or INFO.
     */
    public static void setMessage(HttpSession session, String cssClass, String text) {
        session.setAttribute("sysmsgclass", cssClass);
        session.setAttribute("sysmsgdesc", text);
    }

    /**
     * Returns the saved message as an HTML box and removes it from the session,
     * so it only shows once. Returns "" when there is no message.
     * The close button hides the box by itself (no need for script.js).
     */
    public static String popMessage(HttpSession session) {
        if (session == null || session.getAttribute("sysmsgdesc") == null) {
            return "";
        }
        String cssClass = (String) session.getAttribute("sysmsgclass");
        String text = (String) session.getAttribute("sysmsgdesc");
        session.removeAttribute("sysmsgclass");
        session.removeAttribute("sysmsgdesc");
        if (cssClass == null) {
            cssClass = INFO;
        }
        return "<div class='" + esc(cssClass) + "'>"
                + "<span>" + esc(text) + "</span>"
                + "<button class='close-btn' onclick=\"this.parentElement.style.display='none'\">&times;</button>"
                + "</div>";
    }
}
