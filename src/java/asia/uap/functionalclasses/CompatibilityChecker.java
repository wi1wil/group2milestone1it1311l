/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

import java.util.ArrayList;
import java.util.HashMap;
import asia.uap.classes.BuildPart;
import asia.uap.classes.CompatibilityResult;

/**
 * Checks if the parts of a PC build work together, using the specs in product_specs.
 * No database here: the specs are already inside each BuildPart.
 *
 * Spec names used (product_specs.spec_name, any upper/lower case):
 *   CPU          socket, tdp, igpu (yes/no), cooler_included (yes/no)
 *   Motherboard  socket, memory_type, form_factor, ram_slots
 *   RAM          memory_type, modules (sticks in one box, default 1)
 *   GPU          tdp
 *   Power supply wattage
 *   Case         form_factor (the BIGGEST board it fits)
 *   CPU cooler   sockets (comma list, e.g. "AM5,AM4,LGA1700"), max_tdp
 *
 * A rule only runs when its parts are chosen. A missing spec gives WARN (not checked), never FAIL.
 *
 * @author Andrei
 */
public class CompatibilityChecker {

    // extra watts for the motherboard, RAM, drives and fans
    private static final int BASE_WATTS = 100;
    // recommended extra room on top of the needed watts (20%)
    private static final double HEADROOM = 1.2;

