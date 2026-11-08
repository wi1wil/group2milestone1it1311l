/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import javax.servlet.http.HttpSession;

/**
 * Reads the logged-in user from the session.
 * authenticateuser.java saves these at login:
 *   user  = username
 *   role  = admin, cashier, inventory, technician or customer
 *   name  = first and last name
 *   e_id  = employee_id (staff only)
 *   c_id  = customer_id (customers only)
 *
 * @author Andrei
 */
public class UserSession {

    // employee_id of the logged-in staff member, or null when nobody (or a customer) is logged in
    public static Integer employeeId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute("e_id");
        if (id instanceof Integer) {
            return (Integer) id;
        }
        return null;
    }

    // full name of the logged-in user ("" when nobody is logged in)
    public static String name(HttpSession session) {
        if (session == null || session.getAttribute("name") == null) {
            return "";
        }
        return (String) session.getAttribute("name");
    }

    // role of the logged-in user ("" when nobody is logged in)
    public static String role(HttpSession session) {
        if (session == null || session.getAttribute("role") == null) {
            return "";
        }
        return (String) session.getAttribute("role");
    }

    public static boolean isAdmin(HttpSession session) {
        return "admin".equals(role(session));
    }

    // true when a staff member (any employee role) is logged in
    public static boolean isEmployee(HttpSession session) {
        return employeeId(session) != null;
    }
}
