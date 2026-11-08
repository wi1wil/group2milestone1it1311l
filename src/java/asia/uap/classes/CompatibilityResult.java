/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

/**
 * The result of ONE compatibility rule (see CompatibilityChecker).
 *
 * status:
 *   PASS = the parts fit together
 *   FAIL = the parts do not fit; the build cannot be ordered
 *   WARN = could not be checked (a spec is missing) or works but is not recommended
 *
 * slots = the slot keys this rule is about (e.g. "cpu,motherboard"), so the picker
 * can show which cards would fail with the parts already chosen.
 *
 * @author Andrei
 */
public class CompatibilityResult {

    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";
    public static final String WARN = "WARN";

    private String rule;
    private String status;
    private String message;
    private String slots;
    private boolean needspart;  // true when the fix is ADDING a part (e.g. a graphics card), not swapping one

    public CompatibilityResult(String rule, String status, String message, String slots) {
        this(rule, status, message, slots, false);
    }

    public CompatibilityResult(String rule, String status, String message, String slots, boolean needspart) {
        this.rule = rule;
        this.status = status;
        this.message = message;
        this.slots = slots;
        this.needspart = needspart;
    }

    public boolean isFail() {
        return FAIL.equals(status);
    }

    // true for a FAIL that is fixed by adding an optional part, not by changing one
    public boolean isNeedsPart() {
        return needspart;
    }

    // true when this rule is about the given slot
    public boolean involves(String slotkey) {
        for (String s : slots.split(",")) {
            if (s.equals(slotkey)) {
                return true;
            }
        }
        return false;
    }

    public String getRule() {
        return rule;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSlots() {
        return slots;
    }

}
