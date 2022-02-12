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
        table.addPage(pageId);
    }

    public Page(Table table, String pageFileLocation, int pageId) {
        this.table = table;
        this.pageId = pageId;
        table.addPage(pageId);
        File inputFile = new File(pageFileLocation);
        try {
            FileInputStream fin = new FileInputStream(inputFile);
            DataInputStream din = new DataInputStream(fin);
            this.pageId = din.readInt();
            int totalStoredRecords = din.readInt();
            for (int i = 0; i < totalStoredRecords; i++){
                ArrayList<Object> record = new ArrayList<>();
                for(Attribute attrib : table.getAttributes()){
                    String type = attrib.getAttributeType();
                    if (type.equals("Integer")) {
                        record.add(din.readInt());
                    }else if (type.equals("Double")) {
                        record.add(din.readDouble());
                    }else if (type.equals("Boolean")) {
                        record.add(din.readBoolean());
                    }else if (type.startsWith("Varchar")) {
                        int charLen = Integer.parseInt(type.substring(type.indexOf("(")+1,type.indexOf(")")));
                        String inputChar = "";
                        for(int readIndex = 0; readIndex < charLen;readIndex++) {
                            char c = din.readChar();
                            if(c != '\t') inputChar += c;
                        }
                        record.add(inputChar);
                    }else if (type.startsWith("Char")) {
                        int charLen = Integer.parseInt(type.substring(type.indexOf("(")+1,type.indexOf(")")));
                        String inputChar = "";
                        for(int readIndex = 0; readIndex < charLen;readIndex++) {
                            char c = din.readChar();
                            if(c != '\t') inputChar += c;
                        }
                        record.add(inputChar);
                    }else{
                    }
                }
                records.add(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Page(Table table, int pageId, ArrayList<ArrayList<Object>> records) {
        this.table = table;
        this.pageId = pageId;
        this.records = records;
        table.addPage(pageId);
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

    public Table getTable() {
        return table;
    }
}
