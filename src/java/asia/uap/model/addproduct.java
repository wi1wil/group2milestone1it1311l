/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.functionalclasses.PageHelper;
import asia.uap.classes.Product;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.*;

/**
 *
 * @author Group 2
 */
// Adds a new product (form on inventory.jsp). Nothing is saved yet. URL: /do.addproduct
@WebServlet(name = "addproduct", urlPatterns = {"/do.addproduct"})
public class addproduct extends HttpServlet {

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
        // Builds the whole HTML page; used by both GET and POST
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println(PageHelper.VIEWPORT);
            out.println("<title>Add Product Result</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Read the form fields
            String id = request.getParameter("productid");
            String name = request.getParameter("name");
            String category = request.getParameter("category");
            String priceText = request.getParameter("price");
            String stockText = request.getParameter("stock");
            // EDITED: picture file name in web/images/productimg/ (optional)
            String image = request.getParameter("image");

            // Stop if the page was opened directly, without the form data
            if (id == null || name == null || priceText == null || stockText == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='inventory.jsp'>Go to Inventory</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            if (category == null) {
                category = "";
            }
            id = id.trim();
            name = name.trim();
            category = category.trim();

            // Picture: blank = "<product code>.jpg" (the naming rule of the productimg folder).
            // A code that cannot be a file name (spaces, ...) gets no picture (null = placeholder).
            image = (image == null) ? "" : image.trim();
            boolean imageTyped = !image.isEmpty();
            if (!imageTyped) {
                image = Product.isValidImageName(id + ".jpg") ? id + ".jpg" : null;
            }

            // Convert price and stock to numbers (-1 means invalid)
            double price = -1;
            int stock = -1;
            try {
                price = Double.parseDouble(priceText.trim());
                stock = Integer.parseInt(stockText.trim());
            } catch (NumberFormatException e) {
                price = -1;
                stock = -1;
            }

            // problem stays empty when the product is saved
            String problem = "";

            // Validate the input, then save it
            if (id.isEmpty() || name.isEmpty() || category.isEmpty()) {
                problem = "Product ID, name and category are required.";
            } else if (id.length() > 50 || name.length() > 100 || category.length() > 50) {
                problem = "Product ID and category can be 50 characters and the name 100 characters at most.";
            } else if (price < 0 || stock < 0) {
                problem = "Price must be a number and stock must be a whole number (0 or more).";
            } else if (imageTyped && !Product.isValidImageName(image)) {
                problem = "Picture file name must be a plain name like CPU-001.jpg (letters, numbers, - _ . "
                        + "and .jpg, .jpeg, .png, .webp or .gif). No folders.";
            } else {
                try {
                    problem = saveProduct(id, name, category, price, stock, image);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
            }

            if (problem.isEmpty()) {
                out.println("<h1>Product added</h1>");
                out.println("<p>ID: " + iosqlclass.esc(id) + "</p>");
                out.println("<p>Name: " + iosqlclass.esc(name) + "</p>");
                out.println("<p>Category: " + iosqlclass.esc(category) + "</p>");
                out.println("<p>Price: PHP " + String.format("%.2f", price) + "</p>");
                out.println("<p>Stock: " + stock + "</p>");
                // the picture, as the cashier and the products page will show it
                out.println("<p>Picture: " + (image == null ? "none" : iosqlclass.esc(image)
                        + " <small>(put the file in web/images/productimg/)</small>") + "</p>");
                out.println(PageHelper.productImage(image, name, "product-thumb"));
                // Low stock alert is 5 or fewer
                if (stock <= 5) {
                    out.println("<p><b>LOW STOCK ALERT:</b> 5 or fewer items.</p>");
                }
            } else {
                out.println("<h1>Product not added</h1>");
                out.println("<p>" + iosqlclass.esc(problem) + "</p>");
            }
            out.println("<p><a href='inventory.jsp'>Back to Inventory</a></p><p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the product in the products table. Returns "" when saved, otherwise the reason it was not saved.
    private String saveProduct(String code, String name, String category, double price, int stock, String image) throws SQLException {
        iosqlclass db = new iosqlclass();
        try (Connection conn = db.getConnection()) {

            // The product ID (code) must be new
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM products WHERE code = ?")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (exists) {
                return "Product ID " + code + " already exists.";
            }

            // Save everything or nothing
            conn.setAutoCommit(false);
            try {
                // Find the category, or add it when it is new
                int categoryId = -1;
                try (PreparedStatement ps = conn.prepareStatement("SELECT category_id FROM categories WHERE name = ?")) {
                    ps.setString(1, category);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            categoryId = rs.getInt("category_id");
                        }
                    }
                }
                if (categoryId == -1) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, category);
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

                // Add the product
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO products (category_id, code, name, price, stock_quantity, status, image) "
                        + "VALUES (?, ?, ?, ?, ?, 'active', ?)")) {
                    ps.setInt(1, categoryId);
                    ps.setString(2, code);
                    ps.setString(3, name);
                    ps.setDouble(4, price);
                    ps.setInt(5, stock);
                    ps.setString(6, image);   // EDITED: picture file name (null = placeholder)
                    ps.executeUpdate();
                }
                // commit
                conn.commit();
            } catch (SQLException e) {
                // rollsback if something goes wrong
                conn.rollback();
                throw e;
            }
        }
        return "";
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
        return "Short description";
    }// </editor-fold>

}
