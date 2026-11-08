/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The part chosen for one slot of the PC builder (kept in the session inside PcBuild).
 * Every part is bought from the store's stock and paid when the build is ordered.
 *
 * The specs (from product_specs) are kept here so the compatibility check does not
 * need the database every time the page is shown.
 *
 * @author Andrei
 */
public class BuildPart implements Serializable {

    // keeps saved sessions readable after this class is recompiled (Tomcat saves sessions on restart)
    private static final long serialVersionUID = 1L;

    private String slotkey;
    private Integer productid;
    private String code;
    private String name;
    private Double listprice;       // store price of one unit before the promo
    private Double unitprice;       // price charged for one unit (after the promo)
    private Double discountrate;    // promo in decimal form (0.10 = 10% off), same as CartItem
    private Integer quantity;
    private HashMap<String, String> specs;

    public BuildPart(String slotkey, Integer productid, String code, String name, Double listprice,
            Double unitprice, Double discountrate, Integer quantity, HashMap<String, String> specs) {
        this.slotkey = slotkey;
        this.productid = productid;
        this.code = code;
        this.name = name;
        this.listprice = listprice;
        this.unitprice = unitprice;
        this.discountrate = discountrate;
        this.quantity = quantity;
        this.specs = (specs == null) ? new HashMap<>() : specs;
    }

    // what the customer pays for this line
    public Double getSubtotal() {
        return Math.round(unitprice * quantity * 100.0) / 100.0;
    }

    // one spec value (lower-case name, e.g. "socket"), or null when the product has no such spec
    public String spec(String specName) {
        return specs.get(specName);
    }

    // a number spec (e.g. "tdp" = "65W" gives 65), or null when it is missing or not a number
    public Integer specInt(String specName) {
        String value = specs.get(specName);
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", ""); // "650W" -> "650"
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSlotkey() {
        return slotkey;
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

    public Double getListprice() {
        return listprice;
    }

    public Double getUnitprice() {
        return unitprice;
    }

    public Double getDiscountrate() {
        return discountrate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public HashMap<String, String> getSpecs() {
        return specs;
    }

}