    /**
     * Runs every rule on the chosen parts.
     * parts = slot key -> part (PcBuild.getParts()).
     */
    public static ArrayList<CompatibilityResult> check(HashMap<String, BuildPart> parts) {
        ArrayList<CompatibilityResult> results = new ArrayList<>();
        BuildPart cpu = parts.get("cpu");
        BuildPart mb = parts.get("motherboard");
        BuildPart ram = parts.get("ram");
        BuildPart gpu = parts.get("gpu");
        BuildPart psu = parts.get("psu");
        BuildPart pcCase = parts.get("case");
        BuildPart cooler = parts.get("cooler");

        // 1. CPU socket must match the motherboard socket
        if (cpu != null && mb != null) {
            results.add(sameText("CPU socket", cpu.spec("socket"), mb.spec("socket"),
                    "The CPU (" + cpu.spec("socket") + ") fits the motherboard socket (" + mb.spec("socket") + ").",
                    "The CPU uses socket " + cpu.spec("socket") + " but the motherboard is " + mb.spec("socket") + ".",
                    "cpu,motherboard"));
        }

        // 2. RAM type must match the motherboard (DDR4 / DDR5)
        if (ram != null && mb != null) {
            results.add(sameText("Memory type", ram.spec("memory_type"), mb.spec("memory_type"),
                    "The RAM (" + ram.spec("memory_type") + ") matches the motherboard.",
                    "The motherboard needs " + mb.spec("memory_type") + " but the RAM is " + ram.spec("memory_type") + ".",
                    "ram,motherboard"));
        }

        // 3. Number of RAM sticks must fit the motherboard's slots
        if (ram != null && mb != null) {
            Integer slots = mb.specInt("ram_slots");
            Integer perBox = ram.specInt("modules");
            int sticks = ram.getQuantity() * (perBox == null ? 1 : perBox);
            if (slots == null) {
                results.add(warnMissing("RAM slots", "the motherboard's ram_slots", "ram,motherboard"));
            } else if (sticks > slots) {
                results.add(new CompatibilityResult("RAM slots", CompatibilityResult.FAIL,
                        sticks + " RAM sticks but the motherboard only has " + slots + " slots.", "ram,motherboard"));
            } else {
                results.add(new CompatibilityResult("RAM slots", CompatibilityResult.PASS,
                        sticks + " of " + slots + " RAM slots used.", "ram,motherboard"));
            }
        }

        // 4. The motherboard must fit in the case (ITX < mATX < ATX < E-ATX)
        if (mb != null && pcCase != null) {
            int board = formFactorSize(mb.spec("form_factor"));
            int box = formFactorSize(pcCase.spec("form_factor"));
            if (board == 0 || box == 0) {
                results.add(warnMissing("Case size", "the form_factor of the motherboard or case", "motherboard,case"));
            } else if (board > box) {
                results.add(new CompatibilityResult("Case size", CompatibilityResult.FAIL,
                        "A " + mb.spec("form_factor") + " motherboard does not fit a case made for up to "
                        + pcCase.spec("form_factor") + ".", "motherboard,case"));
            } else {
                results.add(new CompatibilityResult("Case size", CompatibilityResult.PASS,
                        "The " + mb.spec("form_factor") + " motherboard fits the case (up to " + pcCase.spec("form_factor") + ").",
                        "motherboard,case"));
            }
        }

        // 5. The power supply must cover the CPU + graphics card + the rest
        if (psu != null && cpu != null) {
            Integer watts = psu.specInt("wattage");
            Integer cpuTdp = cpu.specInt("tdp");
            Integer gpuTdp = (gpu == null) ? Integer.valueOf(0) : gpu.specInt("tdp");
            if (watts == null || cpuTdp == null || gpuTdp == null) {
                results.add(warnMissing("Power supply", "the PSU wattage or the CPU / GPU tdp", "psu,cpu,gpu"));
            } else {
                int needed = cpuTdp + gpuTdp + BASE_WATTS;
                int recommended = (int) Math.ceil(needed * HEADROOM);
                if (watts < needed) {
                    results.add(new CompatibilityResult("Power supply", CompatibilityResult.FAIL,
                            "The build needs about " + needed + "W but the power supply gives " + watts + "W.", "psu,cpu,gpu"));
                } else if (watts < recommended) {
                    results.add(new CompatibilityResult("Power supply", CompatibilityResult.WARN,
                            watts + "W is enough for about " + needed + "W, but " + recommended + "W or more is recommended.",
                            "psu,cpu,gpu"));
                } else {
                    results.add(new CompatibilityResult("Power supply", CompatibilityResult.PASS,
                            watts + "W covers the build (about " + needed + "W needed).", "psu,cpu,gpu"));
                }
            }
        }

        // 6. Without a graphics card, the CPU must have built-in graphics
        if (cpu != null && gpu == null) {
            String igpu = cpu.spec("igpu");
            if (igpu == null) {
                results.add(warnMissing("Display output", "the CPU's igpu (built-in graphics)", "cpu,gpu"));
            } else if (isYes(igpu)) {
                results.add(new CompatibilityResult("Display output", CompatibilityResult.PASS,
                        "No graphics card: the CPU's built-in graphics will be used.", "cpu,gpu"));
            } else {
                results.add(new CompatibilityResult("Display output", CompatibilityResult.FAIL,
                        "The CPU has no built-in graphics, so the build needs a graphics card.", "cpu,gpu", true));
            }
        }

        // 7. CPU cooler: it must fit the CPU socket and handle its heat.
        //    Without one, the CPU must come with its own cooler.
        if (cpu != null && cooler != null) {
            String sockets = cooler.spec("sockets");
            String cpuSocket = cpu.spec("socket");
            if (sockets == null || cpuSocket == null) {
                results.add(warnMissing("CPU cooler", "the cooler's sockets or the CPU socket", "cooler,cpu"));
            } else if (!listContains(sockets, cpuSocket)) {
                results.add(new CompatibilityResult("CPU cooler", CompatibilityResult.FAIL,
                        "The cooler fits " + sockets + ", not the CPU's " + cpuSocket + ".", "cooler,cpu"));
            } else {
                Integer maxTdp = cooler.specInt("max_tdp");
                Integer cpuTdp = cpu.specInt("tdp");
                if (maxTdp != null && cpuTdp != null && cpuTdp > maxTdp) {
                    results.add(new CompatibilityResult("CPU cooler", CompatibilityResult.FAIL,
                            "The cooler handles up to " + maxTdp + "W but the CPU makes " + cpuTdp + "W.", "cooler,cpu"));
                } else {
                    results.add(new CompatibilityResult("CPU cooler", CompatibilityResult.PASS,
                            "The cooler fits the " + cpuSocket + " socket.", "cooler,cpu"));
                }
            }
        } else if (cpu != null) {
            String included = cpu.spec("cooler_included");
            if (included == null) {
                results.add(warnMissing("CPU cooler", "the CPU's cooler_included", "cooler,cpu"));
            } else if (isYes(included)) {
                results.add(new CompatibilityResult("CPU cooler", CompatibilityResult.PASS,
                        "No cooler chosen: the CPU's own (stock) cooler will be used.", "cooler,cpu"));
            } else {
                results.add(new CompatibilityResult("CPU cooler", CompatibilityResult.FAIL,
                        "The CPU does not come with a cooler. Choose a CPU cooler.", "cooler,cpu", true));
            }
        }

        return results;
    }

