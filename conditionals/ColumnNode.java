package conditionals;

import catalog.ACatalog;
import common.Attribute;
import common.Table;
import storagemanager.StorageManager;

import java.util.ArrayList;
import java.util.List;

public class ColumnNode extends Node{

    private final Table table;
    private int columnIndex = -1;
    private Attribute columnAttribute;

    public ColumnNode(String columnName, Table table){
        this.table = table;
        columnName = checkTableColumns(table,columnName);
        if(table.containsColumn(columnName)) {
            for (int i = 0; i < table.getAttributes().size(); i++) {
                if (table.getAttributes().get(i).getAttributeName().equals(columnName)) {
                    columnIndex = i;
                    columnAttribute = table.getAttributes().get(i);
                }
            }
        }
    }

    /**
     * Helper function to take care of <tablename>.<column_name> syntax
     * @param table Table with attribute names
     * @param columnName Column name, could be <column_name> or <tablename>.<column_name>
     * @return
     */
    public String checkTableColumns(Table table, String columnName){
        // Get list of attributes from table
        List<Attribute> attributes = table.getAttributes();
        // assume we haven't found a matching column yet
        String selectedColumn = "";
        // Only check if user does not use tablename.columnname syntax
        if(!columnName.contains(".")) {

            for (Attribute attr : attributes) {
                String column = attr.getAttributeName();
                if (column.contains(".")) {
                    // Remove table name to find if two attributes match
                    // ex: querying 'baa' with foo.baa and bazzle.baa
                    // baa is ambiguous and should throw an error
                    column = column.split("\\.")[1];
                }

                if (column.isEmpty()) {
                    // if a user entered 'foo.'
                    System.err.println("Cannot process table name in where clause");
                    return "";
                }
                // If a match with the column name
                if (columnName.equalsIgnoreCase(column)) {
                    // If we already found a match, throw an error, query  column name is ambiguous
                    if (!selectedColumn.isEmpty()) {
                        System.out.println("Attempting to use column name without specifying table name: " + column);
                        return "";
                    } else {
                        selectedColumn = attr.getAttributeName();
                    }
                }
            }
            return selectedColumn;
        }else{
            return columnName;
        }
    }

    public int getColumnIndex(){
        return columnIndex;
    }
    public Attribute getColumnAttribute(){
        return columnAttribute;
    }

    @Override
    public ArrayList<ArrayList<Object>> evaluate() {
        return StorageManager.getStorageManager().getRecords(table);
    }

    public String toString(){
        return "Column " + columnAttribute.getAttributeName();
    }
}
