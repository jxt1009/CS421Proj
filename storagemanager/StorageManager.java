package storagemanager;

import common.ITable;
import common.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StorageManager extends AStorageManager{


    // Page buffer will be an arraylist of hashmaps containing records by table name
    // To get a specific page, we need to iterate through all pages and find if the value is present
    // Or in the case of inserting a record, find a page where it makes sense to store
    BufferManager pageBuffer = new BufferManager();

    @Override
    public boolean clearTableData(ITable table) {
        // iterate through attribs of table and call dropAttribute one by one
        return false;
    }

    @Override
    public ArrayList<Object> getRecord(ITable table, Object pkValue) {
        return pageBuffer.getRecord(table,pkValue);
    }

    @Override
    public ArrayList<ArrayList<Object>> getRecords(ITable table) {
        return pageBuffer.getAllRecords(table);
    }

    @Override
    public boolean insertRecord(ITable table, ArrayList<Object> record) {
        return pageBuffer.insertRecord(table,record);
    }

    @Override
    public boolean deleteRecord(ITable table, Object primaryKey) {
        return pageBuffer.deleteRecord(table,primaryKey);
    }

    @Override
    public boolean updateRecord(ITable table, ArrayList<Object> oldRecord, ArrayList<Object> newRecord) {
        return pageBuffer.updateRecord(table,oldRecord,newRecord);
    }

    @Override
    public void purgePageBuffer() {
        // TODO PAGE BUFFER PURGE
    }

    @Override
    public boolean addAttributeValue(ITable table, Object defaultValue) {
        // TODO ADD ATTRIBUTE VALUE
        return false;
    }

    @Override
    public boolean dropAttributeValue(ITable table, int attrIndex) {
        // TODO DROP ATTRIBUTE VALUE
        return false;
    }
}
