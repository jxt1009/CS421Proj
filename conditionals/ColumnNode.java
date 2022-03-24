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

    public String checkTableColumns(Table table, String columnName){
        List<Attribute> attributes = table.getAttributes();
        String selectedColumn = "";
        if(!columnName.contains(".")) {
            for (Attribute attr : attributes) {
                String column = attr.getAttributeName();
                if (column.contains(".")) {
                    column = column.split("\\.")[1];
                }
                if (column.isEmpty()) {
                    System.err.println("Cannot process table name in where clause");
                    return "";
                }
                if (columnName.equalsIgnoreCase(column)) {
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
