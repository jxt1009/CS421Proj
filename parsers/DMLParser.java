package parsers;

import catalog.ACatalog;
import common.Table;
import conditionals.*;
import storagemanager.AStorageManager;
import storagemanager.RecordHelper;

import java.util.*;

/*
  Class for DML parser

  This class is responsible for parsing DDL statements

  You will implement the parseDMLStatement and parseDMLQuery functions.
  You can add helper functions as needed, but the must be private and static.

  @author Scott C Johnson (sxjcs@rit.edu)

 */
public class DMLParser {

    private static final ACatalog catalog = ACatalog.getCatalog();
    private static final AStorageManager sm = AStorageManager.getStorageManager();

    // Correlate operators to a specific level of evaluation
    private enum Operator {
        COMPARISON(1), AND(2), OR(3);
        final int precedence;

        Operator(int p) {
            precedence = p;
        }
    }

    // Define each parameter with a level of precendence for evaluation
    private static final Map<String, Operator> operators = new HashMap<>() {{
        put(">=", Operator.COMPARISON);
        put("<=", Operator.COMPARISON);
        put("<", Operator.COMPARISON);
        put(">", Operator.COMPARISON);
        put("=", Operator.COMPARISON);
        put("!=", Operator.COMPARISON);
        put("AND", Operator.AND);
        put("OR", Operator.OR);
    }};

    private static boolean isHigherPrecedence(String op, String sub) {
        return (operators.containsKey(sub.toUpperCase())
                && operators.get(sub.toUpperCase()).precedence <= operators.get(op.toUpperCase()).precedence);
    }

