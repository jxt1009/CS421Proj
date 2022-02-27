package parsers;

import catalog.ACatalog;
import common.Table;
import storagemanager.AStorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/*
  Class for DML parser

  This class is responsible for parsing DDL statements

  You will implement the parseDMLStatement and parseDMLQuery functions.
  You can add helper functions as needed, but the must be private and static.

  @author Scott C Johnson (sxjcs@rit.edu)

 */
public class DMLParser {

    private static ACatalog catalog = ACatalog.getCatalog();
    private static AStorageManager sm = AStorageManager.getStorageManager();

    /**
     * This function will parse and execute DML statements (insert, delete, update, etc)
     *
     * This will be used for parsing DML statement that do not return data
     *
     * @param stmt the statement to parse/execute
     * @return true if successfully parsed/executed; false otherwise
     */
    public static boolean parseDMLStatement(String stmt){
        if(stmt.endsWith(";")){
            stmt = stmt.replace(";","");
        }else{
            System.err.println("Statement does not end with a semicolon");
            return false;
        }
        if(stmt.toLowerCase().startsWith("insert into")){
            //inserting tuple into table
            String tableName = stmt.split("\\(")[0].split(" ")[2];
            String[] insertValues = stmt.split("\\(")[1].split("\\)")[0].strip().split(",");
            ArrayList<Object> record = new ArrayList<>(Arrays.asList(insertValues));
            Table table = (Table) catalog.getTable(tableName);
            return sm.insertRecord(table,record);
        }

        else if(stmt.toLowerCase().startsWith("delete from")){
            String tableName = stmt.split("delete from")[1].split("where")[0].strip();
            Table table = (Table) catalog.getTable(tableName);

            String where = stmt.strip().split("where")[1].strip();
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(where);

        }
        else if(stmt.toLowerCase().startsWith("update")){
            String tableName = stmt.split("update")[1].split("set")[0].strip();
            Table table = (Table) catalog.getTable(tableName);

            String where = stmt.strip().split("where")[1].strip();
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(where);
            // where will likely return a list of tuples to work with? process after return
            //System.out.println(tableName);
            return true;
        }


        return true;
    }


    private static ArrayList<ArrayList<Object>> parseWhereClause(String stmt) {
        String[] params = stmt.strip().split(" ");
        //System.out.println(Arrays.toString(params));
        // TODO Implement where
        return null;
    }

    /**
     * This function will parse and execute DML statements (select)
     *
     * This will be used for parsing DML statement that return data
     * @param query the query to parse/execute
     * @return the data resulting from the query; null upon error.
     *         Note: No data and error are two different cases.
     */
    //NOT FOR THIS PHASE - EVERYTHING IN THE SECOND PHASE HAS TO BE IMPLEMENTED
    //IN THE PARSEDMLSTATEMENT FUNCTION
    public static ResultSet parseDMLQuery(String query){
        return null;
    }
}
