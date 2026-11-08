/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;

/**
 * One row of the sale_items table (a product that was paid for),
 * plus how many of it were already refunded (from the refunds table).
 *
 * unitprice is the price the customer really paid for one unit
 * (after the promo discount), so a refund gives back exactly that amount.
 *
 * @author Andrei
 */
public class SaleItem implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer saleitemid;
    private Integer saleid;
    private Integer productid;
    private String code;
    private String name;
    private Integer quantity;
    private Double unitprice;
    private Integer refundedquantity;

    public SaleItem(Integer saleitemid, Integer saleid, Integer productid, String code, String name,
            Integer quantity, Double unitprice, Integer refundedquantity) {
        this.saleitemid = saleitemid;
        this.saleid = saleid;
        this.productid = productid;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.unitprice = unitprice;
        this.refundedquantity = refundedquantity;
    }

    // how many can still be returned (sold minus already refunded)
    public Integer getRefundableQuantity() {
        return quantity - refundedquantity;
    }

    // line total that was paid
    public Double getSubtotal() {
        return Math.round(unitprice * quantity * 100.0) / 100.0;
    }

    public Integer getSaleitemid() {
        return saleitemid;
    }

    public Integer getSaleid() {
        return saleid;
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

    public Integer getQuantity() {
        return quantity;
    }

    public Double getUnitprice() {
        return unitprice;
    }

    public Integer getRefundedquantity() {
        return refundedquantity;
    }

}
