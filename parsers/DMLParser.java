package parsers;

import catalog.ACatalog;
import common.Table;
import conditionals.*;
import storagemanager.AStorageManager;

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
            System.out.println(stmt);
            System.err.println("Statement does not end with a semicolon");
            return false;
        }
        if (stmt.toLowerCase().startsWith("insert into")) {
            // Table name should be before the parend
            String tableName = stmt.split("\\(")[0].split(" ")[2].strip();

            // Values are within the parends, grab string inside
            int insertedRecords = 0;
            System.out.println(stmt);
            String[] records = stmt.split("values")[1].split("\\)");
            //check if records[i] = getcolumnthing[i] (from table class) and send an error message
            // if they are of different types
            for(String recordString: records) {
                String[] insertValues = recordString.split("\\(")[1].strip().split(",");

                // Convert string list into arraylist of records to add
                ArrayList<Object> record = new ArrayList<>(Arrays.asList(insertValues));
                Table table = (Table) catalog.getTable(tableName);
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

            for (ArrayList<Object> deleteRow : parseWhere) {
                sm.deleteRecord(table, deleteRow.get(table.getPrimaryKeyIndex()));
            }

        } else if (stmt.toLowerCase().startsWith("update")) {
            // Table name is in between 'update' and 'set' tokens
            String tableName = stmt.split("update")[1].split("set")[0].strip();

            // Set params are in between 'set' and 'where' tokens
            String setParams = stmt.split("set")[1].split("where")[0].strip();

            // Column name is before the equals token
            String columnName = setParams.split("=")[0];
            // New value is after equals token
            String newValue = setParams.split("=")[1];

            // Grab table for nodes to use
            Table table = (Table) catalog.getTable(tableName);

            // Grab everything after 'where' token
            String where = stmt.strip().split("where")[1].strip();

            // Parse node tree and get returned list of values to update
            ArrayList<ArrayList<Object>> parseWhere = parseWhereClause(table, where);

            // Iterate through rows to update and apply new value
            for (ArrayList<Object> updateRow : parseWhere) {
                // Create copy of row to work on
                ArrayList<Object> newRow = (ArrayList<Object>) updateRow.clone();

                // TODO newValue needs to be parsed if it contains "+,-,/,*"
                //switch statements for the operators
                //int newValue = table.getColumnIndex(columnName);
                if(newValue.contains("+")||newValue.contains("-")||newValue.contains("/")||newValue.contains("*")){
                    String operation = newValue.split(" ")[2];
                    float operator = Float.parseFloat(newValue.split(" ")[3]);
                    switch (operation){
                        case "+": newValue = String.valueOf(table.getColumnIndex(columnName) + operator);
                            break;
                        case "-": newValue = String.valueOf(table.getColumnIndex(columnName) - operator);
                            break;
                        case "*": newValue = String.valueOf(table.getColumnIndex(columnName) * operator);
                            break;
                        case "/": newValue = String.valueOf(table.getColumnIndex(columnName) / operator);
                            break;
                    }
                }
                newRow.set(table.getColumnIndex(columnName), newValue);

                // Update record in table
                sm.updateRecord(table, updateRow, newRow);
            }
            return true;
        }


        return true;
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
        // Return final arraylist of results
        return tree.evaluate();
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
        Node left = new ColumnNode(leftString, table);
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
    //NOT FOR THIS PHASE - EVERYTHING IN THE SECOND PHASE HAS TO BE IMPLEMENTED
    //IN THE PARSEDMLSTATEMENT FUNCTION
    public static ResultSet parseDMLQuery(String query) {
        return null;
    }
}
