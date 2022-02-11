package storagemanager;

import catalog.ACatalog;
import common.Attribute;
import common.ITable;
import common.Table;

import java.io.*;
import java.util.ArrayList;

public class Page {

    private final Table table;
    private ArrayList<ArrayList<Object>> records = new ArrayList<>();
    private int pageId;
    private int pageSize;

    public Page(Table table, int pageId){
        this.table = table;
        this.pageId = pageId;
        this.pageSize = ACatalog.getCatalog().getPageSize();
    }

    public Page(Table table, String pageFileLocation, int pageId) {
        this.table = table;
        this.pageId = pageId;
        File inputFile = new File(pageFileLocation);
        try {
            FileInputStream fin = new FileInputStream(inputFile);
            DataInputStream din = new DataInputStream(fin);
            this.pageId = din.readInt();
            int totalStoredRecords = din.readInt();
            for (int i = 0; i < totalStoredRecords; i++){
                ArrayList<Object> record = new ArrayList<>();
                for(Attribute attrib : table.getAttributes()){
                    switch (attrib.getAttributeType()) {
                        case "Integer":
                            record.add(din.readInt());
                            System.out.println("intValue");
                            break;

                        case "Double":
                            record.add(din.readDouble());
                            System.out.println("dblValue");

                            break;

                        case "Boolean":
                            record.add(din.readBoolean());
                            System.out.println("boolValue");

                            break;

                        case "Varchar":
                            String strValue = "";
                            // TODO READ IN STRING VALUES
                            break;
                        case "Char":
                            record.add(din.readChar());
                            System.out.println("charValue");

                            break;
                        default:
                    }
                }
                records.add(record);
                System.out.println(records.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Page(Table table, int pageId, ArrayList<ArrayList<Object>> records) {
        this.table = table;
        this.pageId = pageId;
        this.records = records;
    }

    public Integer getPageId(){
        return pageId;
    }

    public ArrayList<ArrayList<Object>> getRecords(){
        return records;
    }

    public boolean addRecord(ITable table, ArrayList<Object> record){
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for(ArrayList<Object> recordList : records){
            if(recordList.get(primaryKeyIndex).equals(record.get(primaryKeyIndex))){
                return false;
            }
        }
        records.add(record);
        return true;
    }

    public boolean deleteRecord(ITable table, Object pkValue){
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for(ArrayList<Object> recordList : records){
            if(recordList.get(primaryKeyIndex).equals(pkValue)){
                records.remove(recordList);
                return true;
            }
        }
        return false;
    }

    public boolean hasSpace(){
        return records.size() < pageSize;
    }

    public boolean updateRecord(ITable table, Object primaryKey, ArrayList<Object> newRecord) {
        if(deleteRecord(table,primaryKey)) {
            return addRecord(table, newRecord);
        }
        return false;
    }

}