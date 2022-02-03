package catalog;

import common.Attribute;
import common.ITable;
import common.Table;

import java.util.ArrayList;
import java.util.HashMap;

public class Catalog extends ACatalog {

    private String location;
    private int pageSize;
    private int pageBufferSize;
    HashMap<String,Table> tables = new HashMap<String,Table>();


    public Catalog(String location, int pageSize, int pageBufferSize) {
        this.location = location;
        this.pageSize = pageSize;
        this.pageBufferSize = pageBufferSize;
    }

    @Override
    public String getDbLocation() {
        return this.location;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public int getPageBufferSize() {
        return this.pageBufferSize;
    }

    @Override
    public boolean containsTable(String tableName) {
        return tables.containsKey(tableName);
    }

    @Override
    public ITable addTable(String tableName, ArrayList<Attribute> attributes, Attribute primaryKey) {
        Table newTable = new Table(tableName,attributes,primaryKey);
        if(!tables.containsKey(tableName)){
            tables.put(tableName,newTable);
            return newTable;
        }
        return null;
    }

    @Override
    public ITable getTable(String tableName) {
        if(containsTable(tableName)){
            return tables.get(tableName);
        }
        return null;
    }

    @Override
    public boolean dropTable(String tableName) {
        if(containsTable(tableName)){
            System.out.println(tables.remove(tableName));
            return true;
        }
        return false;
    }

    /**
     * FOR LATER PHASES
     */
    @Override
    public boolean alterTable(String tableName, Attribute attr, boolean drop, Object defaultValue) {
        return false;
    }


    @Override
    public boolean clearTable(String tableName) {
        return false;
    }

    /**
     * FOR LATER PHASES
     */
    @Override
    public boolean addIndex(String tableName, String indexName, String attrName) {
        return false;
    }

    /**
     * FOR LATER PHASES
     */
    @Override
    public boolean dropIndex(String tableName, String indexName) {
        return false;
    }

    @Override
    public boolean saveToDisk() {
        return false;
    }
}
