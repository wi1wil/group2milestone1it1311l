/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.Sqlc;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.SqlcPurchasing;
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
import asia.uap.classes.Product;
import asia.uap.classes.PurchaseOrderItem;
import asia.uap.classes.Supplier;

/**
 *
 * @author Group 2
 */
// Inventory staff creates a purchase order to replenish stock. URL: /do.createpo
//
// Works like the cashier cart: the order is prepared in the session first, then submitted.
//   Session attributes:  poDraft      = ArrayList<PurchaseOrderItem> (the lines)
//                        poSupplierId = Integer (the chosen supplier)
//
// GET  shows the page: supplier, low stock suggestions, the draft lines and the forms.
// POST action = supplier -> choose the supplier
//               add      -> add an existing product (ID or code, quantity, unit cost)
//               addnew   -> add a NEW product (new item/variety; saved as "pending" when submitted)
//               remove   -> remove one line (index)
//               clear    -> throw away the draft
//               submit   -> save purchase_orders + purchase_order_items (status Ordered)
// Every POST saves a message and goes back to this page (redirect), so a page refresh
// does not send the form again.
//
// After submitting, the admin approves the delivery on the receive stock page (receivestock.java, /do.restock).
@WebServlet(name = "createpurchaseorder", urlPatterns = {"/do.createpo"})
public class createpurchaseorder extends HttpServlet {

    private static final int MAX_QTY = 10000;

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
        // The database keeps which employee created the order, so the user must be logged in
        HttpSession session = request.getSession(false);
        Integer employeeId = UserSession.employeeId(session);
        if (employeeId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        ArrayList<PurchaseOrderItem> draft = getDraft(session);

        // POST: change the draft, then come back to this page
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String action = request.getParameter("action");
            if (action == null) {
                action = "";
            }
            switch (action) {
                case "supplier":
                    chooseSupplier(request, session);
                    break;
                case "add":
                    addExisting(request, session, draft);
                    break;
                case "addnew":
                    addNew(request, session, draft);
                    break;
                case "remove":
                    Integer index = ParseThis.toInt(request.getParameter("index"));
                    if (index != null && index >= 0 && index < draft.size()) {
                        PurchaseOrderItem removed = draft.remove((int) index);
                        PageHelper.setMessage(session, PageHelper.SUCCESS, "Removed " + removed.getName() + ".");
                    }
                    break;
                case "clear":
                    session.removeAttribute("poDraft");
                    session.removeAttribute("poSupplierId");
                    PageHelper.setMessage(session, PageHelper.INFO, "Purchase order draft cleared.");
                    break;
                case "submit":
                    submit(session, employeeId, draft);
                    break;
                default:
                    PageHelper.setMessage(session, PageHelper.ERROR, "Unknown action.");
            }
            response.sendRedirect("do.createpo");
            return;
        }

        // GET: show the page
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Create Purchase Order</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp
            out.println("<h1>Create Purchase Order</h1>");
            out.println(PageHelper.popMessage(session));

            // Step 1: supplier
            Integer supplierId = (Integer) session.getAttribute("poSupplierId");
            ArrayList<Supplier> suppliers = Sqlc.getSuppliers();
            out.println("<h3>1. Supplier</h3>");
            if (suppliers.isEmpty()) {
                out.println("<p>No suppliers yet. Add one on the <a href='suppliers.jsp'>Suppliers</a> page first.</p>");
            } else {
                out.println("<form action='do.createpo' method='POST'>");
                out.println("<input type='hidden' name='action' value='supplier'>");
                out.println("<select name='supplierid'>");
                for (Supplier s : suppliers) {
                    boolean chosen = s.getSupplierid().equals(supplierId);
                    out.println("<option value='" + s.getSupplierid() + "'" + (chosen ? " selected" : "") + ">"
                            + PageHelper.esc(s.getName())
                            + (s.getContact() == null ? "" : " (" + PageHelper.esc(s.getContact()) + ")")
                            + "</option>");
                }
                out.println("</select>");
                out.println("<input type='submit' value='Use this supplier'>");
                out.println("</form>");
                if (supplierId != null) {
                    Supplier chosen = Sqlc.getSupplier(supplierId);
                    out.println("<p>Chosen: <b>" + (chosen == null ? "(not found)" : PageHelper.esc(chosen.getName())) + "</b></p>");
                }
            }

