/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package asia.uap.model;

import asia.uap.classes.Product;
import asia.uap.functionalclasses.PageHelper;
import asia.uap.functionalclasses.Sqlc;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Group 2
 */
// EDITED (new): sets or changes the picture of a product (form on inventory.jsp). URL: /do.setimage
//
// The picture FILE goes in web/images/productimg/ (added in NetBeans, not uploaded here);
// this page only saves its file name in products.image.
//   productid = the product's code (CPU-001) or product_id
//   image     = the file name, e.g. CPU-001.png. Blank = remove the picture (shows the placeholder).
@WebServlet(name = "setproductimage", urlPatterns = {"/do.setimage"})
public class setproductimage extends HttpServlet {

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
            out.println("<title>Product Picture</title>");
            out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println(PageHelper.nav(request)); // same navigation bar as nav.jsp

            // Read the form fields
            String id = request.getParameter("productid");
            String image = request.getParameter("image");

            // Stop if the page was opened directly, without the form data
            if (id == null || image == null) {
                out.println("<p>No form data received. Please use the form.</p>");
                out.println("<a href='inventory.jsp'>Go to Inventory</a>");
                out.println("</body>");
                out.println("</html>");
                return;
            }
            image = image.trim();

            // problem stays empty when the picture is saved
            String problem = "";
            Product p = Sqlc.findProduct(id);
            if (p == null) {
                problem = "Product " + id.trim() + " was not found.";
            } else if (!image.isEmpty() && !Product.isValidImageName(image)) {
                problem = "Picture file name must be a plain name like CPU-001.jpg (letters, numbers, - _ . "
                        + "and .jpg, .jpeg, .png, .webp or .gif). No folders.";
            } else {
                try {
                    saveImage(p.getProductid(), image.isEmpty() ? null : image);
                } catch (SQLException e) {
                    e.printStackTrace();
                    problem = "Could not save to the database. Please try again.";
                }
            }

            if (problem.isEmpty()) {
                out.println("<h1>Picture " + (image.isEmpty() ? "removed" : "saved") + "</h1>");
                out.println("<p>Product: " + PageHelper.esc(p.getCode()) + " - " + PageHelper.esc(p.getName()) + "</p>");
                if (image.isEmpty()) {
                    out.println("<p>The product now shows the \"No image available\" picture.</p>");
                } else {
                    out.println("<p>File: " + PageHelper.esc(image) + " <small>(it must be in web/images/productimg/)</small></p>");
                }
                // what the cashier, PC builder and products page will show
                // (the placeholder means the file is not in the folder yet)
                out.println(PageHelper.productImage(image.isEmpty() ? null : image, p.getName(), "product-thumb"));
            } else {
                out.println("<h1>Picture not saved</h1>");
                out.println("<p>" + PageHelper.esc(problem) + "</p>");
            }
            out.println("<p><a href='inventory.jsp'>Back to Inventory</a> | <a href='products.jsp'>Products</a></p>"
                    + "<p><a href='index.jsp' class='indexbtn'>Back to menu</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // Saves the picture file name of one product (null = no picture)
    private void saveImage(int productId, String image) throws SQLException {
        try (Connection conn = Sqlc.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET image = ? WHERE product_id = ?")) {
            ps.setString(1, image);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    // GET and POST both run processRequest()
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Set a product picture";
    }// </editor-fold>

}
