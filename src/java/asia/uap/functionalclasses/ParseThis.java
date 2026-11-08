/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asia.uap.functionalclasses;

/**
 * Turns form text into numbers without throwing exceptions.
 *
 * EDITED: toFloat(null) and toDouble(null) used to return the secret "no default"
 * number (-0.1234567 / -1.2345678987654321) instead of null. They now return null,
 * the same as toInt(null). The comments now say what the methods really return.
 *
 * @author Andrei
 */
public class ParseThis {

    // the "no default given" markers used by the one-argument versions
    private static final int NO_INT = -1234567890;
    private static final float NO_FLOAT = -.1234567f;
    private static final double NO_DOUBLE = -1.2345678987654321;

    /** Returns the whole number in the text, or defaultValue if invalid. */
    public static Integer toInt(String text, int defaultValue) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            if (defaultValue <= NO_INT) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns null when the text is invalid. */
    public static Integer toInt(String text) {
        return toInt(text, NO_INT);
    }

    /** True if the text is a valid whole number. */
    public static boolean isInt(String text) {
        if (text == null) {
            return false;
        }
        try {
            Integer.parseInt(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns the decimal number in the text, or defaultValue if invalid. */
    public static Float toFloat(String text, float defaultValue) {
        if (text == null) {
            return (defaultValue <= NO_FLOAT) ? null : defaultValue;
        }
        try {
            float value = Float.parseFloat(text.trim());
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return (defaultValue <= NO_FLOAT) ? null : defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            if (defaultValue <= NO_FLOAT) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns null when the text is invalid. */
    public static Float toFloat(String text) {
        return toFloat(text, NO_FLOAT);
    }

    /** True if the text is a valid decimal number. */
    public static boolean isFloat(String text) {
        if (text == null) {
            return false;
        }
        try {
            float value = Float.parseFloat(text.trim());
            return !Float.isNaN(value) && !Float.isInfinite(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ------------------------------------------------------------- double
    // Same idea as float. Your price fields use double, so it is included.

    /** Returns the double in the text, or defaultValue if invalid. */
    public static Double toDouble(String text, double defaultValue) {
        if (text == null) {
            return (defaultValue <= NO_DOUBLE) ? null : defaultValue;
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return (defaultValue <= NO_DOUBLE) ? null : defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            if (defaultValue <= NO_DOUBLE) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns null when the text is invalid. */
    public static Double toDouble(String text) {
        return toDouble(text, NO_DOUBLE);
    }

    /** True if the text is a valid decimal number. */
    public static boolean isDouble(String text) {
        if (text == null) {
            return false;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return !Double.isNaN(value) && !Double.isInfinite(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** True if the text is null or only spaces (an empty form field). */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean toBoolean(String text, boolean defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        String value = text.trim().toLowerCase();
        if (value.equals("true") || value.equals("yes") || value.equals("1") || value.equals("on")) {
            return true;
        }
        if (value.equals("false") || value.equals("no") || value.equals("0") || value.equals("off")) {
            return false;
        }
        return defaultValue;
    }

    /** Same as above, but returns false when the text is not a recognised value. */
    public static boolean toBoolean(String text) {
        return toBoolean(text, false);
    }

    /** True if the text is one of the recognised true/false values. */
    public static boolean isBoolean(String text) {
        if (text == null) {
            return false;
        }
        String value = text.trim().toLowerCase();
        return value.equals("true") || value.equals("yes") || value.equals("1") || value.equals("on")
                || value.equals("false") || value.equals("no") || value.equals("0") || value.equals("off");
    }

    /**
     * Reads an ID typed with or without its prefix, e.g. "PO-007", "po7" or "7" all give 7.
     * Returns null when there is no number in it.
     */
    public static Integer toId(String text, String prefix) {
        if (text == null) {
            return null;
        }
        String value = text.trim().toUpperCase();
        if (value.startsWith(prefix.toUpperCase())) {
            value = value.substring(prefix.length());
        }
        if (value.startsWith("-")) {
            value = value.substring(1);
        }
        return toInt(value);
    }
}
