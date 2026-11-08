/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;

/**
 * One row of the suppliers table.
 *
 * @author Andrei
 */
public class Supplier implements Serializable {

    
    private static final long serialVersionUID = 1L;
    
    private Integer supplierid;
    private String name;
    private String contact;

    public Supplier(Integer supplierid, String name, String contact) {
        this.supplierid = supplierid;
        this.name = name;
        this.contact = contact;
    }

    public Integer getSupplierid() {
        return supplierid;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

}
