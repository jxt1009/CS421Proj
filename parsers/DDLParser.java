package parsers;

import catalog.ACatalog;
import common.Attribute;
import common.ForeignKey;
import common.ITable;
import storagemanager.AStorageManager;

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
    private static AStorageManager sm;

    public static boolean dropInstruction(String tableName) {
        if(!catalog.containsTable(tableName)){
            System.err.println("Table " + tableName + " does not exist in catalog");
            return false;
        }
        boolean didDrop = catalog.dropTable(tableName);
        if(didDrop){
            System.out.println("Table "+tableName + " dropped successfully");
        }else{
            System.err.println("Table "+tableName+" could not be dropped");
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
        String[] ddlDetails = stmt.split(" ");
        String instruction = ddlDetails[0];
        String parendParams = stmt.substring(stmt.strip().indexOf('(') + 1); // Grab string within parenthesis
        if (instruction.toLowerCase().startsWith("create")) { // Create statement
            if(ddlDetails.length < 3){
                System.err.println("Not enough values entered for table creation");
                return false;
            }
            String tableName = ddlDetails[2].split("\\(")[0]; // Grab the table name
            //TODO check table name starts with alpha char and is only alphanumeric

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
                    String pKey = params.substring(params.indexOf('(') + 1, params.indexOf(')'));
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
                    String[] fKeyParams = params.split(" ");

                    String fKey = fKeyParams[0].substring(fKeyParams[0].indexOf('(') + 1, fKeyParams[0].indexOf(')'));
                    String refTable = fKeyParams[2].substring(0, fKeyParams[2].indexOf('('));
                    String refKey = fKeyParams[2].substring(fKeyParams[2].indexOf('(') + 1, fKeyParams[2].indexOf(')'));

                    foreignKey = new ForeignKey(refTable, refKey, fKey);
                } else {
                    String[] columnParams = params.split(" ");
                    if (columnParams.length >= 2) {
                        Attribute newAttr = new Attribute(columnParams[0], columnParams[1]);
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
            System.out.println(tableAttributes);
            if (primaryKey == null) {
                System.err.println("Primary key attribute not specified in table declaration");
                return false;
            }
            ITable newTable = catalog.addTable(tableName, tableAttributes, primaryKey);
            if (newTable == null) {
                System.err.println("Table already exists");
                return false;
            }else{
                System.out.println("added table " +tableName+ " successfully");
            }
            if (foreignKey != null) {
                catalog.getTable(tableName).addForeignKey(foreignKey);
            }
            //create <smth>
        } else if (stmt.toLowerCase().startsWith("drop table")) {
            return dropInstruction(stmt.split(" ")[2].replace(";","").strip());
        }

        return true;
    }
}
