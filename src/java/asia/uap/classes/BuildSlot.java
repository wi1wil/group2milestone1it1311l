/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.classes;

/**
 * One kind of part in the PC builder (CPU, motherboard, RAM, ...).
 * Each slot takes products from ONE category of the categories table.
 *
 * To add a new kind of part, add one line to SLOTS.
 *
 * @author Andrei
 */
public class BuildSlot {

    //                                    key          label           category name    required  max qty
    public static final BuildSlot[] SLOTS = {
        new BuildSlot("cpu",         "CPU",          "CPU",           true,     1),
        new BuildSlot("motherboard", "Motherboard",  "Motherboard",   true,     1),
        new BuildSlot("ram",         "RAM",          "RAM",           true,     4),
        new BuildSlot("gpu",         "Graphics card", "GPU",          false,    1),
        new BuildSlot("storage",     "Storage",      "Storage",       true,     4),
        new BuildSlot("psu",         "Power supply", "Power Supply",  true,     1),
        new BuildSlot("case",        "Case",         "Case",          true,     1),
        new BuildSlot("cooler",      "CPU cooler",   "CPU Cooler",    false,    1)
    };

    private String key;         // used in forms and URLs, e.g. pcbuilder.jsp?open=cpu
    private String label;       // shown on the page
    private String category;    // categories.name of the products for this slot
    private boolean required;   // the build cannot be ordered without it
    private int maxqty;         // RAM sticks and drives can be more than 1

    public BuildSlot(String key, String label, String category, boolean required, int maxqty) {
        this.key = key;
        this.label = label;
        this.category = category;
        this.required = required;
        this.maxqty = maxqty;
    }

    // The slot with this key, or null
    public static BuildSlot find(String key) {
        if (key == null) {
            return null;
        }
        for (BuildSlot slot : SLOTS) {
            if (slot.key.equals(key)) {
                return slot;
            }
        }
        return null;
    }

    // The slot whose products come from this category name (e.g. "Power Supply" -> psu), or null
    public static BuildSlot findByCategory(String category) {
        if (category == null) {
            return null;
        }
        for (BuildSlot slot : SLOTS) {
            if (slot.category.equalsIgnoreCase(category)) {
                return slot;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public boolean isRequired() {
        return required;
    }

    public int getMaxqty() {
        return maxqty;
    }

}
