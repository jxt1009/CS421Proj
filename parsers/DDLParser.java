package parsers;

import catalog.ACatalog;
import common.Attribute;
import common.ForeignKey;
import common.ITable;
import common.Table;
import storagemanager.AStorageManager;

import java.awt.desktop.SystemEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/*
  Class for DDL parser

  This class is responsible for parsing DDL statements

  You will implement the parseDDLStatement function.
  You can add helper functions as needed, but the must be private and static.

  @author Scott C Johnson (sxjcs@rit.edu)

 */
public class DDLParser {

    private static ACatalog catalog = ACatalog.getCatalog();
    private static AStorageManager sm = AStorageManager.getStorageManager();

    public static boolean parseDropClause(String tableName) {
        if (!catalog.containsTable(tableName)) {
            System.err.println("Table " + tableName + " does not exist in catalog");
            return false;
        }
        boolean didDrop = catalog.dropTable(tableName);
        if (didDrop) {
            System.out.println("Table " + tableName + " dropped successfully");
        } else {
            System.err.println("Table " + tableName + " could not be dropped");
        }
        return didDrop;
    }

    /**
     * This function will parse and execute DDL statements (create table, create index, etc)
     *
     * @param stmt the statement to parse
     * @return true if successfully parsed/executed; false otherwise
     */
    public static boolean parseDDLStatement(String stmt) {
        stmt = stmt.strip();
        if (stmt.toLowerCase().startsWith("create table")) { // Create statement
            return parseCreateClause(stmt);
        } else if (stmt.toLowerCase().startsWith("drop table")) {
            return parseDropClause(stmt.split(" ")[2].replace(";", "").strip());
        }
        //to do alter table <instruction> stuff
        else if (stmt.toLowerCase().startsWith("alter table")){
            return parseAlterClause(stmt);
        }
        return true;
    }


    /**
     * the method which the alter table part calls in the DMLParser method
     * @param stmt
     * @return
     */
    private static boolean parseAlterClause(String stmt){
        String tableName = stmt.split("\\(")[0].split(" ")[2];
        Table table = (Table) catalog.getTable(tableName);
        if (!catalog.containsTable(tableName)) {
            System.err.println("Table " + tableName + " does not exist in catalog");
            return false;
        }

        String instruction = stmt.split("\\(")[0].split(" ")[3];
        String attributeName = stmt.split("\\(")[0].split(" ")[4];

        if(instruction.equals("add")){
            //eg. alter table foo add name varchar(20);
            String attributeType = stmt.split("\\(")[0].split(" ")[5];
            table.addAttribute(attributeName, attributeType);
            boolean success = false;
            if(stmt.contains("default")) {
                Object value = stmt.split("default")[1].strip();
                success = sm.addAttributeValue(table, value);   //adds value in each tuple
            }else{
                success = sm.addAttributeValue(table, null);   //adds null in each tuple
            }
            return success;
        }
        else if(instruction.equals("drop")){
            //eg. alter table foo drop name;
            table.dropAttribute(attributeName);
            // this will go through the buffer manager which will reset the record size after
            //deleting a record.
        }
        return true;
    }

//    private static ArrayList<ArrayList<Object>> parseWhereClause(String stmt) {
//        String[] ddlDetails = stmt.strip().split(" ");
//        // TODO Implement where
//        return null;
//    }

    private static boolean parseCreateClause(String stmt) {
        String[] ddlDetails = stmt.split(" ");
        String parendParams = stmt.substring(stmt.strip().indexOf('(') + 1); // Grab string within parenthesis
        if (ddlDetails.length < 3) {
            System.err.println("Not enough values entered for table creation");
            return false;
        }
        String tableName = ddlDetails[2].split("\\(")[0]; // Grab the table name
        //checking if table name starts with alpha char
        char c = tableName.charAt(0);
        if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z')){
            System.err.println("The table name does not start with an alphabetic character.");
        }
        //checking if the table name is only alphanumeric
        if(!(tableName.matches("^[a-zA-Z]*$"))){
            System.err.println("The table name is not following the correct format.");
        }
        //checking if the table name is null - feel free to remove if this is redundant
        if(tableName==null){
            System.err.println("The table name is null.");
        }




        // New table attributes
        Attribute primaryKey = null;
        ForeignKey foreignKey = null;
        ArrayList<Attribute> tableAttributes = new ArrayList<>();

        // Grab details within parenthesis, ex: (attr1 Integer, attr2 double);
        parendParams = parendParams.substring(0, parendParams.length() - 2);

        // Split param string on comma and iterate
        for (String params : parendParams.split(",")) {
            // Todo implement notnull constraint for attributes
            params = params.strip(); // Clear whitespace chars
            if (params.startsWith("primarykey")) { // If line is for primary key
                if (primaryKey != null) { // If a primary key already exists, throw error
                    System.err.println("More than one primary key specified for table");
                    return false;
                }
                // Grab primary key between parends
                String pKey = params.substring(params.indexOf('(') + 1, params.indexOf(')')).strip();
                // Key should already be defined, find it and set primarykey attribute equal to attribute from attr list
                for (Attribute attr : tableAttributes) {
                    if (attr.attributeName().equals(pKey)) {
                        primaryKey = attr;
                        break;
                    }
                }
                // If we got here and no primary key is found, throw error
                if (primaryKey == null) {
                    System.err.println("Primary key not correctly defined in table creation");
                    return false;
                }
            } else if (params.startsWith("foreignkey")) {
                // Grab value inside first set of ()
                String fKey = params.substring(params.indexOf('(') + 1, params.indexOf(')')).strip();

                // Split the string on references and grab table name/primary column name
                String refParams = params.split("references")[1];
                String refKey = refParams.substring(refParams.indexOf('(') + 1, refParams.indexOf(')')).strip();
                String refTable = refParams.substring(0, refParams.indexOf('(')).strip();

                // Create new foreign key object
                foreignKey = new ForeignKey(refTable, refKey, fKey);
            } else {
                String[] columnParams = params.split(" ");
                if (columnParams.length >= 2) {
                    Attribute newAttr = new Attribute(columnParams[0].strip(), columnParams[1].strip());
                    tableAttributes.add(newAttr);
                    if (columnParams.length > 2) {
                        if (columnParams[2].equalsIgnoreCase("primarykey")) {
                            primaryKey = newAttr;
                        }
                    }
                } else {
                    System.err.println("Not all parameters specified for " + params);
                }
            }
        }
        if (primaryKey == null) {
            System.err.println("Primary key attribute not specified in table declaration");
            return false;
        }
        ITable newTable = catalog.addTable(tableName, tableAttributes, primaryKey);
        if (newTable == null) {
            System.err.println("Table already exists");
            return false;
        } else {
            System.out.println("Added table " + tableName + " successfully");
        }
        if (foreignKey != null) {
            catalog.getTable(tableName).addForeignKey(foreignKey);
        }
        return true;
    }

}