    /**
     * This function will parse and execute DML statements (insert, delete, update, etc)
     * <p>
     * This will be used for parsing DML statement that do not return data
     *
     * @param stmt the statement to parse/execute
     * @return true if successfully parsed/executed; false otherwise
     */
    public static boolean parseDMLStatement(String stmt) {
        if (stmt.endsWith(";")) {
            stmt = stmt.replace(";", "");
        } else {
            //System.out.println(stmt);
            System.err.println("Statement does not end with a semicolon");
            return false;
        }
        if (stmt.toLowerCase().startsWith("insert")) {
            if(!stmt.toLowerCase().startsWith("insert into")){
                System.err.println("Error with insert statement, use 'insert into'");
                return false;
            }
            // Table name should be before the parend
            String tableName = stmt.split("\\(")[0].split(" ")[2].strip();

            Table table = (Table) catalog.getTable(tableName);

            // Values are within the parends, grab string inside
            int insertedRecords = 0;
            //System.out.println(stmt);
            String[] records = stmt.split("values")[1].split("\\)");
            //check if records[i] = getcolumnthing[i] (from table class) and send an error message
            // if they are of different types
            for(String recordString: records) {
                String[] insertValues = recordString.split("\\(")[1].strip().split(",");
                for(int i = 0; i < insertValues.length;i++){
                    insertValues[i] = insertValues[i].strip();
                }

                // Convert string list into arraylist of records to add
                ArrayList<Object> record = new ArrayList<>(Arrays.asList(insertValues));
                // Insert records
                if(sm.insertRecord(table, record)){
                    insertedRecords += 1;
                }
            }

            return insertedRecords == records.length;
        } else if (stmt.toLowerCase().startsWith("delete from")) {
            String tableName = stmt.split("delete from")[1].split("where")[0].strip();
            Table table = (Table) catalog.getTable(tableName);

            String where = stmt.strip().split("where")[1].strip();
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(table, where);
            boolean success = true;
            System.out.println(parseWhere);
            for (ArrayList<Object> deleteRow : parseWhere) {
                success = success && sm.deleteRecord(table, deleteRow.get(table.getPrimaryKeyIndex()));
            }
            System.out.println(success);
            return success;
        } else if (stmt.toLowerCase().startsWith("update")) {
            // Table name is in between 'update' and 'set' tokens
            String tableName = stmt.split("update")[1].split("set")[0].strip();

            // Set params are in between 'set' and 'where' tokens
            String setParams = stmt.split("set")[1].split("where")[0].strip();

            // Column name is before the equals token
            String columnName = setParams.split("=")[0].strip();
            // New value is after equals token
            String newValue = setParams.split("=")[1].split("where")[0].strip();

            // Grab table for nodes to use
            Table table = (Table) catalog.getTable(tableName);
            if(table == null){
                return false;
            }
            if(newValue.equals("null")){
                return !table.isANonNullableAttribute(table.getColumnIndex(columnName));
            }
            if(table.getColumnIndex(columnName) == -1){
                return false;
            }
            // Grab everything after 'where' token
            String where = stmt.strip().split("where")[1].strip();

            // Parse node tree and get returned list of values to update
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(table, where);
            if(parseWhere == null){
                System.err.println("Where clause could not be parsed");
                return false;
            }
            boolean success = true;
            // Iterate through rows to update and apply new value
            for (ArrayList<Object> updateRow : parseWhere) {
                // Create copy of row to work on
                ArrayList<Object> newRow = (ArrayList<Object>) updateRow.clone();
                //switch statements for the operators
                if(setParams.contains("+")||setParams.contains("-")||setParams.contains("/")||setParams.contains("*")){
                    System.out.println(Arrays.toString(setParams.split(" ")));
                    String operation = setParams.split(" ")[3].strip();
                    String value = setParams.split(" ")[4].strip();
                    String setColumnName = setParams.split(" ")[2].strip();
                    Object operator;
                    boolean isNumber = RecordHelper.isNumeric(value) || RecordHelper.isNumeric(columnName);
                    if(isNumber || table.containsColumn(value) || table.containsColumn(columnName)){
                        float originalValue = 0;
                        if(table.containsColumn(value)) {
                            originalValue = Float.parseFloat((String) updateRow.get(table.getColumnIndex(value)));
                            operator = Float.parseFloat(setColumnName);
                            operation = setParams.split(" ")[3];
                        }else if(table.containsColumn(setColumnName)) {
                            originalValue = Float.parseFloat((String) updateRow.get(table.getColumnIndex(setColumnName)));
                            operator = Float.parseFloat(value);
                            operation = setParams.split(" ")[3];
                        }else if(RecordHelper.isNumeric(value) && RecordHelper.isNumeric(setColumnName)){
                            originalValue = Float.parseFloat(value);
                            operator = Float.parseFloat(setColumnName);
                        }else{
                            operator = Float.parseFloat(value);
                        }
                        switch (operation) {
                            case "+" -> newRow.set(table.getColumnIndex(columnName), String.valueOf(originalValue + (float)operator));
                            case "-" -> newRow.set(table.getColumnIndex(columnName), String.valueOf(originalValue - (float)operator));
                            case "*" -> newRow.set(table.getColumnIndex(columnName), String.valueOf(originalValue * (float)operator));
                            case "/" -> newRow.set(table.getColumnIndex(columnName), String.valueOf(originalValue / (float)operator));
                        }
                    }else{
                        newRow.set(table.getColumnIndex(columnName), newValue.replace("\"",""));
                    }
                }else {
                    newRow.set(table.getColumnIndex(columnName), newValue.replace("\"",""));
                }
                success = success && sm.updateRecord(table, updateRow, newRow);

                // Update record in table
            }
            return success;
        }
        System.err.println("Statement not formatted correctly: " + stmt);
        return false;
    }