    // true when no rule failed
    public static boolean isCompatible(ArrayList<CompatibilityResult> results) {
        for (CompatibilityResult r : results) {
            if (r.isFail()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used by the picker: would this candidate part FAIL with the parts already chosen?
     * Returns the first failure message about the candidate's slot, or null when it fits.
     * Failures fixed by ADDING a part (graphics card, cooler) are not counted here: see needs().
     */
    public static String whyNot(HashMap<String, BuildPart> parts, BuildPart candidate) {
        for (CompatibilityResult r : failsWith(parts, candidate)) {
            if (!r.isNeedsPart()) {
                return r.getMessage();
            }
        }
        return null;
    }

    /**
     * Used by the picker: does this candidate need another part to work (e.g. a CPU
     * without graphics needs a graphics card)? Returns that message, or null.
     */
    public static String needs(HashMap<String, BuildPart> parts, BuildPart candidate) {
        for (CompatibilityResult r : failsWith(parts, candidate)) {
            if (r.isNeedsPart()) {
                return r.getMessage();
            }
        }
        return null;
    }

    // the failed rules about the candidate's slot, if the candidate were put in the build
    private static ArrayList<CompatibilityResult> failsWith(HashMap<String, BuildPart> parts, BuildPart candidate) {
        HashMap<String, BuildPart> test = new HashMap<>(parts);
        test.put(candidate.getSlotkey(), candidate);
        ArrayList<CompatibilityResult> fails = new ArrayList<>();
        for (CompatibilityResult r : check(test)) {
            if (r.isFail() && r.involves(candidate.getSlotkey())) {
                fails.add(r);
            }
        }
        return fails;
    }

    // PASS when both texts are the same (upper/lower case does not matter), FAIL when not, WARN when missing
    private static CompatibilityResult sameText(String rule, String a, String b, String passText, String failText, String slots) {
        if (a == null || b == null) {
            return warnMissing(rule, "a needed spec", slots);
        }
        if (a.trim().equalsIgnoreCase(b.trim())) {
            return new CompatibilityResult(rule, CompatibilityResult.PASS, passText, slots);
        }
        return new CompatibilityResult(rule, CompatibilityResult.FAIL, failText, slots);
    }

    private static CompatibilityResult warnMissing(String rule, String what, String slots) {
        return new CompatibilityResult(rule, CompatibilityResult.WARN,
                "Not checked: " + what + " is not in the product specs.", slots);
    }

    // Board sizes from small to big. 0 = unknown.
    private static int formFactorSize(String value) {
        if (value == null) {
            return 0;
        }
        String v = value.trim().toUpperCase().replace("-", "").replace(" ", "");
        if (v.equals("ITX") || v.equals("MINIITX")) {
            return 1;
        } else if (v.equals("MATX") || v.equals("MICROATX")) {
            return 2;
        } else if (v.equals("ATX")) {
            return 3;
        } else if (v.equals("EATX")) {
            return 4;
        }
        return 0;
    }

    private static boolean isYes(String value) {
        return ParseThis.toBoolean(value, false);
    }

    // "AM5, AM4,LGA1700" contains "am4"
    private static boolean listContains(String list, String item) {
        for (String s : list.split(",")) {
            if (s.trim().equalsIgnoreCase(item.trim())) {
                return true;
            }
        }
        return false;
    }
}
