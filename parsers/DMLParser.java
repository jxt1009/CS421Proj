package parsers;

import catalog.ACatalog;
import common.Table;
import conditionals.*;
import storagemanager.AStorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(table,where);

            for(ArrayList<Object> deleteRow : parseWhere){
                sm.deleteRecord(table,deleteRow.get(table.getPrimaryKeyIndex()));
            }

        }
        else if(stmt.toLowerCase().startsWith("update")){
            String tableName = stmt.split("update")[1].split("set")[0].strip();

            String setParams = stmt.split("set")[1].split("where")[0].strip();

            String columnName = setParams.split("=")[0];
            String newValue = setParams.split("=")[1];

            Table table = (Table) catalog.getTable(tableName);

            String where = stmt.strip().split("where")[1].strip();
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(table,where);

            for(ArrayList<Object> updateRow : parseWhere){
                ArrayList<Object> newRow = (ArrayList<Object>) updateRow.clone();
                newRow.set(table.getColumnIndex(columnName),newValue);
                sm.updateRecord(table,updateRow,newRow);
            }
            return true;
        }


        return true;
    }


    private static ArrayList<ArrayList<Object>> parseWhereClause(Table table, String stmt) {
        List<String> params = Arrays.asList(stmt.strip().split(" "));
        Node tree = parseNode(table,params);
        return tree.evaluate();
    }

    private static Node parseNode(Table table, List<String> params){
        // Left node is always a column
        Node left = new ColumnNode(params.get(0),table);
        // Right node could be column or value, don't set yet
        Node right;
        if(params.get(2).contains("\"") && table.containsColumn(params.get(2).replace("\"",""))){
            // If the string is name of column, create column node
            right = new ColumnNode(params.get(2),table);
        }else {
            // If not a column, assume it is a value
            right = new ValueNode(params.get(2));
        }
        // If there are more params, check if it's an and/or statement
        if(params.size() > 3) {
            if (params.get(3).equals("and") || params.get(3).equals("or")) {
                // Create new conditionalnode and recursively generate more nodes
               return new ConditionalNode(
                       new OperatorNode(left,right,params.get(1)),
                       parseNode(table, params.subList(4, params.size())),
                       params.get(3),
                       table
               );
            }
        }
        return new OperatorNode(left,right,params.get(1));
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
