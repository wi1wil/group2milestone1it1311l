/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.sql.*;
import java.util.ArrayList;
import asia.uap.classes.Product;
import asia.uap.classes.PurchaseOrder;
import asia.uap.classes.PurchaseOrderItem;

/**
 * Static SQL class for purchase orders (stock purchasing) and receiving stock.
 *
 * Flow:
 *   1. Inventory staff creates a purchase order (createpurchaseorder.java) -> status Ordered.
 *      New products in the order are added to products with status "pending" and 0 stock.
 *   2. When the delivery arrives, the admin approves it (receivestock.java) -> status Received.
 *      Stock goes up, and pending / out of stock products become "active".
 *   3. Or the admin cancels it -> status Cancelled, stock is not changed.
 *
 * Problems the user should see are thrown as IllegalArgumentException.
 *
 * @author Andrei
 */
public class SqlcPurchasing {

    /**
     * Saves a purchase order with all its lines (one transaction).
     * New-product lines are inserted into products first (status pending, stock 0).
     *
     * @return the new po_id
     */
    public static int savePurchaseOrder(int supplierId, int employeeId, ArrayList<PurchaseOrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one product to the purchase order.");
        }

        try (Connection conn = Sqlc.getConnection()) {
            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                // The supplier must exist
                try (PreparedStatement ps = conn.prepareStatement("SELECT supplier_id FROM suppliers WHERE supplier_id = ?")) {
                    ps.setInt(1, supplierId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("The chosen supplier was not found.");
                        }
                    }
                }

                // New products: insert them so the order lines can point to them
                for (PurchaseOrderItem item : items) {
                    if (item.isNewProduct()) {
                        item.setProductid(insertPendingProduct(conn, item));
                    }
                }

