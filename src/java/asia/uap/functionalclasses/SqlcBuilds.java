/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.sql.*;
import java.util.ArrayList;
import asia.uap.classes.BuildPart;
import asia.uap.classes.BuildQuote;
import asia.uap.classes.BuildSlot;
import asia.uap.classes.CartItem;
import asia.uap.classes.JobOrder;
import asia.uap.classes.PcBuild;
import asia.uap.classes.Product;

/**
 * Static SQL class for the PC builder: saved quotes and placing a build order.
 *
 * The store has no online orders. A customer plans a build on the store's device and
 * SAVES it as a quote. At the counter, the cashier loads the quote, takes the payment
 * and places the order (sale + build + job order).
 *
 * builds.status:      quote = saved by a customer, not paid yet (no sale, no job order)
 *                     pending / building / completed / cancelled = follows the job order
 * build_items.source: quote = part of a saved quote
 *                     paid  = bought with the build order (the stock was taken when it was paid)
 *                     store = added later by a technician (taken from stock when the job is finished)
 *
 * @author Andrei
 */
public class SqlcBuilds {

    public static final String STATUS_QUOTE = "quote";
    public static final String SOURCE_QUOTE = "quote";
    public static final String SOURCE_PAID = "paid";
    public static final String SOURCE_STORE = "store";

    // ------------------------------------------------------------- saved quotes

