import catalog.ACatalog;
import parsers.DDLParser;
import parsers.ResultSet;

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

    public static void main(String[] args) {
        ACatalog.createCatalog(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
        Scanner in = new Scanner(System.in);
        String input = in.next();
        while(!input.equalsIgnoreCase("quit")){
            if(!input.endsWith(";")) {
                input += " " + in.next();
                continue;
            }
            if(input.toLowerCase().startsWith("create table")
                    || input.toLowerCase().startsWith("drop table")
                    || input.toLowerCase().startsWith("alter table")){
                executeStatement(input);
            }
            input = in.next().toLowerCase();
        }
        if(terminateDatabase()){
            System.out.println("Saved and closed database successfully");
        }else{
            System.err.println("Could not save and shutdown database");
        }
    }

    public static boolean executeStatement(String stmt){
        stmt = stmt.strip();
        return DDLParser.parseDDLStatement(stmt);
    }

    public static ResultSet executeQuery(String query){

        //NOT FOR THIS PHASE
        return null;
    }

    public static boolean terminateDatabase(){
        return false;
    }
}
