/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.sql.*;
import java.util.ArrayList;
import asia.uap.classes.BuildItem;
import asia.uap.classes.JobOrder;
import asia.uap.classes.Product;

/**
 * Static SQL class for job orders (job_orders), PC builds (builds) and build parts (build_items).
 *
 * Every change first locks the job_orders row (FOR UPDATE) and checks the job is still open,
 * so two technicians cannot change the same job at the same time.
 *
 * Stock rule: parts are taken out of stock only when the job becomes "Success"
 * (the parts were installed). Before that, the build is only a plan.
 *
 * Problems the user should see are thrown as IllegalArgumentException.
 *
 * @author Andrei
 */
public class SqlcJobs {

    private static final String JOB_SELECT = "SELECT j.job_id, j.build_id, j.employee_id, j.status, j.created_at, "
            + "j.completed_at, b.build_name, b.status AS build_status, b.customer_id, "
            + "CONCAT(c.first_name, ' ', c.last_name) AS customer, "
            + "CONCAT(e.first_name, ' ', e.last_name) AS technician "
            + "FROM job_orders j "
            + "JOIN builds b ON j.build_id = b.build_id "
            + "JOIN customers c ON b.customer_id = c.customer_id "
            + "JOIN employees e ON j.employee_id = e.employee_id ";

    // Every job order, newest first (without the parts)
    public static ArrayList<JobOrder> getJobOrders() throws SQLException {
        ArrayList<JobOrder> list = new ArrayList<>();
        try (Connection conn = Sqlc.getConnection();
             PreparedStatement ps = conn.prepareStatement(JOB_SELECT + "ORDER BY j.job_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(readJob(rs));
            }
        }
        return list;
    }

