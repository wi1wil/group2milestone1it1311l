/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * One row of the purchase_orders table with its purchase_order_items.
 *
 * Status values (purchase_orders.status is varchar(15)):
 *   Ordered   = created by inventory staff, waiting for the delivery and admin approval
 *   Received  = admin approved the delivery, stock was added
 *   Cancelled = admin cancelled the order, stock was not changed
 *
 * @author Andrei
 */
public class PurchaseOrder {

    public static final String STATUS_ORDERED = "Ordered";
    public static final String STATUS_RECEIVED = "Received";
    public static final String STATUS_CANCELLED = "Cancelled";

    private Integer poid;
    private Integer supplierid;
    private String suppliername;
    private Integer createdby;
    private String createdbyname;
    private LocalDate orderdate;
    private String status;
    private ArrayList<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder(Integer poid, Integer supplierid, String suppliername, Integer createdby,
            String createdbyname, LocalDate orderdate, String status) {
        this.poid = poid;
        this.supplierid = supplierid;
        this.suppliername = suppliername;
        this.createdby = createdby;
        this.createdbyname = createdbyname;
        this.orderdate = orderdate;
        this.status = status;
    }

    // Same PO number format as the old createpurchaseorder and viewpurchaseorders (PO-001)
    public String getPoNumber() {
        return String.format("PO-%03d", poid);
    }

    // total cost of all the lines
    public Double getTotalCost() {
        double total = 0;
        for (PurchaseOrderItem item : items) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public boolean isOrdered() {
        return STATUS_ORDERED.equalsIgnoreCase(status);
    }

    public Integer getPoid() {
        return poid;
    }

    public Integer getSupplierid() {
        return supplierid;
    }

    public String getSuppliername() {
        return suppliername;
    }

    public Integer getCreatedby() {
        return createdby;
    }

    public String getCreatedbyname() {
        return createdbyname;
    }

    public LocalDate getOrderdate() {
        return orderdate;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<PurchaseOrderItem> getItems() {
        return items;
    }

}
