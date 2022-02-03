package storagemanager;

import common.ITable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StorageManager extends AStorageManager{

    HashMap<String,ArrayList<ArrayList<Object>>> records = new HashMap<String,ArrayList<ArrayList<Object>>>();

    @Override
    public boolean clearTableData(ITable table) {
        return false;
    }

    @Override
    public ArrayList<Object> getRecord(ITable table, Object pkValue) {
        if(!records.containsKey(table.getTableName())){
            return null;
        }
        for(ArrayList<Object> recordList : records.get(table.getTableName())){
            int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
            String primaryKeyType = table.getPrimaryKey().getAttributeType();

                switch (primaryKeyType) {
                    case "Integer":
                        Integer intValue = (Integer) recordList.get(primaryKeyIndex);
                        if(intValue == pkValue){
                            return recordList;
                        }
                        break;

                    case "Double":
                        Double dblValue = (Double) recordList.get(primaryKeyIndex);
                        if(dblValue == pkValue){
                            return recordList;
                        }
                        break;

                    case "Boolean":
                        Boolean boolValue = (Boolean) recordList.get(primaryKeyIndex);
                        if(boolValue == pkValue){
                            return recordList;
                        }
                        break;

                    case "Varchar":
                    case "Char":
                        String strValue = (String) recordList.get(primaryKeyIndex);
                        if(strValue == pkValue){
                            return recordList;
                        }
                        break;
                    default:
                }

        }
        return null;
    }

    @Override
    public ArrayList<ArrayList<Object>> getRecords(ITable table) {
        if(!records.containsKey(table.getTableName())){
            return new ArrayList<>();
        }
        return (ArrayList<ArrayList<Object>>) records.get(table.getTableName()).clone();
    }

    @Override
    public boolean insertRecord(ITable table, ArrayList<Object> record) {
        if(!records.containsKey(table.getTableName())){
            records.put(table.getTableName(), new ArrayList<>());
        }
        ArrayList<ArrayList<Object>> tableRecords = records.get(table.getTableName());
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for(ArrayList<Object> recordList : tableRecords){
            if(recordList.get(primaryKeyIndex).equals(record.get(primaryKeyIndex))){
                return false;
            }
        }
        records.get(table.getTableName()).add(record);
        return true;
    }

    @Override
    public boolean deleteRecord(ITable table, Object primaryKey) {
        if(!records.containsKey(table.getTableName())){
            return false;
        }
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        String primaryKeyType = table.getPrimaryKey().getAttributeType();
        for(int i = 0; i <  records.get(table.getTableName()).size();i++){
            ArrayList<Object> recordList  = records.get(table.getTableName()).get(i);
            switch (primaryKeyType) {
                case "Integer":
                    Integer intValue = (Integer) recordList.get(primaryKeyIndex);
                    if(intValue == primaryKey){
                        records.get(table.getTableName()).remove(i);
                        return true;
                    }
                    break;

                case "Double":
                    Double dblValue = (Double) recordList.get(primaryKeyIndex);
                    if(dblValue == primaryKey){
                        records.get(table.getTableName()).remove(i);
                        return true;
                    }
                    break;

                case "Boolean":
                    Boolean boolValue = (Boolean) recordList.get(primaryKeyIndex);
                    if(boolValue == primaryKey){
                        records.get(table.getTableName()).remove(i);
                        return true;
                    }
                    break;

                case "Varchar":
                case "Char":
                    String strValue = (String) recordList.get(primaryKeyIndex);
                    if(strValue == primaryKey){
                        records.get(table.getTableName()).remove(i);
                        return true;
                    }
                    break;
                default:
            }

        }
        return false;
    }

    @Override
    public boolean updateRecord(ITable table, ArrayList<Object> oldRecord, ArrayList<Object> newRecord) {
        ArrayList<ArrayList<Object>> tableRecords = records.get(table.getTableName());
        if(tableRecords != null){
            int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
            for(int i = 0; i < tableRecords.size();i++){
                ArrayList<Object> recordList = tableRecords.get(i);
                if(recordList.get(primaryKeyIndex).equals(oldRecord.get(primaryKeyIndex))){
                    records.get(table.getTableName()).remove(i);
                    records.get(table.getTableName()).add(i,newRecord);
                    return true;
                }
            }
        }
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
