/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import asia.uap.classes.Product;
import asia.uap.classes.Supplier;

/**
 * Static SQL class: the simple SELECTs shared by many servlets
 * (products, promos, categories, customers, suppliers).
 *
 * The bigger jobs that change several tables at once live in their own classes:
 *   SqlcSales      = save a sale, load a sale, refunds
 *   SqlcPurchasing = purchase orders and receiving stock
 *   SqlcJobs       = job orders and build parts
 *
 * Lookup methods here return null when nothing is found OR when the database
 * cannot be reached (the error is printed in the server log).
 *
 * EDITED (fixes to the old methods):
 *   - checkPrice read the "product_id" column instead of "price"
 *   - checkProductId used COUNT(*), which always returns one row, so it was always true
 *   - connections were never closed; now try-with-resources closes them
 *
 * @author Andrei
 */
public class Sqlc {

    private static ResourceBundle rb = ResourceBundle.getBundle("asia.uap.model.group2sql");
    private static String driver = rb.getString("driver");
    private static String dbUrl = rb.getString("url");
    private static String user = rb.getString("user");
    private static String pass = rb.getString("password");

    // username of the customer account used when the buyer gives no account (see sql/EDITED_sample_data.sql)
    public static final String WALKIN_USERNAME = "walkin";

    // products with this many or fewer left are "low stock" (same as stock.jsp)
    public static final int LOW_STOCK = 5;

    // the SELECT part shared by every product lookup (readProduct reads these columns)
    private static final String PRODUCT_SELECT = "SELECT p.product_id, p.category_id, c.name AS category, p.code, "
            + "p.name, p.brand, p.price, p.stock_quantity, p.description, p.status, p.image "
            + "FROM products p JOIN categories c ON p.category_id = c.category_id ";