            // Step 2: products to order
            out.println("<h3>2. Add products</h3>");
            out.println("<h4>Low stock (" + Sqlc.LOW_STOCK + " or fewer left)</h4>");
            out.println(lowStockTable());

            out.println("<h4>Existing product</h4>");
            out.println("<form action='do.createpo' method='POST'>");
            out.println("<input type='hidden' name='action' value='add'>");
            out.println("Product ID or code: <input type='text' name='productid'>");
            out.println("Quantity: <input type='text' name='quantity' value='10'>");
            out.println("Unit cost: <input type='text' name='unitcost'> <small>(blank = current selling price)</small>");
            out.println("<input type='submit' value='Add to order'>");
            out.println("</form>");

            out.println("<h4>New product (new item or variety)</h4>");
            out.println("<p><small>It is added to the products list as <b>pending</b> (not for sale) "
                    + "and becomes active when the stock is received.</small></p>");
            out.println("<form action='do.createpo' method='POST'>");
            out.println("<input type='hidden' name='action' value='addnew'>");
            out.println("<p>Code: <input type='text' name='code'> Name: <input type='text' name='name'></p>");
            out.println("<p>Category: <input type='text' name='category' list='categories'> Brand: <input type='text' name='brand'></p>");
            out.println(categoryList());
            out.println("<p>Selling price: <input type='text' name='price'> Quantity: <input type='text' name='quantity' value='10'>"
                    + " Unit cost: <input type='text' name='unitcost'></p>");
            out.println("<input type='submit' value='Add new product to order'>");
            out.println("</form>");

            // Step 3: the draft and submit
            out.println("<h3>3. Purchase order lines</h3>");
            out.println(draftTable(draft));
            if (!draft.isEmpty()) {
                out.println("<form action='do.createpo' method='POST' style='display:inline'>");
                out.println("<input type='hidden' name='action' value='submit'>");
                out.println("<input type='submit' value='Submit purchase order'>");
                out.println("</form> &nbsp; ");
                out.println("<form action='do.createpo' method='POST' style='display:inline'>");
                out.println("<input type='hidden' name='action' value='clear'>");
                out.println("<input type='submit' value='Clear' onclick=\"return confirm('Throw away this draft?');\">");
                out.println("</form>");
            }

            out.println("<p><a href='do.purchaseorders?status=all'>View purchase orders</a> | "
                    + "<a href='do.restock'>Receive stock</a> | <a href='inventory.jsp'>Back to Inventory</a></p>"
                    + "<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // action=supplier
    private void chooseSupplier(HttpServletRequest request, HttpSession session) {
        Integer supplierId = ParseThis.toInt(request.getParameter("supplierid"));
        Supplier s = (supplierId == null) ? null : Sqlc.getSupplier(supplierId);
        if (s == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Supplier not found.");
            return;
        }
        session.setAttribute("poSupplierId", s.getSupplierid());
        PageHelper.setMessage(session, PageHelper.SUCCESS, "Supplier set to " + s.getName() + ".");
    }

    // action=add: an existing product
    private void addExisting(HttpServletRequest request, HttpSession session, ArrayList<PurchaseOrderItem> draft) {
        // Read the form fields
        String id = request.getParameter("productid");
        Integer qty = readQuantity(request, session);
        if (qty == null) {
            return;
        }

        Product p = Sqlc.findProduct(id);
        if (p == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Product " + (id == null ? "" : id.trim())
                    + " was not found. Use the New product form for new items.");
            return;
        }
        if (Product.STATUS_INACTIVE.equalsIgnoreCase(p.getStatus())) {
            PageHelper.setMessage(session, PageHelper.ERROR, p.getName() + " is inactive (removed from sale).");
            return;
        }

        // Unit cost: blank = the current selling price (same as the old version)
        Double cost = readMoney(request.getParameter("unitcost"), p.getPrice(), "Unit cost", session);
        if (cost == null) {
            return;
        }

        // purchase_order_items allows one row per product, so the same product adds to its line
        for (PurchaseOrderItem item : draft) {
            if (p.getProductid().equals(item.getProductid())) {
                item.setQuantity(item.getQuantity() + qty);
                session.setAttribute("poDraft", draft);
                PageHelper.setMessage(session, PageHelper.SUCCESS, p.getName() + " is now " + item.getQuantity() + " in the order.");
                return;
            }
        }
        draft.add(new PurchaseOrderItem(null, p.getProductid(), p.getCode(), p.getName(), qty, cost));
        session.setAttribute("poDraft", draft);
        PageHelper.setMessage(session, PageHelper.SUCCESS, "Added " + qty + " x " + p.getName() + ".");
    }

