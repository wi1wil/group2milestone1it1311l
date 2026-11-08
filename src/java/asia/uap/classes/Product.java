/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;

/**
 * One row of the products table (plus the category name).
 *
 * Status values used by the system (products.status is varchar(10)):
 *   active     = can be sold
 *   outofstock = stock reached 0 after a sale; becomes active again when restocked
 *   pending    = new product added through a purchase order, not received yet
 *   inactive   = removed from sale by staff (never changed automatically)
 *
 * @author Andrei
 */
public class Product implements Serializable {

    
    private static final long serialVersionUID = 1L;
    
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_OUTOFSTOCK = "outofstock";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_INACTIVE = "inactive";

    private Integer productid;
    private Integer categoryid;
    private String category;
    private String code;
    private String name;
    private String brand;
    private Double price;
    private Integer stockquantity;
    private String description;
    private String status;
    private String image;           // EDITED: picture file name in web/images/productimg/ (null = none)

    // folder of the product pictures, and the picture shown when a product has none
    public static final String IMAGE_FOLDER = "images/productimg/";
    public static final String NO_IMAGE = "images/EDITEDno-image.svg";

    public Product(Integer productid, Integer categoryid, String category, String code, String name,
            String brand, Double price, Integer stockquantity, String description, String status) {
        this(productid, categoryid, category, code, name, brand, price, stockquantity, description, status, null);
    }

    public Product(Integer productid, Integer categoryid, String category, String code, String name,
            String brand, Double price, Integer stockquantity, String description, String status, String image) {
        this.productid = productid;
        this.categoryid = categoryid;
        this.category = category;
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stockquantity = stockquantity;
        this.description = description;
        this.status = status;
        this.image = image;
    }

    /**
     * Is this a safe picture file name? Only a plain name is allowed: letters, numbers, - _ .
     * and a picture extension. This blocks paths like "../../WEB-INF/x" and HTML tricks,
     * since the name is put into the page.
     */
    public static boolean isValidImageName(String name) {
        return name != null && name.length() <= 100
                && name.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*\\.(?i)(jpg|jpeg|png|webp|gif)");
    }

    // Picture shown on the products page, cashier and PC builder.
    // EDITED: the product's own picture from images/productimg/, or the placeholder when it has none.
    // (A file that is missing from the folder also shows the placeholder: see imageTag.)
    public String getImagePath() {
        return isValidImageName(image) ? IMAGE_FOLDER + image : NO_IMAGE;
    }

    public String getImage() {
        return image;
    }

    // true when the cashier may sell it (out of stock items are not sellable)
    public boolean isSellable() {
        return STATUS_ACTIVE.equalsIgnoreCase(status) && stockquantity > 0;
    }

    public Integer getProductid() {
        return productid;
    }

    public Integer getCategoryid() {
        return categoryid;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getStockquantity() {
        return stockquantity;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

}