    private static boolean loadClass() {
        try {
            Class.forName(driver);
            return true;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Opens a new connection. Always use it in try-with-resources so it gets closed.
    public static Connection getConnection() throws SQLException {
        loadClass();
        return DriverManager.getConnection(dbUrl, user, pass);
    }

    // ------------------------------------------------------------- old methods (fixed)

    // Price of one product, or null when not found
    public static Double checkPrice(Integer product_id) {
        if (product_id == null) {
            return null;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT price FROM products WHERE product_id = ?")) {
            ps.setInt(1, product_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // True when a product with this product_id exists
    public static boolean checkProductId(Integer id) {
        if (id == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM products WHERE product_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true only when a row came back
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Name of one product, or null when not found
    public static String checkProductName(Integer id) {
        if (id == null) {
            return null;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM products WHERE product_id = ? LIMIT 1")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------- products

    // Reads the current row of a ResultSet made with PRODUCT_SELECT into a Product
    public static Product readProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getInt("category_id"),
                rs.getString("category"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("brand"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("image"));   // EDITED: picture file name (null = none)
    }

    // One product by its product_id, or null
    public static Product getProduct(int productId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(PRODUCT_SELECT + "WHERE p.product_id = ?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readProduct(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // One product by its code (e.g. CPU-001), or null. Upper/lower case does not matter.
    public static Product getProductByCode(String code) {
        if (code == null) {
            return null;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(PRODUCT_SELECT + "WHERE p.code = ?")) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readProduct(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // What the user typed may be the product_id (a number) or the code (CPU-001). Tries both.
    public static Product findProduct(String idOrCode) {
        if (idOrCode == null || idOrCode.trim().isEmpty()) {
            return null;
        }
        Product p = null;
        if (ParseThis.isInt(idOrCode)) {
            p = getProduct(ParseThis.toInt(idOrCode));
        }
        if (p == null) {
            p = getProductByCode(idOrCode);
        }
        return p;
    }

    /**
     * Products shown on the cashier POS picker, sorted by category then name.
     * Shows active and out of stock products (out of stock ones are shown greyed out).
     * Pending (not received yet) and inactive products are hidden.
     * search = part of the name, code, brand or category; null or blank shows everything.
     */
    public static ArrayList<Product> getPosProducts(String search) {
        ArrayList<Product> list = new ArrayList<>();
        boolean all = (search == null || search.trim().isEmpty());
        String sql = PRODUCT_SELECT + "WHERE p.status IN (?, ?) "
                + (all ? "" : "AND (p.name LIKE ? OR p.code LIKE ? OR p.brand LIKE ? OR c.name LIKE ?) ")
                + "ORDER BY c.name, p.name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Product.STATUS_ACTIVE);
            ps.setString(2, Product.STATUS_OUTOFSTOCK);
            if (!all) {
                String like = "%" + search.trim() + "%";
                for (int i = 3; i <= 6; i++) {
                    ps.setString(i, like);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readProduct(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Products of one category (by category name) for the PC builder picker, cheapest first.
    // Same statuses as getPosProducts: active and out of stock (shown greyed out).
    public static ArrayList<Product> getProductsByCategory(String category) {
        ArrayList<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(PRODUCT_SELECT
                     + "WHERE c.name = ? AND p.status IN (?, ?) ORDER BY p.price, p.name")) {
            ps.setString(1, category);
            ps.setString(2, Product.STATUS_ACTIVE);
            ps.setString(3, Product.STATUS_OUTOFSTOCK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readProduct(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------- product specs

    // Specs of one product: spec name (lower case) -> value. Empty when it has none.
    public static HashMap<String, String> getSpecs(int productId) {
        HashMap<String, String> specs = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT spec_name, spec_value FROM product_specs WHERE product_id = ?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    specs.put(rs.getString("spec_name").trim().toLowerCase(), rs.getString("spec_value").trim());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return specs;
    }

    // Specs of every product in a category, read with ONE query: product_id -> (spec name -> value)
    public static HashMap<Integer, HashMap<String, String>> getSpecsForCategory(String category) {
        HashMap<Integer, HashMap<String, String>> all = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT s.product_id, s.spec_name, s.spec_value FROM product_specs s "
                     + "JOIN products p ON s.product_id = p.product_id "
                     + "JOIN categories c ON p.category_id = c.category_id WHERE c.name = ?")) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    all.computeIfAbsent(rs.getInt("product_id"), k -> new HashMap<>())
                            .put(rs.getString("spec_name").trim().toLowerCase(), rs.getString("spec_value").trim());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return all;
    }

    // Products that need restocking (LOW_STOCK or fewer), lowest stock first.
    // Inactive (removed) products are skipped.
    public static ArrayList<Product> getLowStockProducts() {
        ArrayList<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(PRODUCT_SELECT
                     + "WHERE p.stock_quantity <= ? AND p.status <> ? ORDER BY p.stock_quantity, p.name")) {
            ps.setInt(1, LOW_STOCK);
            ps.setString(2, Product.STATUS_INACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readProduct(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Best promo percent (0 to 100) for a product today.
     * A promo with product_id NULL is for every product.
     * Promos with no start/end date are always on.
     * Returns 0 when there is no promo.
     */
    public static double getPromoPercent(int productId) {
        String sql = "SELECT MAX(percentage) AS best FROM promos "
                + "WHERE (product_id = ? OR product_id IS NULL) "
                + "AND (start_date IS NULL OR start_date <= CURDATE()) "
                + "AND (end_date IS NULL OR end_date >= CURDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double best = rs.getDouble("best"); // 0 when MAX() is NULL
                    if (best > 0 && best <= 100) {
                        return best;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Every promo that is on today, read with ONE query (for pages that list many products).
     * Key = product_id, value = best percent for that product.
     * Key 0 = best store-wide promo (product_id NULL), which applies to every product.
     * Use bestPromo(promos, productId) to read it.
     */
    public static HashMap<Integer, Double> getActivePromos() {
        HashMap<Integer, Double> promos = new HashMap<>();
        String sql = "SELECT COALESCE(product_id, 0) AS pid, MAX(percentage) AS best FROM promos "
                + "WHERE (start_date IS NULL OR start_date <= CURDATE()) "
                + "AND (end_date IS NULL OR end_date >= CURDATE()) "
                + "GROUP BY COALESCE(product_id, 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double best = rs.getDouble("best");
                if (best > 0 && best <= 100) {
                    promos.put(rs.getInt("pid"), best);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return promos;
    }

    // Best percent (0 to 100) for one product from getActivePromos(): its own promo or the store-wide one
    public static double bestPromo(HashMap<Integer, Double> promos, int productId) {
        double own = promos.getOrDefault(productId, 0.0);
        double storeWide = promos.getOrDefault(0, 0.0);
        return Math.max(own, storeWide);
    }

    // ------------------------------------------------------------- customers

    // customer_id for a username, or null when not found
    public static Integer findCustomerId(String username) {
        if (username == null) {
            return null;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT customer_id FROM customers WHERE username = ?")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // { customer_id, full name } for a username, or null when not found
    public static String[] findCustomer(String username) {
        if (username == null) {
            return null;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT customer_id, first_name, last_name FROM customers WHERE username = ?")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{String.valueOf(rs.getInt("customer_id")),
                        rs.getString("first_name") + " " + rs.getString("last_name")};
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------- suppliers

    // Every supplier, sorted by name (empty list when there are none)
    public static ArrayList<Supplier> getSuppliers() {
        ArrayList<Supplier> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT supplier_id, name, contact FROM suppliers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Supplier(rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // One supplier, or null
    public static Supplier getSupplier(int supplierId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT supplier_id, name, contact FROM suppliers WHERE supplier_id = ?")) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Supplier(rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------- categories

    // Every category name, sorted (used for the "new product" suggestions)
    public static ArrayList<String> getCategoryNames() {
        ArrayList<String> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

}
