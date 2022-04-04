import catalog.ACatalog;
import common.Attribute;
import parsers.DDLParser;
import parsers.DMLParser;
import parsers.ResultSet;
import storagemanager.AStorageManager;
import storagemanager.StorageManager;

import java.util.ArrayList;
import java.util.Scanner;

/*
    This is the main driver class for the database.

    It is responsible for starting and running the database.

    Other than in the provided testers this will be the only class to contain a main.

    You will add functionality to this class during the different phases.

    More details in the writeup.
 */
public class Database {

    private static StorageManager sm;
    private static ACatalog catalog;

    public static void main(String[] args) {
        catalog = ACatalog.createCatalog(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
        sm = (StorageManager) AStorageManager.createStorageManager();

        Scanner in = new Scanner(System.in);
        StringBuilder input = new StringBuilder(in.next());

        while(!input.toString().equalsIgnoreCase("quit;")){
            if(!input.toString().endsWith(";")) {
                if(input.toString().equalsIgnoreCase("quit;")){
                    break;
                }
                input.append(" ").append(in.next());
                continue;
            }
            String[] inputStrings = input.toString().split(";");
            for(String inputString : inputStrings) {
                inputString += ";";
                // Ugly but will be useful when we implement executeQuery
                if (inputString.toLowerCase().startsWith("create table")
                        || inputString.toLowerCase().startsWith("drop table")
                        || inputString.toLowerCase().startsWith("insert")
                        || inputString.toLowerCase().startsWith("update")
                        || inputString.toLowerCase().startsWith("delete")
                        || inputString.toLowerCase().startsWith("alter table")) {
                    if(executeStatement(inputString)){
                        System.out.println("SUCCESS");
                    }else{
                        System.err.println("ERROR: " + inputString);
                    }
                }else if (inputString.toLowerCase().startsWith("select")) {
                   printTable(executeQuery(inputString));
                }
            }
            input = new StringBuilder(in.next().toLowerCase());
        }
        if(terminateDatabase()){
            System.out.println("Saved and closed database successfully");
            in.close();
            System.exit(0);
        }else{
            System.err.println("Could not save and shutdown database");
            in.close();
            System.exit(-1);
        }
    }

    public static boolean executeStatement(String stmt){
        if(stmt.toLowerCase().startsWith("insert")
                || stmt.toLowerCase().startsWith("update")
                || stmt.toLowerCase().startsWith("delete")){
            return DMLParser.parseDMLStatement(stmt);
        }else{
            return DDLParser.parseDDLStatement(stmt);
        }
    }

    public static ResultSet executeQuery(String query){
        return DMLParser.parseDMLQuery(query);
    }

    public static boolean terminateDatabase(){
        sm.purgePageBuffer();
        return catalog.saveToDisk();
    }

    public static void printTable(ResultSet tableData){
        if(tableData == null){
            return;
        }
        ArrayList<Attribute> column_Names = tableData.attrs();
        ArrayList<ArrayList<Object>> data = tableData.results();
        StringBuilder formattingString = new StringBuilder();
        int lastRow = data.size()-1;
        for(int col = 0; col < column_Names.size(); col++) {
            // Another option would be to get a random index here or to actually loop through and find the largest
            // length item in the column (would take even more time)
            int length = (data.get(lastRow).get(col).toString().length() * 2);
            if (length < (column_Names.get(col).toString().length())) {
                length = column_Names.get(col).toString().length();
            }
            formattingString.append(("%-")).append(length).append("s ");
        }
        formattingString.append("%n");
        String format = formattingString.toString();
        System.out.format(format, column_Names.toArray(new Object[0]));
        StringBuilder result = new StringBuilder();
        for (ArrayList<Object> row: data) {
            result.append(String.format(format, row.toArray(new Object[0]))).append("\n");
        }
        System.out.println(result);
//        System.out.println(tableData.attrs());
//        for(ArrayList<Object> result:tableData.results()) {
//            System.out.println(result);
//        }
    }
}
