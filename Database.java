import catalog.ACatalog;
import parsers.DDLParser;
import parsers.DMLParser;
import parsers.ResultSet;
import storagemanager.AStorageManager;
import storagemanager.StorageManager;

import java.util.ArrayList;
import java.util.Locale;
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
        String input = in.next();

        while(!input.equalsIgnoreCase("quit")){
            if(!input.endsWith(";")) {
                if(input.equalsIgnoreCase("quit")){
                    break;
                }
                input += " " + in.next();
                continue;
            }
            String[] inputStrings = input.split(";");
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
                    }
                }else if (inputString.toLowerCase().startsWith("select")) {
                   printTable(executeQuery(inputString));
                }
            }
            input = in.next().toLowerCase();
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
        System.out.println(tableData.attrs());
        for(ArrayList<Object> result:tableData.results()) {
            System.out.println(result);
        }
    }
}
