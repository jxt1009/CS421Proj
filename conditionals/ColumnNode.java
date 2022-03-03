package conditionals;

import catalog.ACatalog;
import common.Attribute;
import common.Table;
import storagemanager.StorageManager;

import java.util.ArrayList;

public class ColumnNode extends Node{

    private final Table table;
    private int columnIndex = -1;
    private Attribute columnAttribute;

    public ColumnNode(String columnName, Table table){
        this.table = table;
        for(int i = 0; i < table.getAttributes().size();i++){
            if(table.getAttributes().get(i).getAttributeName().equals(columnName)){
                columnIndex = i;
                columnAttribute = table.getAttributes().get(i);
            }
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