    // action=addnew: a product that is not in the products table yet
    private void addNew(HttpServletRequest request, HttpSession session, ArrayList<PurchaseOrderItem> draft) {
        // Read the form fields
        String code = trim(request.getParameter("code"));
        String name = trim(request.getParameter("name"));
        String category = trim(request.getParameter("category"));
        String brand = trim(request.getParameter("brand"));

        // Validate (lengths follow the products and categories columns)
        if (code.isEmpty() || name.isEmpty() || category.isEmpty()) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Code, name and category are required for a new product.");
            return;
        }
        if (code.length() > 50 || name.length() > 100 || category.length() > 50 || brand.length() > 50) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Text is too long (code 50, name 100, category 50, brand 50 characters).");
            return;
        }
        if (Sqlc.getProductByCode(code) != null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Code " + code + " already exists. Add it as an existing product.");
            return;
        }
        for (PurchaseOrderItem item : draft) {
            if (code.equalsIgnoreCase(item.getCode())) {
                PageHelper.setMessage(session, PageHelper.ERROR, "Code " + code + " is already in this order.");
                return;
            }
        }
        Integer qty = readQuantity(request, session);
        if (qty == null) {
            return;
        }
        Double price = readMoney(request.getParameter("price"), null, "Selling price", session);
        if (price == null) {
            return;
        }
        Double cost = readMoney(request.getParameter("unitcost"), null, "Unit cost", session);
        if (cost == null) {
            return;
        }

        draft.add(new PurchaseOrderItem(code, name, category, brand, price, qty, cost));
        session.setAttribute("poDraft", draft);
        PageHelper.setMessage(session, PageHelper.SUCCESS, "Added new product " + name + " (" + qty + " pcs).");
    }

    // action=submit: save the order to the database
    private void submit(HttpSession session, int employeeId, ArrayList<PurchaseOrderItem> draft) {
        Integer supplierId = (Integer) session.getAttribute("poSupplierId");
        if (supplierId == null) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Choose a supplier first.");
            return;
        }
        if (draft.isEmpty()) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Add at least one product first.");
            return;
        }
        try {
            int poId = SqlcPurchasing.savePurchaseOrder(supplierId, employeeId, draft);
            double total = 0;
            for (PurchaseOrderItem item : draft) {
                total += item.getSubtotal();
            }
            // saved: the draft is finished
            session.removeAttribute("poDraft");
            session.removeAttribute("poSupplierId");
            PageHelper.setMessage(session, PageHelper.SUCCESS, "Purchase order " + String.format("PO-%03d", poId)
                    + " created (" + draft.size() + " line(s), PHP " + PageHelper.money(total)
                    + "). Status: Ordered, waiting for admin approval when the stock arrives.");
        } catch (IllegalArgumentException e) {
            PageHelper.setMessage(session, PageHelper.ERROR, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            PageHelper.setMessage(session, PageHelper.ERROR, "Could not save to the database. Please try again.");
        }
    }

    // Reads the "quantity" field. Saves an error message and returns null when it is invalid.
    private Integer readQuantity(HttpServletRequest request, HttpSession session) {
        String qtyText = request.getParameter("quantity");
        if (!ParseThis.isInt(qtyText) || ParseThis.toInt(qtyText) <= 0 || ParseThis.toInt(qtyText) > MAX_QTY) {
            PageHelper.setMessage(session, PageHelper.ERROR, "Quantity must be a whole number from 1 to " + MAX_QTY + ".");
            return null;
        }
        return ParseThis.toInt(qtyText);
    }

    // Reads a peso amount. Blank gives blankValue (or an error when blankValue is null).
    private Double readMoney(String text, Double blankValue, String label, HttpSession session) {
        if (ParseThis.isBlank(text)) {
            if (blankValue == null) {
                PageHelper.setMessage(session, PageHelper.ERROR, label + " is required.");
            }
            return blankValue;
        }
        if (!ParseThis.isDouble(text) || ParseThis.toDouble(text) <= 0 || ParseThis.toDouble(text) >= 100000000) {
            PageHelper.setMessage(session, PageHelper.ERROR, label + " must be a number greater than 0.");
            return null;
        }
        return PageHelper.round2(ParseThis.toDouble(text));
    }

    private String trim(String text) {
        return (text == null) ? "" : text.trim();
    }

    // The draft in the session (a new empty one is saved when there is none yet)
    private ArrayList<PurchaseOrderItem> getDraft(HttpSession session) {
        ArrayList<PurchaseOrderItem> draft = (ArrayList<PurchaseOrderItem>) session.getAttribute("poDraft");
        if (draft == null) {
            draft = new ArrayList<>();
            session.setAttribute("poDraft", draft);
        }
        return draft;
    }

    // Low stock products, each with a quick "add" form
    private String lowStockTable() {
        ArrayList<Product> low = Sqlc.getLowStockProducts();
        if (low.isEmpty()) {
            return "<p>No products are low on stock.</p>";
        }
        String html = "<table border='1'>";
        html += "<tr><th>ID</th><th>Code</th><th>Name</th><th>Stock</th><th>Status</th><th>Order</th></tr>";
        for (Product p : low) {
            html += "<tr><td>" + p.getProductid() + "</td>"
                    + "<td>" + PageHelper.esc(p.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(p.getName()) + "</td>"
                    + "<td>" + p.getStockquantity() + "</td>"
                    + "<td>" + PageHelper.esc(p.getStatus()) + "</td>"
                    + "<td><form action='do.createpo' method='POST'>"
                    + "<input type='hidden' name='action' value='add'>"
                    + "<input type='hidden' name='productid' value='" + p.getProductid() + "'>"
                    + "<input type='text' name='quantity' value='10' size='4'>"
                    + "<input type='submit' value='Add'>"
                    + "</form></td></tr>";
        }
        html += "</table>";
        return html;
    }

    // Existing category names as suggestions for the category box (a new name is also allowed)
    private String categoryList() {
        String html = "<datalist id='categories'>";
        for (String name : Sqlc.getCategoryNames()) {
            html += "<option value='" + PageHelper.esc(name) + "'>";
        }
        html += "</datalist>";
        return html;
    }

    // The draft lines with a Remove button each
    private String draftTable(ArrayList<PurchaseOrderItem> draft) {
        if (draft.isEmpty()) {
            return "<p>No products in this order yet.</p>";
        }
        String html = "<table border='1'>";
        html += "<tr><th>#</th><th>Code</th><th>Product</th><th>Qty</th><th>Unit cost</th><th>Subtotal</th><th></th></tr>";
        double total = 0;
        for (int i = 0; i < draft.size(); i++) {
            PurchaseOrderItem item = draft.get(i);
            total += item.getSubtotal();
            html += "<tr><td>" + (i + 1) + "</td>"
                    + "<td>" + PageHelper.esc(item.getCode()) + "</td>"
                    + "<td>" + PageHelper.esc(item.getName())
                    + (item.isNewProduct() ? " <b>(NEW, " + PageHelper.esc(item.getCategoryname())
                            + ", sells at " + PageHelper.money(item.getSellingprice()) + ")</b>" : "") + "</td>"
                    + "<td>" + item.getQuantity() + "</td>"
                    + "<td>" + PageHelper.money(item.getUnitcost()) + "</td>"
                    + "<td>" + PageHelper.money(item.getSubtotal()) + "</td>"
                    + "<td><form action='do.createpo' method='POST'>"
                    + "<input type='hidden' name='action' value='remove'>"
                    + "<input type='hidden' name='index' value='" + i + "'>"
                    + "<input type='submit' value='Remove'>"
                    + "</form></td></tr>";
        }
        html += "</table>";
        html += "<p><b>Total cost: PHP " + PageHelper.money(total) + "</b></p>";
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
        return "Create purchase order";
    }// </editor-fold>

}
