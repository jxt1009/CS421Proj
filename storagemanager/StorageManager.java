package storagemanager;

import common.ITable;

import java.util.ArrayList;

public class StorageManager extends AStorageManager{
    private int attrIndex;
    private ArrayList<Object> record = new ArrayList<>();
    private ArrayList<Object> oldRecord = new ArrayList<>();
    private ArrayList<Object> newRecord = new ArrayList<>();
    private Object primaryKey;



    @Override
    public boolean clearTableData(ITable table) {
        return false;
    }

    @Override
    public ArrayList<Object> getRecord(ITable table, Object pkValue) {
        return null;
    }

    @Override
    public ArrayList<ArrayList<Object>> getRecords(ITable table) {
        return null;
    }

    @Override
    public boolean insertRecord(ITable table, ArrayList<Object> record) {
        return false;
    }

    @Override
    public boolean deleteRecord(ITable table, Object primaryKey) {
        return false;
    }

    @Override
    public boolean updateRecord(ITable table, ArrayList<Object> oldRecord, ArrayList<Object> newRecord) {
        return false;
    }

    @Override
    public void purgePageBuffer() {

    }

    @Override
    public boolean addAttributeValue(ITable table, Object defaultValue) {
        return false;
    }

    @Override
    public boolean dropAttributeValue(ITable table, int attrIndex) {
        return false;
    }
}
