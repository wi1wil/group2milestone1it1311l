/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;

/**
 * One line of a purchase order (purchase_order_items).
 *
 * Two kinds of lines:
 *   1. Existing product: productid is set.
 *   2. NEW product (a new item/variety the store does not sell yet): productid is null
 *      and the new product's details (category, brand, selling price) are kept here.
 *      The product row is only inserted when the purchase order is submitted
 *      (status "pending", stock 0). It becomes "active" when the stock is received.
 *
 * While the order is being prepared, the lines are kept in the session ("poDraft").
 *
 * @author Andrei
 */
public class PurchaseOrderItem implements Serializable {

    
    private static final long serialVersionUID = 1L;
    
    private Integer poitemid;       // null while it is still a draft
    private Integer productid;      // null for a new product that is not saved yet
    private String code;
    private String name;
    private Integer quantity;
    private Double unitcost;        // cost of one unit from the supplier

    // only used for new products
    private String categoryname;
    private String brand;
    private Double sellingprice;

    // Line for a product that is already in the products table
    public PurchaseOrderItem(Integer poitemid, Integer productid, String code, String name, Integer quantity, Double unitcost) {
        this.poitemid = poitemid;
        this.productid = productid;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.unitcost = unitcost;
    }

    // Line for a NEW product (inserted into products when the order is submitted)
    public PurchaseOrderItem(String code, String name, String categoryname, String brand,
            Double sellingprice, Integer quantity, Double unitcost) {
        this(null, null, code, name, quantity, unitcost);
        this.categoryname = categoryname;
        this.brand = brand;
        this.sellingprice = sellingprice;
    }

    public boolean isNewProduct() {
        return productid == null;
    }

    // cost of the whole line
    public Double getSubtotal() {
        return Math.round(unitcost * quantity * 100.0) / 100.0;
    }

    public Integer getPoitemid() {
        return poitemid;
    }

    public Integer getProductid() {
        return productid;
    }

    // set by SqlcPurchasing after the new product row is inserted
    public void setProductid(Integer productid) {
        this.productid = productid;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    // used when the same product is added to the draft again
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitcost() {
        return unitcost;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public String getBrand() {
        return brand;
    }

    public Double getSellingprice() {
        return sellingprice;
    }

}
