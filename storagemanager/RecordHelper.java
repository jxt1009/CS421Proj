package storagemanager;

import common.Attribute;

public class RecordHelper {
    public static boolean compareObjects(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            if (Integer.parseInt((String) o1) < Integer.parseInt((String) o2)) {
                return true;
            }
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            if (Double.parseDouble((String) o1) < Double.parseDouble((String) o2)) {
                return true;
            }
        } else if (o1 instanceof String || o1 instanceof Character) {
            if (((String) o1).compareTo((String) o2) < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean lessThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            if (Integer.parseInt((String) o1) < Integer.parseInt((String) o2)) {
                return true;
            }
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            if (Double.parseDouble((String) o1) < Double.parseDouble((String) o2)) {
                return true;
            }
        } else if (o1 instanceof String || o1 instanceof Character) {
            if (((String) o1).compareTo((String) o2) < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean greaterThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            if (Integer.parseInt((String) o1) > Integer.parseInt((String) o2)) {
                return true;
            }
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            if (Double.parseDouble((String) o1) > Double.parseDouble((String) o2)) {
                return true;
            }
        } else if (o1 instanceof String || o1 instanceof Character) {
            if (((String) o1).compareTo((String) o2) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            if (Integer.parseInt((String) o1) == Integer.parseInt((String) o2)) {
                return true;
            }
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            if (Double.parseDouble((String) o1) == Double.parseDouble((String) o2)) {
                return true;
            }
        } else {
            if (((String) o1).strip().equals(((String) o2).strip())) {
                return true;
            }
        }
        return false;
    }
}
