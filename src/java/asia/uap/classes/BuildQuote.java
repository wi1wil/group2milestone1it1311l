/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.time.LocalDateTime;

/**
 * A PC build a customer saved in the PC builder so the cashier can order it
 * (a builds row with status "quote": no sale, no job order, no stock change yet).
 * Shown in the "saved builds" lists of pcbuilder.java.
 *
 * total = the price of the parts when the quote was saved; the cashier is
 * charged today's price when the quote is loaded and ordered.
 *
 * @author Andrei
 */
public class BuildQuote {

    private Integer buildid;
    private String buildname;
    private Integer customerid;
    private String customername;
    private String username;
    private LocalDateTime savedat;
    private Integer partcount;
    private Double total;

    public BuildQuote(Integer buildid, String buildname, Integer customerid, String customername, String username,
            LocalDateTime savedat, Integer partcount, Double total) {
        this.buildid = buildid;
        this.buildname = buildname;
        this.customerid = customerid;
        this.customername = customername;
        this.username = username;
        this.savedat = savedat;
        this.partcount = partcount;
        this.total = total;
    }

    // quote number shown on the pages, e.g. Q-012
    public String getQuoteNumber() {
        return String.format("Q-%03d", buildid);
    }

    public Integer getBuildid() {
        return buildid;
    }

    public String getBuildname() {
        return buildname;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public String getCustomername() {
        return customername;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getSavedat() {
        return savedat;
    }

    public Integer getPartcount() {
        return partcount;
    }

    public Double getTotal() {
        return total;
    }

}
