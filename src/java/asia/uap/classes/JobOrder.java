/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * One row of job_orders, joined with its build (builds) and the build's parts (build_items).
 *
 * Job status values (same words as technician.jsp, plus Cancelled):
 *   Pending     -> In Progress, Cancelled
 *   In Progress -> Pending, Success, Cancelled
 *   Success     -> (final) parts were taken out of stock
 *   Cancelled   -> (final)
 *
 * The build's parts can only be edited while the job is Pending or In Progress.
 *
 * @author Andrei
 */
public class JobOrder {

    public static final String PENDING = "Pending";
    public static final String IN_PROGRESS = "In Progress";
    public static final String SUCCESS = "Success";
    public static final String CANCELLED = "Cancelled";

    // every status, in the order shown in the dropdown
    public static final String[] STATUSES = {PENDING, IN_PROGRESS, SUCCESS, CANCELLED};

    private Integer jobid;
    private Integer buildid;
    private String buildname;
    private String buildstatus;
    private Integer customerid;
    private String customername;
    private Integer employeeid;
    private String technicianname;
    private String status;
    private LocalDateTime createdat;
    private LocalDateTime completedat;
    private ArrayList<BuildItem> items = new ArrayList<>();

    public JobOrder(Integer jobid, Integer buildid, String buildname, String buildstatus, Integer customerid,
            String customername, Integer employeeid, String technicianname, String status,
            LocalDateTime createdat, LocalDateTime completedat) {
        this.jobid = jobid;
        this.buildid = buildid;
        this.buildname = buildname;
        this.buildstatus = buildstatus;
        this.customerid = customerid;
        this.customername = customername;
        this.employeeid = employeeid;
        this.technicianname = technicianname;
        this.status = status;
        this.createdat = createdat;
        this.completedat = completedat;
    }

    // Is moving from one status to another allowed? (see the table at the top)
    public static boolean canChange(String from, String to) {
        if (from == null || to == null || from.equals(to)) {
            return false;
        }
        if (from.equals(PENDING)) {
            return to.equals(IN_PROGRESS) || to.equals(CANCELLED);
        }
        if (from.equals(IN_PROGRESS)) {
            return to.equals(PENDING) || to.equals(SUCCESS) || to.equals(CANCELLED);
        }
        return false; // Success and Cancelled are final
    }

    // true when the status is one of the known values
    public static boolean isValidStatus(String status) {
        for (String s : STATUSES) {
            if (s.equals(status)) {
                return true;
            }
        }
        return false;
    }

    // builds.status is only varchar(10), so it gets a short word for each job status
    public static String buildStatusFor(String jobStatus) {
        if (IN_PROGRESS.equals(jobStatus)) {
            return "building";
        } else if (SUCCESS.equals(jobStatus)) {
            return "completed";
        } else if (CANCELLED.equals(jobStatus)) {
            return "cancelled";
        }
        return "pending";
    }

    // The parts can only be changed while the job is still open
    public boolean isEditable() {
        return PENDING.equals(status) || IN_PROGRESS.equals(status);
    }

    // Job number shown on the pages, e.g. JO-001
    public String getJobNumber() {
        return String.format("JO-%03d", jobid);
    }

    // total price of all the parts
    public Double getTotal() {
        double total = 0;
        for (BuildItem item : items) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public Integer getJobid() {
        return jobid;
    }

    public Integer getBuildid() {
        return buildid;
    }

    public String getBuildname() {
        return buildname;
    }

    public String getBuildstatus() {
        return buildstatus;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public String getCustomername() {
        return customername;
    }

    public Integer getEmployeeid() {
        return employeeid;
    }

    public String getTechnicianname() {
        return technicianname;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public LocalDateTime getCompletedat() {
        return completedat;
    }

    public ArrayList<BuildItem> getItems() {
        return items;
    }

}