    private static ArrayList<ArrayList<Object>> parseWhereClause(Table table, String stmt) {
        // This function is a bit convoluted, but it works
        // Convert statement into a postfix string by order of precedence
        // Then convert back to a stack and pass to parseNode
        String[] params = stmt.strip().split(" ");
        StringBuilder output = new StringBuilder();
        Stack<String> stack  = new Stack<>();

        for (String token : params) {
            // If this is an operator token
            if (operators.containsKey(token.toUpperCase())) {
                // If tokens on stack and top of stack is higher precedence (AND/OR), add value/column tokens in front
                while (!stack.isEmpty() && isHigherPrecedence(token, stack.peek())) {
                    output.append(stack.pop()).append(" ");
                }
                // Push operator after value/column tokens
                stack.push(token);
            } else {
                // If a value or column, add to string
                output.append(token).append(" ");
            }
        }
        // If any operators or tokens left on stack, append in order
        while ( ! stack.isEmpty()) {
            output.append(stack.pop()).append(' ');
        }
        // Split output back to a list, not ideal but hey
        String[] tokenString = output.toString().split(" ");
        // Create stack for tokens, way better than working with lists
        Stack<String> tokenStack = new Stack<>();
        for(String str: tokenString){
            tokenStack.push(str);
        }
        // Parse node structure
        Node tree = parseNode(table,tokenStack);
        if(tree == null){
            return null;
        }
        // Return final arraylist of results
        ArrayList<ArrayList<Object>> results = tree.evaluate();

        Set<ArrayList<Object>> set = new HashSet<>(results);
        results.clear();
        results.addAll(set);
        return results;
    }

    private static Node parseNode(Table table, Stack<String> params) {
        if(params.peek().equalsIgnoreCase("or") || params.peek().equalsIgnoreCase("and")){
            String conditional = params.pop();
            // Man... recursion is awesome, this saves so much work using a stack
            return new ConditionalNode(
                    parseNode(table, params), // Left node
                    parseNode(table,params),  // Right node
                    conditional,              // Conditional
                    table);
        }
        return parseSingleNode(table,params);
    }

    private static Node parseSingleNode(Table table, Stack<String> params){
        // Pop values off stack in order
        String operator = params.pop();
        String rightString = params.pop();
        String leftString = params.pop();

        // Left node is always a column
        ColumnNode left = new ColumnNode(leftString, table);
        if(left.getColumnIndex() == -1){
            System.err.println("Column name does not exist in table");
            return null;
        }
        // Right node could be column or value, don't set yet
        Node right;
        if (table.containsColumn(rightString.strip())) {
            // If the string is name of column, create column node
            right = new ColumnNode(rightString, table);
        } else {
            // If not a column, assume it is a value
            right = new ValueNode(rightString);
        }
        // Create operator node w/ values
        return new OperatorNode(left, right, operator);
    }

    /**
     * This function will parse and execute DML statements (select)
     * <p>
     * This will be used for parsing DML statement that return data
     *
     * @param query the query to parse/execute
     * @return the data resulting from the query; null upon error.
     * Note: No data and error are two different cases.
     */
    public static ResultSet parseDMLQuery(String query) {
        System.out.println(query);
        if(query.endsWith(";")){
            query = query.replace(";","");
        }else{
            System.err.println("query does end with ;");
        }
        if(query.contains("from")){
            String fromString = query.split("from")[1].strip();
            ArrayList<String> tableNames = new ArrayList<>();
            if(fromString.contains("where")){
                String tableNameList = fromString.split("where")[0];
                if(tableNameList.contains(",")) {
                    tableNames.addAll(Arrays.asList(tableNameList.split(",")));
                }else{
                    tableNames.add(tableNameList);
                }

            }else{
                if(fromString.contains(",")) {
                    tableNames.addAll(Arrays.asList(fromString.split(",")));
                }else{
                    tableNames.add(fromString);
                }
            }
            for(String tableName : tableNames){
                if(catalog.containsTable(tableName)){
                    Table temp = (Table) catalog.getTable(tableName);
                    return new ResultSet(temp.getAttributes(),sm.getRecords(temp));
                }else{
                    System.err.println("DB does not contain table: " + tableName);
                }
            }
        }
        return null;
    }

    public static ResultSet parseSelectClause(String query){
        //select * from foo;
        if (query.contains("*")) {
            return parseFromClause(query);
        }

        //select name, gpa from student
        //select name, dept_name from student, department where student.dept_id = department.dept_id;
        ArrayList<String> tableNames = new ArrayList<>();
        String fromString = query.split("from")[1].strip();
        //System.out.println(fromString);
        if(fromString.contains(",")){
            tableNames.addAll(Arrays.asList(fromString.split(",")));
        }



        return null;
    }

    public static ResultSet parseFromClause(String query){
        return null;
    }
}