    // One customer's job orders, newest first (for "My build orders" on the PC builder)
    public static ArrayList<JobOrder> getJobOrdersForCustomer(int customerId) throws SQLException {
        ArrayList<JobOrder> list = new ArrayList<>();
        try (Connection conn = Sqlc.getConnection();
             PreparedStatement ps = conn.prepareStatement(JOB_SELECT + "WHERE b.customer_id = ? ORDER BY j.job_id DESC")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readJob(rs));
                }
            }
        }
        return list;
    }

    // One job order with its build parts, or null
    public static JobOrder getJobOrder(int jobId) throws SQLException {
        try (Connection conn = Sqlc.getConnection()) {
            JobOrder job = null;
            try (PreparedStatement ps = conn.prepareStatement(JOB_SELECT + "WHERE j.job_id = ?")) {
                ps.setInt(1, jobId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        job = readJob(rs);
                    }
                }
            }
            if (job != null) {
                job.getItems().addAll(loadItems(conn, job.getBuildid()));
            }
            return job;
        }
    }

    private static JobOrder readJob(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp completed = rs.getTimestamp("completed_at");
        return new JobOrder(rs.getInt("job_id"), rs.getInt("build_id"), rs.getString("build_name"),
                rs.getString("build_status"), rs.getInt("customer_id"), rs.getString("customer"),
                rs.getInt("employee_id"), rs.getString("technician"), rs.getString("status"),
                (created == null) ? null : created.toLocalDateTime(),
                (completed == null) ? null : completed.toLocalDateTime());
    }

    private static ArrayList<BuildItem> loadItems(Connection conn, int buildId) throws SQLException {
        ArrayList<BuildItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT bi.build_item_id, bi.build_id, bi.product_id, p.code, p.name, bi.unit_price, "
                + "bi.quantity, bi.source, p.stock_quantity FROM build_items bi "
                + "JOIN products p ON bi.product_id = p.product_id "
                + "WHERE bi.build_id = ? ORDER BY bi.build_item_id")) {
            ps.setInt(1, buildId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BuildItem(rs.getInt("build_item_id"), rs.getInt("build_id"), rs.getInt("product_id"),
                            rs.getString("code"), rs.getString("name"), rs.getDouble("unit_price"),
                            rs.getInt("quantity"), rs.getInt("stock_quantity"), rs.getString("source")));
                }
            }
        }
        return list;
    }

    // Locks the job row and returns { build_id, status }. Throws when the job does not exist.
    private static String[] lockJob(Connection conn, int jobId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT build_id, status FROM job_orders WHERE job_id = ? FOR UPDATE")) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Job order " + String.format("JO-%03d", jobId) + " was not found.");
                }
                return new String[]{String.valueOf(rs.getInt("build_id")), rs.getString("status")};
            }
        }
    }

    // Locks the job and checks the parts may still be changed. Returns the build_id.
    private static int lockEditableJob(Connection conn, int jobId) throws SQLException {
        String[] job = lockJob(conn, jobId);
        if (!(JobOrder.PENDING.equals(job[1]) || JobOrder.IN_PROGRESS.equals(job[1]))) {
            throw new IllegalArgumentException("The job is already " + job[1] + ", so its parts can no longer be changed.");
        }
        return Integer.parseInt(job[0]);
    }

    /**
     * Adds a part to the build. If the part is already in the build, the quantity is added
     * to that line (build_items allows one row per product per build).
     * The unit price is the product's current price.
     */
    public static void addBuildItem(int jobId, int productId, int qty) throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be a whole number of at least 1.");
        }
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int buildId = lockEditableJob(conn, jobId);

                // the product must exist and be for sale
                double price;
                try (PreparedStatement ps = conn.prepareStatement("SELECT name, price, status FROM products WHERE product_id = ?")) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Product was not found.");
                        }
                        String status = rs.getString("status");
                        if (Product.STATUS_INACTIVE.equalsIgnoreCase(status) || Product.STATUS_PENDING.equalsIgnoreCase(status)) {
                            throw new IllegalArgumentException(rs.getString("name") + " is not available (" + status + ").");
                        }
                        price = rs.getDouble("price");
                    }
                }

                // already in the build? then only the quantity changes
                Integer lineId = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT build_item_id, source FROM build_items WHERE build_id = ? AND product_id = ?")) {
                    ps.setInt(1, buildId);
                    ps.setInt(2, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // build_items allows one row per product, and a customer's part cannot be changed
                            if (!SqlcBuilds.SOURCE_STORE.equals(rs.getString("source"))) {
                                throw new IllegalArgumentException("This product is already in the build as a part the customer paid for.");
                            }
                            lineId = rs.getInt("build_item_id");
                        }
                    }
                }

                if (lineId == null) {
                    // added by the technician: source "store", taken from stock when the job is finished
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO build_items (build_id, product_id, unit_price, quantity, source) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setInt(1, buildId);
                        ps.setInt(2, productId);
                        ps.setDouble(3, price);
                        ps.setInt(4, qty);
                        ps.setString(5, SqlcBuilds.SOURCE_STORE);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE build_items SET quantity = quantity + ? WHERE build_item_id = ?")) {
                        ps.setInt(1, qty);
                        ps.setInt(2, lineId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Changes the quantity of one part. A quantity of 0 removes the part.
    // Only parts added by a technician (source "store") can be changed: the customer's parts were paid for.
    public static void updateBuildItemQty(int jobId, int buildItemId, int qty) throws SQLException {
        if (qty < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int buildId = lockEditableJob(conn, jobId);

                // the part must belong to THIS job's build
                String source = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT source FROM build_items WHERE build_item_id = ? AND build_id = ?")) {
                    ps.setInt(1, buildItemId);
                    ps.setInt(2, buildId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            source = rs.getString("source");
                        }
                    }
                }
                if (source == null) {
                    throw new IllegalArgumentException("That part is not in this build.");
                }
                if (!SqlcBuilds.SOURCE_STORE.equals(source)) {
                    throw new IllegalArgumentException("The customer already paid for this part, so it cannot be changed here.");
                }

                String sql = (qty == 0)
                        ? "DELETE FROM build_items WHERE build_item_id = ? AND build_id = ?"
                        : "UPDATE build_items SET quantity = ? WHERE build_item_id = ? AND build_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    if (qty > 0) {
                        ps.setInt(i++, qty);
                    }
                    ps.setInt(i++, buildItemId);
                    ps.setInt(i, buildId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Removes one part from the build
    public static void removeBuildItem(int jobId, int buildItemId) throws SQLException {
        updateBuildItemQty(jobId, buildItemId, 0);
    }

    /**
     * Changes the job status (one transaction).
     *   - Only the moves allowed by JobOrder.canChange are accepted.
     *   - In Progress: the technician who starts it becomes the assigned employee.
     *   - Success: the build needs at least one part and enough stock for every part;
     *     the parts are taken out of stock and completed_at is set.
     *   - builds.status is updated to match (pending, building, completed, cancelled).
     */
    public static void updateJobStatus(int jobId, String newStatus, int employeeId) throws SQLException {
        if (!JobOrder.isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Unknown status: " + newStatus);
        }
        try (Connection conn = Sqlc.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String[] job = lockJob(conn, jobId);
                int buildId = Integer.parseInt(job[0]);
                String oldStatus = job[1];
                if (!JobOrder.canChange(oldStatus, newStatus)) {
                    throw new IllegalArgumentException("A job that is " + oldStatus + " cannot be changed to " + newStatus + ".");
                }

                if (newStatus.equals(JobOrder.SUCCESS)) {
                    takePartsFromStock(conn, buildId);
                }

                // the job (In Progress also saves who is working on it)
                String sql = "UPDATE job_orders SET status = ?, completed_at = "
                        + (newStatus.equals(JobOrder.SUCCESS) ? "NOW()" : "NULL")
                        + (newStatus.equals(JobOrder.IN_PROGRESS) ? ", employee_id = ?" : "")
                        + " WHERE job_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, newStatus);
                    if (newStatus.equals(JobOrder.IN_PROGRESS)) {
                        ps.setInt(i++, employeeId);
                    }
                    ps.setInt(i, jobId);
                    ps.executeUpdate();
                }

                // the build follows the job
                try (PreparedStatement ps = conn.prepareStatement("UPDATE builds SET status = ? WHERE build_id = ?")) {
                    ps.setString(1, JobOrder.buildStatusFor(newStatus));
                    ps.setInt(2, buildId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Checks and subtracts the stock of the parts the technician added (used when the job is finished).
    // Parts paid with the order already left the stock, and the customer's own parts never used it.
    private static void takePartsFromStock(Connection conn, int buildId) throws SQLException {
        ArrayList<BuildItem> items = loadItems(conn, buildId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("The build has no parts yet. Add the parts before finishing the job.");
        }
        for (BuildItem item : items) {
            if (item.isCustomerPart()) {
                continue;
            }
            // lock the product and check the stock again
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT stock_quantity FROM products WHERE product_id = ? FOR UPDATE")) {
                ps.setInt(1, item.getProductid());
                try (ResultSet rs = ps.executeQuery()) {
                    int stock = rs.next() ? rs.getInt("stock_quantity") : 0;
                    if (stock < item.getQuantity()) {
                        throw new IllegalArgumentException("Not enough stock of " + item.getName() + " (needs "
                                + item.getQuantity() + ", only " + stock + " left). Order more or change the part.");
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?")) {
                ps.setInt(1, item.getQuantity());
                ps.setInt(2, item.getProductid());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET status = ? WHERE product_id = ? AND stock_quantity <= 0 AND status = ?")) {
                ps.setString(1, Product.STATUS_OUTOFSTOCK);
                ps.setInt(2, item.getProductid());
                ps.setString(3, Product.STATUS_ACTIVE);
                ps.executeUpdate();
            }
        }
    }
}