    /**
     * Saves the build as a quote for its customer (one transaction).
     * If the build was loaded from one of this customer's quotes, that quote is updated;
     * otherwise a new quote is made.
     *
     * @return the quote's build_id
     */
    public static int saveQuote(PcBuild build, String quoteName) throws SQLException {
        if (build.getCustomerid() == null) {
            throw new IllegalArgumentException("Log in as a customer to save a build.");
        }
        if (build.isEmpty()) {
            throw new IllegalArgumentException("Choose some parts first.");
        }
        if (!build.getMissingRequired().isEmpty()) {
            throw new IllegalArgumentException("The build still needs: " + build.getMissingRequired() + ".");
        }
        if (!CompatibilityChecker.isCompatible(CompatibilityChecker.check(build.getParts()))) {
            throw new IllegalArgumentException("The parts are not compatible. Fix the failed checks before saving.");
        }
        int customerId = build.getCustomerid();

        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // update the quote it was loaded from, when it is still this customer's quote
                Integer buildId = null;
                if (build.getQuoteid() != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE builds SET build_name = ?, created_at = NOW() WHERE build_id = ? AND customer_id = ? AND status = ?")) {
                        ps.setString(1, quoteName);
                        ps.setInt(2, build.getQuoteid());
                        ps.setInt(3, customerId);
                        ps.setString(4, STATUS_QUOTE);
                        if (ps.executeUpdate() == 1) {
                            buildId = build.getQuoteid();
                        }
                    }
                    if (buildId != null) {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM build_items WHERE build_id = ?")) {
                            ps.setInt(1, buildId);
                            ps.executeUpdate();
                        }
                    }
                }

                // otherwise a new quote
                if (buildId == null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO builds (customer_id, build_name, created_at, status) VALUES (?, ?, NOW(), ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, customerId);
                        ps.setString(2, quoteName);
                        ps.setString(3, STATUS_QUOTE);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                buildId = keys.getInt(1);
                            }
                        }
                    }
                    if (buildId == null) {
                        throw new SQLException("No build_id was returned for the new quote.");
                    }
                }

                // the parts (price saved for reference; today's price is charged when ordered)
                insertItems(conn, buildId, build, SOURCE_QUOTE);

                conn.commit();
                build.setQuote(buildId, quoteName);
                return buildId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Saved quotes, newest first.
     * customerId = one customer's quotes, or null for every customer's (staff view).
     */
    public static ArrayList<BuildQuote> getQuotes(Integer customerId) throws SQLException {
        ArrayList<BuildQuote> list = new ArrayList<>();
        String sql = "SELECT b.build_id, b.build_name, b.customer_id, b.created_at, c.username, "
                + "CONCAT(c.first_name, ' ', c.last_name) AS customer, "
                + "COUNT(bi.build_item_id) AS parts, COALESCE(SUM(bi.unit_price * bi.quantity), 0) AS total "
                + "FROM builds b JOIN customers c ON b.customer_id = c.customer_id "
                + "LEFT JOIN build_items bi ON bi.build_id = b.build_id "
                + "WHERE b.status = ? " + (customerId == null ? "" : "AND b.customer_id = ? ")
                + "GROUP BY b.build_id, b.build_name, b.customer_id, b.created_at, c.username, c.first_name, c.last_name "
                + "ORDER BY b.created_at DESC, b.build_id DESC";
        try (Connection conn = Sqlc.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, STATUS_QUOTE);
            if (customerId != null) {
                ps.setInt(2, customerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp saved = rs.getTimestamp("created_at");
                    list.add(new BuildQuote(rs.getInt("build_id"), rs.getString("build_name"), rs.getInt("customer_id"),
                            rs.getString("customer"), rs.getString("username"),
                            (saved == null) ? null : saved.toLocalDateTime(),
                            rs.getInt("parts"), rs.getDouble("total")));
                }
            }
        }
        return list;
    }

    /**
     * Loads a saved quote into a new PcBuild, with TODAY's prices, promos and specs.
     * customerId = the logged-in customer (they may only load their own), or null for staff.
     * Parts that can no longer be bought are left out; their names are added to "skipped".
     *
     * @return the build, or null when there is no such quote (or it belongs to someone else)
     */
    public static PcBuild loadQuote(int quoteId, Integer customerId, ArrayList<String> skipped) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            PcBuild build = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT b.build_name, b.customer_id, CONCAT(c.first_name, ' ', c.last_name) AS customer "
                    + "FROM builds b JOIN customers c ON b.customer_id = c.customer_id "
                    + "WHERE b.build_id = ? AND b.status = ?" + (customerId == null ? "" : " AND b.customer_id = ?"))) {
                ps.setInt(1, quoteId);
                ps.setString(2, STATUS_QUOTE);
                if (customerId != null) {
                    ps.setInt(3, customerId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        build = new PcBuild();
                        build.setCustomer(rs.getInt("customer_id"), rs.getString("customer"));
                        build.setQuote(quoteId, rs.getString("build_name"));
                    }
                }
            }
            if (build == null) {
                return null;
            }

            // each part goes back into the slot of its category, with today's price
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT bi.product_id, bi.quantity, p.name, c.name AS category FROM build_items bi "
                    + "JOIN products p ON bi.product_id = p.product_id "
                    + "JOIN categories c ON p.category_id = c.category_id WHERE bi.build_id = ?")) {
                ps.setInt(1, quoteId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BuildSlot slot = BuildSlot.findByCategory(rs.getString("category"));
                        Product p = Sqlc.getProduct(rs.getInt("product_id"));
                        if (slot == null || p == null || !p.isSellable()) {
                            skipped.add(rs.getString("name"));
                            continue;
                        }
                        build.put(storePart(slot.getKey(), p.getProductid(), rs.getInt("quantity")));
                    }
                }
            }
            return build;
        }
    }

    /**
     * Deletes a saved quote (only quotes: a paid build can never be deleted here).
     * customerId = the logged-in customer (only their own), or null for staff.
     *
     * @return true when a quote was deleted
     */
    public static boolean deleteQuote(int quoteId, Integer customerId) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean deleted = removeQuote(conn, quoteId, customerId);
                conn.commit();
                return deleted;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Deletes the quote's parts and the quote itself (inside the caller's transaction)
    private static boolean removeQuote(Connection conn, int quoteId, Integer customerId) throws SQLException {
        // check it really is a quote (and the customer's, when given) before touching its parts
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT build_id FROM builds WHERE build_id = ? AND status = ?" + (customerId == null ? "" : " AND customer_id = ?")
                + " FOR UPDATE")) {
            ps.setInt(1, quoteId);
            ps.setString(2, STATUS_QUOTE);
            if (customerId != null) {
                ps.setInt(3, customerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM build_items WHERE build_id = ?")) {
            ps.setInt(1, quoteId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM builds WHERE build_id = ? AND status = ?")) {
            ps.setInt(1, quoteId);
            ps.setString(2, STATUS_QUOTE);
            ps.executeUpdate();
        }
        return true;
    }

    // Inserts every part of the build as build_items rows with the given source
    private static void insertItems(Connection conn, int buildId, PcBuild build, String source) throws SQLException {
        for (BuildPart part : build.getPartList()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO build_items (build_id, product_id, unit_price, quantity, source) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, buildId);
                ps.setInt(2, part.getProductid());
                ps.setDouble(3, part.getUnitprice());
                ps.setInt(4, part.getQuantity());
                ps.setString(5, source);
                ps.executeUpdate();
            }
        }
    }

    // ------------------------------------------------------------- placing the order (staff only)

    /**
     * The active technician with the fewest open jobs (Pending or In Progress).
     * Returns { employee_id, full name }, or null when there is no active technician.
     */
    private static String[] pickTechnician(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT e.employee_id, e.first_name, e.last_name, "
                + "(SELECT COUNT(*) FROM job_orders j WHERE j.employee_id = e.employee_id AND j.status IN (?, ?)) AS open_jobs "
                + "FROM employees e WHERE e.role_name = 'technician' AND LOWER(e.status) = 'active' "
                + "ORDER BY open_jobs, e.employee_id LIMIT 1")) {
            ps.setString(1, JobOrder.PENDING);
            ps.setString(2, JobOrder.IN_PROGRESS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{String.valueOf(rs.getInt("employee_id")),
                        rs.getString("first_name") + " " + rs.getString("last_name")};
                }
            }
        }
        return null;
    }

    /**
     * Places a build order at the counter (one transaction):
     *   1. pick the technician (active, fewest open jobs)
     *   2. save the sale under the staff member with SqlcSales.insertSale (checks the stock and lowers it)
     *   3. builds row (status pending) + build_items (source paid)
     *   4. job_orders row (Pending) for the technician
     *   5. when the build was loaded from a saved quote, that quote is removed (it became this build)
     *
     * @return { build_id, job_id, sale_id, technician name }
     */
    public static String[] placeBuildOrder(PcBuild build, String buildName, int staffId,
            double amountPaid, String paymentMethod) throws SQLException {
        if (build.getCustomerid() == null) {
            throw new IllegalArgumentException("Choose who the build is for first.");
        }
        if (!build.getMissingRequired().isEmpty()) {
            throw new IllegalArgumentException("The build still needs: " + build.getMissingRequired() + ".");
        }
        if (!CompatibilityChecker.isCompatible(CompatibilityChecker.check(build.getParts()))) {
            throw new IllegalArgumentException("The parts are not compatible. Fix the failed checks first.");
        }
        int customerId = build.getCustomerid();

        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. technician
                String[] tech = pickTechnician(conn);
                if (tech == null) {
                    throw new IllegalArgumentException("No technician is available right now. Please try again later.");
                }
                int techId = Integer.parseInt(tech[0]);

                // 2. the parts are paid (as one sale), same price math as when they were picked (CartItem)
                ArrayList<CartItem> cart = new ArrayList<>();
                for (BuildPart part : build.getPartList()) {
                    cart.add(new CartItem(part.getProductid(), part.getCode(), part.getName(),
                            part.getListprice(), part.getQuantity(), part.getDiscountrate()));
                }
                int saleId = SqlcSales.insertSale(conn, staffId, customerId, cart, amountPaid, paymentMethod);

                // 3. the build and its parts
                int buildId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO builds (customer_id, build_name, created_at, status) VALUES (?, ?, NOW(), ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setString(2, buildName);
                    ps.setString(3, JobOrder.buildStatusFor(JobOrder.PENDING));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            buildId = keys.getInt(1);
                        }
                    }
                }
                if (buildId == -1) {
                    throw new SQLException("No build_id was returned for the new build.");
                }
                insertItems(conn, buildId, build, SOURCE_PAID);

                // 4. the job order for the technician
                int jobId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO job_orders (build_id, employee_id, status, created_at) VALUES (?, ?, ?, NOW())",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, buildId);
                    ps.setInt(2, techId);
                    ps.setString(3, JobOrder.PENDING);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            jobId = keys.getInt(1);
                        }
                    }
                }
                if (jobId == -1) {
                    throw new SQLException("No job_id was returned for the new job order.");
                }

                // 5. the saved quote is now a real order
                if (build.getQuoteid() != null) {
                    removeQuote(conn, build.getQuoteid(), customerId);
                }

                conn.commit();
                return new String[]{String.valueOf(buildId), String.valueOf(jobId), String.valueOf(saleId), tech[1]};
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Makes a BuildPart for a store product: reads its specs and applies today's best promo.
     * Returns null when the product does not exist.
     */
    public static BuildPart storePart(String slotKey, int productId, int qty) {
        Product p = Sqlc.getProduct(productId);
        if (p == null) {
            return null;
        }
        double rate = Sqlc.getPromoPercent(productId) / 100.0;
        double charged = new CartItem(productId, p.getPrice(), 1, rate).getPrice();
        return new BuildPart(slotKey, productId, p.getCode(), p.getName(), p.getPrice(), charged, rate, qty,
                Sqlc.getSpecs(productId));
    }
}
