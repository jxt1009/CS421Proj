package storagemanager;

import common.Attribute;
import common.Table;

import java.util.ArrayList;
import java.util.Locale;

public class RecordHelper {
    public static boolean compareObjects(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer) o1 < (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double) o1 < (Double) o2;
        }else if (attribute.getAttributeType().equalsIgnoreCase("Boolean")){
            return (boolean) o1 != (boolean) o2;
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) < 0;
        }
        return false;
    }

    public static boolean lessThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer) o1 < (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double) o1 < (Double) o2;
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) < 0;
        }
        return false;
    }

    public static boolean lessThanEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer) o1 <= (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double) o1 <= (Double) o2;
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) <= 0;
        }
        return false;
    }

    public static boolean greaterThan(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer) o1 > (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double) o1 > (Double) o2;
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) > 0;
        }
        return false;
    }

    public static boolean greaterThanEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer) o1 >= (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double) o1 >= (Double) o2;
        } else if (o1 instanceof String || o1 instanceof Character) {
            return ((String) o1).compareTo((String) o2) >= 0;
        }
        return false;
    }

    public static boolean equals(Object o1, Object o2, Attribute attribute) {
        if(o1 == null || o2 == null){
            return false;
        }
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return (Integer)o1 == (Integer) o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return (Double)o1 == (Double)o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("boolean")) {
            return (boolean)o1 == (boolean)o2;
        } else {
            return ((String) o1).strip().equals(((String) o2).strip());
        }
    }

    public static boolean notEquals(Object o1, Object o2, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return o1 != o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return o1 != o2;
        } else if (attribute.getAttributeType().equalsIgnoreCase("boolean")) {
            return o1 != o2;
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

    public static Object returnNumeric(Object o) {
        if(!isNumeric(o)){
            return false;
        }
        try {
            return Integer.parseInt((String) o);
        } catch (NumberFormatException e) {
        }
        try {
            return Double.parseDouble((String) o);
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public static boolean matchesType(Object o, Attribute attribute) {
        if (attribute.getAttributeType().equalsIgnoreCase("integer")) {
            return o instanceof Integer;
        } else if (attribute.getAttributeType().equalsIgnoreCase("double")) {
            return o instanceof Double;
        } else if (attribute.getAttributeType().toLowerCase().startsWith("varchar(")
                || attribute.getAttributeType().toLowerCase().startsWith("char(")) {
            String type = attribute.getAttributeType();
            int charLen = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
            return (((String) o).length() - charLen <= 0);
        } else if (attribute.getAttributeType().equalsIgnoreCase("boolean")) {
            return o instanceof Boolean;
        }
        return false;
    }

    public static ArrayList<Object> formatRecord(Table table, ArrayList<Object> origRecord) {
        for (int i = 0; i < table.getAttributes().size(); i++) {
            Attribute attr = table.getAttributes().get(i);

            Object record = origRecord.get(i);
            String type = attr.getAttributeType();
            if (!table.isNullable(i) && record == null) {
                System.err.println("Null value entered in nonnull column: " + attr.getAttributeName());
                return null;
            } else if (record == null || (record instanceof String && ((String) record).equalsIgnoreCase("null"))) {
                origRecord.set(i, null);
            }

            if (type.equalsIgnoreCase("Integer") && origRecord.get(i) instanceof String) {
                if (((String) origRecord.get(i)).contains(".")) {
                    System.err.println("Double value attempting to be inserted into Integer column");
                    return null;
                }
                origRecord.set(i, Integer.parseInt((String) record));
            } else if (type.equalsIgnoreCase("Double") && origRecord.get(i) instanceof String) {
                origRecord.set(i, Double.parseDouble((String) record));
            } else if (type.equalsIgnoreCase("Boolean") && origRecord.get(i) instanceof String) {
                origRecord.set(i, Boolean.parseBoolean((String) record));
            } else if (type.toLowerCase().startsWith("varchar") || type.toLowerCase().startsWith("char")) {
                int charLen = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                if(((String) record).length() <= charLen) {
                    origRecord.set(i, record);
                }else{
                    System.err.println("Character string contains too many chars");
                    return null;
                }
            }
            if (!RecordHelper.matchesType(origRecord.get(i), attr)) {
                return null;
            }
        }

        return origRecord;
    }

}
