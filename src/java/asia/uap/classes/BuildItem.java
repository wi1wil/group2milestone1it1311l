/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

/**
 * One part in a PC build (build_items), plus the product's current stock
 * so the technician can see if there is enough before finishing the job.
 *
 * @author Andrei
 */
public class BuildItem {

    private Integer builditemid;
    private Integer buildid;
    private Integer productid;
    private String code;
    private String name;
    private Double unitprice;       // price saved when the part was added to the build
    private Integer quantity;
    private Integer stock;          // current products.stock_quantity
    private String source;          // paid or store (see SqlcBuilds)

    public BuildItem(Integer builditemid, Integer buildid, Integer productid, String code, String name,
            Double unitprice, Integer quantity, Integer stock, String source) {
        this.builditemid = builditemid;
        this.buildid = buildid;
        this.productid = productid;
        this.code = code;
        this.name = name;
        this.unitprice = unitprice;
        this.quantity = quantity;
        this.stock = stock;
        this.source = source;
    }

    public Double getSubtotal() {
        return Math.round(unitprice * quantity * 100.0) / 100.0;
    }

    // true for parts the customer paid for with the build order: the technician cannot change them,
    // and they already left the stock when they were paid
    public boolean isCustomerPart() {
        return "paid".equals(source);
    }

    // true when this part is ready: customer parts always are, store parts need enough stock
    public boolean isInStock() {
        return isCustomerPart() || stock >= quantity;
    }

    public String getSource() {
        return source;
    }

    // what the page shows in the Source column
    public String getSourceLabel() {
        if ("paid".equals(source)) {
            return "Paid with order";
        }
        return "Added by technician";
    }

    public Integer getBuilditemid() {
        return builditemid;
    }

    public Integer getBuildid() {
        return buildid;
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

    public Double getUnitprice() {
        return unitprice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getStock() {
        return stock;
    }

}
