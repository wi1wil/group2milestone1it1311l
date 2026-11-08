/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;

/**
 * One line in the cashier's cart (the "unpaidCart" session attribute).
 * The same product always stays on ONE line, because sale_items allows
 * only one row per product per sale (UNIQUE sale_id, product_id).
 *
 * Discount rules (same idea as before, but fixed):
 *   null or 0       = no discount
 *   0 < d < 1       = percentage in decimal form (0.2 = 20% off)
 *   d >= 1          = fixed peso amount off EACH unit (never more than the price)
 *
 * @author Andrei
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer productid;
    private String code;            // product code, e.g. CPU-001
    private String name;            // product name shown on the cart and receipt
    private Double originalprice;   // price of one unit before the discount
    private Double price;           // price of one unit after the discount (what the customer pays)
    private Integer itemquantity;
    private Double itemdiscount;    // peso discount for ONE unit

    // Old constructor kept so older code still compiles (no code or name)
    public CartItem(Integer productid, Double price, Integer itemquantity, Double itemdiscount) {
        this(productid, "", "", price, itemquantity, itemdiscount);
    }

    public CartItem(Integer productid, String code, String name, Double price, Integer itemquantity, Double itemdiscount) {
        this.productid = productid;
        this.code = code;
        this.name = name;
        this.originalprice = round2(price);
        this.itemquantity = itemquantity;

        double off = 0;
        if (itemdiscount != null && itemdiscount > 0 && itemdiscount < 1) {
            // percentage in decimal form (i.e. 0.2 for 20%, .5 for 50% etc.)
            off = price * itemdiscount;
        } else if (itemdiscount != null && itemdiscount >= 1) {
            // fixed peso amount, but never more than the price itself
            off = Math.min(itemdiscount, price);
        }
        this.itemdiscount = round2(off);
        this.price = round2(price - off);
    }

    // rounds money to 2 decimal places (centavos)
    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Integer getProductid() {
        return productid;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    // price of one unit BEFORE the discount
    public Double getOriginalprice() {
        return originalprice;
    }

    // price of one unit AFTER the discount
    public Double getPrice() {
        return price;
    }

    public Integer getItemquantity() {
        return itemquantity;
    }

    // used when the same product is scanned again: the line keeps one row, only the quantity grows
    public void setItemquantity(Integer itemquantity) {
        this.itemquantity = itemquantity;
    }

    // peso discount for ONE unit
    public Double getItemdiscount() {
        return itemdiscount;
    }

    // peso discount for the whole line (discount x quantity)
    public Double getTotalDiscount() {
        return round2(itemdiscount * itemquantity);
    }

    // line total before the discount (original price x quantity)
    public Double getOriginalSubtotal() {
        return round2(originalprice * itemquantity);
    }

    // line total the customer pays (discounted price x quantity)
    public Double getSubtotal() {
        return round2(price * itemquantity);
    }

}
