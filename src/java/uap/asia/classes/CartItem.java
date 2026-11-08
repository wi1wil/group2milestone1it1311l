/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uap.asia.classes;

import java.sql.*;

/**
 *
 * @author Andrei
 */
public class CartItem {
 
    private Integer productid;
    private Double price;
    private Integer itemquantity;
    private Double itemdiscount;

    public CartItem(Integer productid, Double price, Integer itemquantity, Double itemdiscount) {
        this.productid = productid;
        this.itemquantity = itemquantity;
        this.itemdiscount = itemdiscount;
        
        // percentage in decimal form (i.e. 0.2 for 20%, .5 for 50% etc.)
        if (itemdiscount < 0 && itemdiscount > -1) {
            this.price = price * (1-itemdiscount);
            this.itemdiscount = price * itemdiscount;
        }
        else if (itemdiscount > 0 && itemdiscount <= price) {
            this.price = price - itemdiscount;
        }
        
        else {
            this.price = price;
        }
    }

    public Integer getProductid() {
        return productid;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getItemquantity() {
        return itemquantity;
    }

    public Double getItemdiscount() {
        return itemdiscount;
    }
        
}
