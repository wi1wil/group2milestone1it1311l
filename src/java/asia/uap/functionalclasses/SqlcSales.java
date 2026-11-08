/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.sql.*;
import java.util.ArrayList;
import asia.uap.classes.CartItem;
import asia.uap.classes.Product;
import asia.uap.classes.Sale;
import asia.uap.classes.SaleItem;

/**
 * Static SQL class for sales and refunds.
 *
 * Saving a sale or a refund changes several tables, so each one runs as ONE
 * database transaction: either every change is saved, or none of them.
 *
 * Problems the user should see (not enough stock, too many refunded, ...)
 * are thrown as IllegalArgumentException with a readable message.
 * Database problems are thrown as SQLException.
 *
 * @author Andrei
 */
public class SqlcSales {

    /**
     * Saves a paid sale: sales row, one sale_items row per cart line, and less stock.
     *
     * Steps (all in one transaction):
     *   1. Lock each product row (FOR UPDATE) and check it can still be sold
     *      (another cashier may have sold the last unit after it was added to this cart).
     *   2. Insert the sales row (discount = all line discounts, total = amount due).
     *   3. Insert the sale_items rows (unit_price = price paid for one unit after the discount).
     *   4. Take the quantities out of stock. A product that reaches 0 becomes "outofstock".
     *
     * @return the new sale_id
     */
    public static int saveSale(int employeeId, int customerId, ArrayList<CartItem> cart,
            double amountPaid, String paymentMethod) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                int saleId = insertSale(conn, employeeId, customerId, cart, amountPaid, paymentMethod);
                conn.commit();
                return saleId;
            } catch (SQLException | RuntimeException e) {
                // rolls back if something goes wrong, then lets the servlet show the problem
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Steps 1 to 4 of saveSale on a connection that is ALREADY in a transaction.
     * It does not commit or roll back: the caller does. Used by saveSale, and by
     * SqlcBuilds.placeBuildOrder so the sale, the build and the job order are saved together.
     *
     * @return the new sale_id
     */
    public static int insertSale(Connection conn, int employeeId, int customerId, ArrayList<CartItem> cart,
            double amountPaid, String paymentMethod) throws SQLException {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty.");
        }

        // Totals from the cart
        double discount = 0;
        double total = 0;
        for (CartItem item : cart) {
            discount += item.getTotalDiscount();
            total += item.getSubtotal();
        }
        discount = PageHelper.round2(discount);
        total = PageHelper.round2(total);
        if (PageHelper.round2(amountPaid) < total) {
            throw new IllegalArgumentException("Payment is short by PHP " + PageHelper.money(total - amountPaid) + ".");
        }

        // 1. Lock and re-check every product
        for (CartItem item : cart) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, stock_quantity, status FROM products WHERE product_id = ? FOR UPDATE")) {
                ps.setInt(1, item.getProductid());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Product " + item.getCode() + " no longer exists.");
                    }
                    if (!Product.STATUS_ACTIVE.equalsIgnoreCase(rs.getString("status"))) {
                        throw new IllegalArgumentException(rs.getString("name") + " can no longer be sold.");
                    }
                    int stock = rs.getInt("stock_quantity");
                    if (stock < item.getItemquantity()) {
                        throw new IllegalArgumentException("Only " + stock + " of " + rs.getString("name")
                                + " left in stock. Remove it or lower the quantity.");
                    }
                }
            }
        }

        // 2. The sale
        int saleId = -1;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sales (employee_id, customer_id, sale_date, discount, total, amount_paid, payment_method) "
                + "VALUES (?, ?, NOW(), ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, customerId);
            ps.setDouble(3, discount);
            ps.setDouble(4, total);
            ps.setDouble(5, PageHelper.round2(amountPaid));
            ps.setString(6, paymentMethod);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { // grabs the new sale_id
                if (keys.next()) {
                    saleId = keys.getInt(1);
                }
            }
        }
        if (saleId == -1) {
            throw new SQLException("No sale_id was returned for the new sale.");
        }

        for (CartItem item : cart) {
            // 3. The sold product
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, saleId);
                ps.setInt(2, item.getProductid());
                ps.setInt(3, item.getItemquantity());
                ps.setDouble(4, item.getPrice());
                ps.executeUpdate();
            }

            // 4. Less stock
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?")) {
                ps.setInt(1, item.getItemquantity());
                ps.setInt(2, item.getProductid());
                ps.executeUpdate();
            }
            // nothing left = out of stock (it becomes active again when restocked or refunded)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET status = ? WHERE product_id = ? AND stock_quantity <= 0 AND status = ?")) {
                ps.setString(1, Product.STATUS_OUTOFSTOCK);
                ps.setInt(2, item.getProductid());
                ps.setString(3, Product.STATUS_ACTIVE);
                ps.executeUpdate();
            }
        }

        return saleId;
    }

    /**
     * Loads one sale with its items and how many of each were already refunded.
     * Returns null when there is no sale with that ID.
     */
    public static Sale getSale(int saleId) throws SQLException {
        Sale sale = null;
        try (Connection conn = Sqlc.getConnection()) {

            // The sale, the cashier and the customer
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT s.sale_id, s.employee_id, s.customer_id, s.sale_date, s.discount, s.total, "
                    + "s.amount_paid, s.payment_method, "
                    + "CONCAT(e.first_name, ' ', e.last_name) AS cashier, "
                    + "CONCAT(c.first_name, ' ', c.last_name) AS customer "
                    + "FROM sales s "
                    + "JOIN employees e ON s.employee_id = e.employee_id "
                    + "JOIN customers c ON s.customer_id = c.customer_id "
                    + "WHERE s.sale_id = ?")) {
                ps.setInt(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Timestamp date = rs.getTimestamp("sale_date");
                        sale = new Sale(rs.getInt("sale_id"), rs.getInt("employee_id"), rs.getString("cashier"),
                                rs.getInt("customer_id"), rs.getString("customer"),
                                (date == null) ? null : date.toLocalDateTime(),
                                rs.getDouble("discount"), rs.getDouble("total"),
                                rs.getDouble("amount_paid"), rs.getString("payment_method"));
                    }
                }
            }
            if (sale == null) {
                return null;
            }

            // The items. The sub-SELECT adds up what was already refunded for each line.
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT si.sale_item_id, si.sale_id, si.product_id, p.code, p.name, si.quantity, si.unit_price, "
                    + "COALESCE((SELECT SUM(r.quantity) FROM refunds r WHERE r.sale_item_id = si.sale_item_id), 0) AS refunded "
                    + "FROM sale_items si JOIN products p ON si.product_id = p.product_id "
                    + "WHERE si.sale_id = ? ORDER BY si.sale_item_id")) {
                ps.setInt(1, saleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sale.getItems().add(new SaleItem(rs.getInt("sale_item_id"), rs.getInt("sale_id"),
                                rs.getInt("product_id"), rs.getString("code"), rs.getString("name"),
                                rs.getInt("quantity"), rs.getDouble("unit_price"), rs.getInt("refunded")));
                    }
                }
            }
        }
        return sale;
    }

    /**
     * Refunds part (or all) of one sale_items line.
     *
     * Steps (all in one transaction):
     *   1. Lock the sale_items row so two cashiers cannot refund the same item at once.
     *   2. Check the line belongs to this sale and that enough are left to refund.
     *   3. Insert the refunds row (refund_amount = unit price paid x quantity).
     *   4. If restock is true, put the items back in stock (an out of stock product becomes active).
     *      Use restock = false for damaged items that cannot be sold again.
     *
     * @return { refund_id, product name, refund amount }
     */
    public static String[] saveRefund(int saleId, int saleItemId, int employeeId, int qty,
            String reason, boolean restock) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be a whole number of at least 1.");
        }

        try (Connection conn = Sqlc.getConnection()) {
            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                // 1 + 2. The sold line, locked
                int productId;
                int sold;
                double unitPrice;
                String name;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT si.product_id, si.quantity, si.unit_price, p.name FROM sale_items si "
                        + "JOIN products p ON si.product_id = p.product_id "
                        + "WHERE si.sale_item_id = ? AND si.sale_id = ? FOR UPDATE")) {
                    ps.setInt(1, saleItemId);
                    ps.setInt(2, saleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("That item is not part of sale " + saleId + ".");
                        }
                        productId = rs.getInt("product_id");
                        sold = rs.getInt("quantity");
                        unitPrice = rs.getDouble("unit_price");
                        name = rs.getString("name");
                    }
                }

                int refunded = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(SUM(quantity), 0) AS refunded FROM refunds WHERE sale_item_id = ?")) {
                    ps.setInt(1, saleItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            refunded = rs.getInt("refunded");
                        }
                    }
                }
                if (qty > sold - refunded) {
                    throw new IllegalArgumentException("Only " + (sold - refunded) + " of " + name
                            + " can still be refunded (" + sold + " sold, " + refunded + " already refunded).");
                }

                // 3. The refund
                double amount = PageHelper.round2(unitPrice * qty);
                int refundId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO refunds (sale_item_id, processed_by, refund_amount, reason, refund_date, quantity) "
                        + "VALUES (?, ?, ?, ?, NOW(), ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, saleItemId);
                    ps.setInt(2, employeeId);
                    ps.setDouble(3, amount);
                    ps.setString(4, reason);
                    ps.setInt(5, qty);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            refundId = keys.getInt(1);
                        }
                    }
                }

                // 4. Back to stock
                if (restock) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?")) {
                        ps.setInt(1, qty);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE products SET status = ? WHERE product_id = ? AND status = ?")) {
                        ps.setString(1, Product.STATUS_ACTIVE);
                        ps.setInt(2, productId);
                        ps.setString(3, Product.STATUS_OUTOFSTOCK);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return new String[]{String.valueOf(refundId), name, String.valueOf(amount)};
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
