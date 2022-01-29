package common;

import java.util.ArrayList;

public class Table implements ITable {

    private String tableName;
    private int tableId;
    private ArrayList<Attribute> lstAttribute = new ArrayList<Attribute>();
    private Attribute primaryKey;
    private ArrayList<ForeignKey> lstForeignKeys = new ArrayList<ForeignKey>();

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String name) {
        this.tableName = name;
    }

    @Override
    public int getTableId() {
        return tableId;
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return lstAttribute;
    }

    @Override
    public Attribute getAttrByName(String name) {
        return null;
    }

    @Override
    public Attribute getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public ArrayList<ForeignKey> getForeignKeys() {
        return lstForeignKeys;
    }

    @Override
    public boolean addAttribute(String name, String type) {
        return false;
    }

    @Override
    public boolean dropAttribute(String name) {
        return false;
    }

    @Override
    public boolean addForeignKey(ForeignKey fk) {
        return false;
    }

    @Override
    public boolean addIndex(String attributeName) {
        return false;
    }
}