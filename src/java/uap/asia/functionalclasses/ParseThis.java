/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uap.asia.functionalclasses;

/**
 *
 * @author Andrei
 */
public class ParseThis {
    public static Integer toInt(String text, int defaultValue) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            if (defaultValue <= -1234567890) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns 0 when the text is invalid. */
    public static Integer toInt(String text) {
        return toInt(text, -1234567890);
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

    
    public static Float toFloat(String text, float defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        try {
            float value = Float.parseFloat(text.trim());
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            if (defaultValue <= -.1234567f) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns 0 when the text is invalid. */
    public static Float toFloat(String text) {
        return toFloat(text, -.1234567f);
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
            return defaultValue;
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            if (defaultValue <= -1.2345678987654321) {
                return null;
            }
            else return defaultValue;
        }
    }

    /** Same as above, but returns 0 when the text is invalid. */
    public static Double toDouble(String text) {
        return toDouble(text, -1.2345678987654321);
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
}