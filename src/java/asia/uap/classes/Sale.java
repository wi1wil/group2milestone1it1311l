/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * One row of the sales table with its sale_items.
 * Used for the receipt (cashierreceipt.java) and the refund page (refundtransaction.java).
 *
 * @author Andrei
 */
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer saleid;
    private Integer employeeid;
    private String cashiername;
    private Integer customerid;
    private String customername;
    private LocalDateTime saledate;
    private Double discount;        // total peso discount of the whole sale
    private Double total;           // amount due after the discount
    private Double amountpaid;
    private String paymentmethod;
    private ArrayList<SaleItem> items = new ArrayList<>();

    public Sale(Integer saleid, Integer employeeid, String cashiername, Integer customerid, String customername,
            LocalDateTime saledate, Double discount, Double total, Double amountpaid, String paymentmethod) {
        this.saleid = saleid;
        this.employeeid = employeeid;
        this.cashiername = cashiername;
        this.customerid = customerid;
        this.customername = customername;
        this.saledate = saledate;
        this.discount = discount;
        this.total = total;
        this.amountpaid = amountpaid;
        this.paymentmethod = paymentmethod;
    }

    // receipt number shown to the customer, e.g. TX-0007
    public String getSaleNumber() {
        return String.format("TX-%04d", saleid);
    }

    // change given back to the customer
    public Double getChange() {
        return Math.round((amountpaid - total) * 100.0) / 100.0;
    }

    // total before the discount
    public Double getSubtotal() {
        return Math.round((total + discount) * 100.0) / 100.0;
    }

    public Integer getSaleid() {
        return saleid;
    }

    public Integer getEmployeeid() {
        return employeeid;
    }

    public String getCashiername() {
        return cashiername;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public String getCustomername() {
        return customername;
    }

    public LocalDateTime getSaledate() {
        return saledate;
    }

    public Double getDiscount() {
        return discount;
    }

    public Double getTotal() {
        return total;
    }

    public Double getAmountpaid() {
        return amountpaid;
    }

    public String getPaymentmethod() {
        return paymentmethod;
    }

    public ArrayList<SaleItem> getItems() {
        return items;
    }

    // finds one line by its sale_item_id (null when it is not part of this sale)
    public SaleItem findItem(int saleitemid) {
        for (SaleItem item : items) {
            if (item.getSaleitemid() == saleitemid) {
                return item;
            }
        }
        return null;
    }

}
