package parsers;

import catalog.ACatalog;
import catalog.Catalog;
import common.Table;
import storagemanager.AStorageManager;

/*
  Class for DDL parser

  This class is responsible for parsing DDL statements

  You will implement the parseDDLStatement function.
  You can add helper functions as needed, but the must be private and static.

  @author Scott C Johnson (sxjcs@rit.edu)

 */
public class DDLParser {

    private static ACatalog catalog;
    private static AStorageManager sm;

    public static boolean dropInstruction(String tableName){
        catalog.dropTable(tableName);
        return true;
    }

    /**
     * This function will parse and execute DDL statements (create table, create index, etc)
     * @param stmt the statement to parse
     * @return true if successfully parsed/executed; false otherwise
     */
    public static boolean parseDDLStatement(String stmt){
        String instruction = stmt.split(" ")[0];
        if(instruction.equals("create")){
            //create <smth>
        }
        else if (instruction.equals("drop")){
            dropInstruction(stmt.split(" ")[2]);
        }

        return true;
    }
}
