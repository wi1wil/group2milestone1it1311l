/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import javax.servlet.http.HttpSession;
import asia.uap.classes.PcBuild;

/**
 * Reads the PC build from the session (attribute "pcBuild") for pcbuilder.java and checkbuild.java.
 *
 * Who the build is for:
 *   - logged-in customer: always themselves (c_id), set automatically
 *   - staff (cashier, admin, ...): the customer whose username they typed, or whose quote they loaded
 *   - logged out: nobody (parts can be picked and checked, but not saved or ordered)
 *
 * EDITED: the store has no online orders. Customers SAVE a build as a quote;
 * only staff can place (and charge) the order at the counter.
 *
 * @author Andrei
 */
public class BuilderSession {

    public static PcBuild getBuild(HttpSession session) {
        PcBuild build = (PcBuild) session.getAttribute("pcBuild");
        if (build == null) {
            build = new PcBuild();
            session.setAttribute("pcBuild", build);
        }

        if (isCustomer(session)) {
            Integer customerId = customerId(session);
            if (customerId != null && !customerId.equals(build.getCustomerid())) {
                build.setCustomer(customerId, UserSession.name(session));
                build.setQuote(null, null); // a quote of another customer is not theirs to update
            }
        } else if (!UserSession.isEmployee(session) && build.getCustomerid() != null) {
            // logged out: forget the customer and their quote
            build.setCustomer(null, null);
            build.setQuote(null, null);
        }
        return build;
    }

    // customer_id of the logged-in customer, or null
    public static Integer customerId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute("c_id");
        return (id instanceof Integer) ? (Integer) id : null;
    }

    public static boolean isCustomer(HttpSession session) {
        return "customer".equals(UserSession.role(session));
    }

    // true when the user may save the build as a quote: a logged-in customer
    public static boolean canSave(HttpSession session, PcBuild build) {
        return isCustomer(session) && build.getCustomerid() != null;
    }

    // true when the user may place (and charge) the order: staff with a customer chosen
    public static boolean canOrder(HttpSession session, PcBuild build) {
        return UserSession.isEmployee(session) && build.getCustomerid() != null;
    }
}
