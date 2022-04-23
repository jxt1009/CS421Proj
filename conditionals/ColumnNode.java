package conditionals;

import catalog.ACatalog;
import common.Table;
import storagemanager.RecordHelper;
import storagemanager.StorageManager;

import java.util.ArrayList;
import java.util.HashMap;

public class ColumnNode extends Node{

    private Table table;
    private int columnIndex = -1;
    private String columnName;

    public ColumnNode(String columnName, Table table){
        columnName = RecordHelper.checkTableColumns(table.getAttributes(),columnName);
        if(columnName.contains(".")){
            String[] columnInfo = columnName.split("\\.");
            columnName = columnInfo[1];
            table = (Table) ACatalog.getCatalog().getTable(columnInfo[0]);
        }
        this.table = table;
        this.columnName = columnName;
        columnIndex = table.getColumnIndex(this.columnName);
    }


    public int getColumnIndex(){
        return columnIndex;
    }

    public String getColumnName(){
        return columnName;
    }

    @Override
    public ArrayList<ArrayList<Object>> evaluate() {
        return StorageManager.getStorageManager().getRecords(table);
    }

    public String toString(){
        return "Column " + columnName;
    }

    public Table getTable() {
        return table;
    }
}
