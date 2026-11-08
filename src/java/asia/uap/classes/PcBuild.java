/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The PC build being prepared in the PC builder (session attribute "pcBuild").
 * Works like the cashier's cart: nothing is saved until the build is ordered.
 *
 * parts: slot key -> chosen part (one part per slot, see BuildSlot)
 * customer: who the build is for. A logged-in customer is set automatically;
 *           staff type the customer's username.
 *
 * @author Andrei
 */
public class PcBuild implements Serializable {

    // keeps saved sessions readable after this class is recompiled (Tomcat saves sessions on restart)
    private static final long serialVersionUID = 1L;

    private HashMap<String, BuildPart> parts = new HashMap<>();
    private Integer customerid;
    private String customername;
    // EDITED: the saved quote this build was loaded from (null = a new build).
    // Saving again updates that quote; ordering it turns the quote into the real build.
    private Integer quoteid;
    private String quotename;

    public BuildPart get(String slotkey) {
        return parts.get(slotkey);
    }

    public void put(BuildPart part) {
        parts.put(part.getSlotkey(), part);
    }

    public void remove(String slotkey) {
        parts.remove(slotkey);
    }

    // empties the build and forgets the quote it came from
    public void clear() {
        parts.clear();
        quoteid = null;
        quotename = null;
    }

    public Integer getQuoteid() {
        return quoteid;
    }

    public String getQuotename() {
        return quotename;
    }

    public void setQuote(Integer quoteid, String quotename) {
        this.quoteid = quoteid;
        this.quotename = quotename;
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public HashMap<String, BuildPart> getParts() {
        return parts;
    }

    // the chosen parts in the same order as BuildSlot.SLOTS
    public ArrayList<BuildPart> getPartList() {
        ArrayList<BuildPart> list = new ArrayList<>();
        for (BuildSlot slot : BuildSlot.SLOTS) {
            if (parts.containsKey(slot.getKey())) {
                list.add(parts.get(slot.getKey()));
            }
        }
        return list;
    }

    // labels of the required slots that are still empty ("" when none are missing)
    public String getMissingRequired() {
        String missing = "";
        for (BuildSlot slot : BuildSlot.SLOTS) {
            if (slot.isRequired() && !parts.containsKey(slot.getKey())) {
                missing += (missing.isEmpty() ? "" : ", ") + slot.getLabel();
            }
        }
        return missing;
    }

    // total to pay for every part
    public Double getTotal() {
        double total = 0;
        for (BuildPart part : parts.values()) {
            total += part.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomer(Integer customerid, String customername) {
        this.customerid = customerid;
        this.customername = customername;
    }

}