                // The purchase order (status starts as Ordered)
                int poId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO purchase_orders (supplier_id, created_by, order_date, status) "
                        + "VALUES (?, ?, CURDATE(), ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, supplierId);
                    ps.setInt(2, employeeId);
                    ps.setString(3, PurchaseOrder.STATUS_ORDERED);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { // grabs the new po_id
                        if (keys.next()) {
                            poId = keys.getInt(1);
                        }
                    }
                }
                if (poId == -1) {
                    throw new SQLException("No po_id was returned for the new purchase order.");
                }

                // The ordered products
                for (PurchaseOrderItem item : items) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO purchase_order_items (po_id, product_id, quantity, unit_cost) VALUES (?, ?, ?, ?)")) {
                        ps.setInt(1, poId);
                        ps.setInt(2, item.getProductid());
                        ps.setInt(3, item.getQuantity());
                        ps.setDouble(4, item.getUnitcost());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return poId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                // the new products were rolled back too, so forget their IDs
                for (PurchaseOrderItem item : items) {
                    if (item.getPoitemid() == null && item.getCategoryname() != null) {
                        item.setProductid(null);
                    }
                }
                throw e;
            }
        }
    }

    // Inserts a new product (status pending, 0 stock) and returns its product_id.
    // The category is found by name, or added when it is new (same as addproduct.java).
    private static int insertPendingProduct(Connection conn, PurchaseOrderItem item) throws SQLException {
        // The code must not be used yet (products.code is UNIQUE)
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM products WHERE code = ?")) {
            ps.setString(1, item.getCode());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Product code " + item.getCode()
                            + " is already used. Remove the line and add it as an existing product.");
                }
            }
        }

        // Find or add the category
        int categoryId = -1;
        try (PreparedStatement ps = conn.prepareStatement("SELECT category_id FROM categories WHERE name = ?")) {
            ps.setString(1, item.getCategoryname());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    categoryId = rs.getInt("category_id");
                }
            }
        }
        if (categoryId == -1) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, item.getCategoryname());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        categoryId = keys.getInt(1);
                    }
                }
            }
        }
        if (categoryId == -1) {
            throw new SQLException("No category_id was returned for the new category.");
        }

        // The product itself
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (category_id, code, name, brand, price, stock_quantity, status) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, categoryId);
            ps.setString(2, item.getCode());
            ps.setString(3, item.getName());
            if (item.getBrand() == null || item.getBrand().isEmpty()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, item.getBrand());
            }
            ps.setDouble(5, item.getSellingprice());
            ps.setString(6, Product.STATUS_PENDING);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No product_id was returned for the new product.");
    }

    /**
     * Lists purchase orders with their lines, newest first.
     * status = Ordered, Received, Cancelled, or null / "all" for every order.
     */
    public static ArrayList<PurchaseOrder> getPurchaseOrders(String status) throws SQLException {
        boolean showAll = (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("all"));
        ArrayList<PurchaseOrder> list = new ArrayList<>();
        try (Connection conn = Sqlc.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT po.po_id, po.supplier_id, s.name AS supplier, po.created_by, "
                    + "CONCAT(e.first_name, ' ', e.last_name) AS created_by_name, po.order_date, po.status "
                    + "FROM purchase_orders po "
                    + "JOIN suppliers s ON po.supplier_id = s.supplier_id "
                    + "JOIN employees e ON po.created_by = e.employee_id "
                    + (showAll ? "" : "WHERE po.status = ? ")
                    + "ORDER BY po.po_id DESC")) {
                if (!showAll) {
                    ps.setString(1, status.trim());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(readPurchaseOrder(rs));
                    }
                }
            }
            // the lines of each order
            for (PurchaseOrder po : list) {
                loadItems(conn, po);
            }
        }
        return list;
    }

    // One purchase order with its lines, or null
    public static PurchaseOrder getPurchaseOrder(int poId) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            PurchaseOrder po = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT po.po_id, po.supplier_id, s.name AS supplier, po.created_by, "
                    + "CONCAT(e.first_name, ' ', e.last_name) AS created_by_name, po.order_date, po.status "
                    + "FROM purchase_orders po "
                    + "JOIN suppliers s ON po.supplier_id = s.supplier_id "
                    + "JOIN employees e ON po.created_by = e.employee_id "
                    + "WHERE po.po_id = ?")) {
                ps.setInt(1, poId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        po = readPurchaseOrder(rs);
                    }
                }
            }
            if (po != null) {
                loadItems(conn, po);
            }
            return po;
        }
    }

    private static PurchaseOrder readPurchaseOrder(ResultSet rs) throws SQLException {
        Date date = rs.getDate("order_date");
        return new PurchaseOrder(rs.getInt("po_id"), rs.getInt("supplier_id"), rs.getString("supplier"),
                rs.getInt("created_by"), rs.getString("created_by_name"),
                (date == null) ? null : date.toLocalDate(), rs.getString("status"));
    }

    private static void loadItems(Connection conn, PurchaseOrder po) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT i.po_item_id, i.product_id, p.code, p.name, i.quantity, i.unit_cost "
                + "FROM purchase_order_items i JOIN products p ON i.product_id = p.product_id "
                + "WHERE i.po_id = ? ORDER BY i.po_item_id")) {
            ps.setInt(1, po.getPoid());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    po.getItems().add(new PurchaseOrderItem(rs.getInt("po_item_id"), rs.getInt("product_id"),
                            rs.getString("code"), rs.getString("name"), rs.getInt("quantity"), rs.getDouble("unit_cost")));
                }
            }
        }
    }

    /**
     * Admin approval of a delivery (one transaction):
     *   - the order must still be Ordered (locked with FOR UPDATE so it cannot be received twice)
     *   - each line's quantity is added to the product's stock
     *   - pending (new) and out of stock products become active
     *   - the order becomes Received
     *
     * @return the received order (with its lines)
     */
    public static PurchaseOrder receivePurchaseOrder(int poId) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                lockOrderedPo(conn, poId);

                PurchaseOrder po = new PurchaseOrder(poId, null, null, null, null, null, PurchaseOrder.STATUS_RECEIVED);
                loadItems(conn, po);
                if (po.getItems().isEmpty()) {
                    throw new IllegalArgumentException("Purchase order " + po.getPoNumber() + " has no products.");
                }

                for (PurchaseOrderItem item : po.getItems()) {
                    // more stock
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?")) {
                        ps.setInt(1, item.getQuantity());
                        ps.setInt(2, item.getProductid());
                        ps.executeUpdate();
                    }
                    // new or sold-out products can be sold again (inactive ones stay inactive)
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE products SET status = ? WHERE product_id = ? AND status IN (?, ?)")) {
                        ps.setString(1, Product.STATUS_ACTIVE);
                        ps.setInt(2, item.getProductid());
                        ps.setString(3, Product.STATUS_PENDING);
                        ps.setString(4, Product.STATUS_OUTOFSTOCK);
                        ps.executeUpdate();
                    }
                }

                setStatus(conn, poId, PurchaseOrder.STATUS_RECEIVED);
                conn.commit();
                return po;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Admin cancels an order that was not received. Stock is not changed.
    public static void cancelPurchaseOrder(int poId) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                lockOrderedPo(conn, poId);
                setStatus(conn, poId, PurchaseOrder.STATUS_CANCELLED);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Locks the order row and checks that it is still waiting (Ordered)
    private static void lockOrderedPo(Connection conn, int poId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM purchase_orders WHERE po_id = ? FOR UPDATE")) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Purchase order " + String.format("PO-%03d", poId) + " was not found.");
                }
                String status = rs.getString("status");
                if (!PurchaseOrder.STATUS_ORDERED.equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("Purchase order " + String.format("PO-%03d", poId)
                            + " is already " + status + ".");
                }
            }
        }
    }

    private static void setStatus(Connection conn, int poId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE purchase_orders SET status = ? WHERE po_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, poId);
            ps.executeUpdate();
        }
    }
}
