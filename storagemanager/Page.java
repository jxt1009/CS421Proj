package storagemanager;

import catalog.ACatalog;
import common.Attribute;
import common.ITable;
import common.Table;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

public class Page {

    private final Table table;
    private ArrayList<ArrayList<Object>> records = new ArrayList<>();
    private final int pageId;
    private final int pageSize;

    public Page(Table table, int pageId){
        this.table = table;
        this.pageId = pageId;
        this.pageSize = ACatalog.getCatalog().getPageSize();
    }

    public Page(Table table, String pageFileLocation, int pageId) {
        this.table = table;
        this.pageSize = ACatalog.getCatalog().getPageSize();
        File inputFile = new File(pageFileLocation);
        try {
            FileInputStream fin = new FileInputStream(inputFile);
            DataInputStream din = new DataInputStream(fin);
            pageId = din.readInt();
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
                        record.add(FileManager.readChars(din));
                    }else if (type.startsWith("Char")) {
                        int charLen = din.readInt();
                        String outputString = "";
                        for (int readIndex = 0; readIndex < charLen; readIndex++) {
                            char c = din.readChar();
                            if (c!='\t') {
                               outputString += c;
                            }
                        }
                        record.add(outputString);
                    }
                }
                records.add(record);
            }
            din.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pageId = pageId;
    }

    public Page(Table table, int pageId, ArrayList<ArrayList<Object>> records) {
        this.table = table;
        this.pageId = pageId;
        this.records = records;
        this.pageSize = ACatalog.getCatalog().getPageSize();
    }

    public Integer getPageId(){
        return pageId;
    }

    public Object getSmallestPrimaryKey(){
        return this.records.get(0).get(table.getAttributes().indexOf(table.getPrimaryKey()));
    }

    public ArrayList<ArrayList<Object>> getRecords(){
        return records;
    }

    public boolean addRecord(ITable table, ArrayList<Object> record,int index){
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for(ArrayList<Object> recordList : records){
            if(recordList.get(primaryKeyIndex).equals(record.get(primaryKeyIndex))){
                return false;
            }
        }
        records.add(index,record);
        return true;
    }

    public int deleteRecord(ITable table, Object pkValue){
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for(ArrayList<Object> recordList : records){
            if(recordList.get(primaryKeyIndex).equals(pkValue)){
                int index = records.indexOf(recordList);
                records.remove(recordList);
                return index;
            }
        }
        return -1;
    }

    public boolean hasSpace(ArrayList<Object> newRecord) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(records);
            out.writeObject(newRecord);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numBytes = baos.toByteArray().length;
        return numBytes < pageSize;
    }

    public boolean updateRecord(ITable table, Object primaryKey, ArrayList<Object> newRecord) {
        int recordIndex = deleteRecord(table,primaryKey);
        if(recordIndex != -1) {
            return addRecord(table, newRecord,recordIndex);
        }
        return false;
    }

    public Table getTable() {
        return table;
    }

    public String toString(){
        return "Page ID: "+pageId +"|";
    }

}