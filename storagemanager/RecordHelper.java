package storagemanager;

import common.Attribute;

import java.util.Locale;

public class RecordHelper {
    public static boolean compareObjects(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) < Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) < Double.parseDouble((String) o2);
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) < 0;
        }
        return false;
    }

    public static boolean lessThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) < Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) < Double.parseDouble((String) o2);
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) < 0;
        }
        return false;
    }

    public static boolean lessThanEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) <= Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) <= Double.parseDouble((String) o2);
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) <= 0;
        }
        return false;
    }

    public static boolean greaterThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) > Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) > Double.parseDouble((String) o2);
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) > 0;
        }
        return false;
    }

    public static boolean greaterThanEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) >= Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) >= Double.parseDouble((String) o2);
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) >= 0;
        }
        return false;
    }

    public static boolean equals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) == Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) == Double.parseDouble((String) o2);
        } else {
            return ((String) o1).strip().equals(((String) o2).strip());
        }
    }

    public static boolean notEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return Integer.parseInt((String) o1) != Integer.parseInt((String) o2);
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return Double.parseDouble((String) o1) != Double.parseDouble((String) o2);
        } else {
            return !((String) o1).strip().equals(((String) o2).strip());
        }
    }

    public static boolean isNumeric(Object o) {
        try {
            Integer.parseInt((String) o);
            return true;
        } catch (NumberFormatException e) {
        }
        try {
            Double.parseDouble((String) o);
            return true;
        } catch (NumberFormatException e) {
        }
        try {
            Float.parseFloat((String) o);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public static boolean matchesType(Object o, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            try {
                Integer.parseInt((String) o);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            try {
                Double.parseDouble((String) o);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (attribute.getAttributeType().toLowerCase().startsWith("varchar(")
                || attribute.getAttributeType().toLowerCase().startsWith("char(")) {
            String type = attribute.getAttributeType();
            int charLen = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
            return ((String) o).length() <= charLen;
        } else if (attribute.getAttributeType().equalsIgnoreCase("boolean")) {
            return ((String) o).equalsIgnoreCase("true") || ((String) o).equalsIgnoreCase("false");
        }
        return false;
    }

}
