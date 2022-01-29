package common;

import java.util.ArrayList;

public class Table implements ITable {

    private String tableName;
    private int tableId;
    private ArrayList<Attribute> lstAttribute = new ArrayList<Attribute>();
    private Attribute primaryKey;
    private ForeignKey foreignKey;
    private String index;
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
        // Iterate through attribute list to try and find a matching name
        // If not found, return null
        for(Attribute attr : lstAttribute){
            if(attr.getAttributeName().equals(name)){
                return attr;
            }
        }
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
        // Attributes of the same name are not allowed, check and return false if match is found
        if(getAttrByName(name) != null){
            return false;
        }
        lstAttribute.add(new Attribute(name, type));
        return false;
    }

    @Override
    public boolean dropAttribute(String name) {
        // Iterate through the list to try and find an attrib with the same name
        // If found, drop it and return true. If no match, fall back on returning false
        for(Attribute attr:lstAttribute){
            if(attr.getAttributeName().equals(name)){
                lstAttribute.remove(attr);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addForeignKey(ForeignKey fk) {
        // Check that the foreign key object is not already set, if so return false
        if(foreignKey != null){
            return false;
        }
        this.foreignKey = fk;
        return true;
    }

    @Override
    public boolean addIndex(String attributeName) {
        // Ensure an index is not already set. If so, return false
        if(index != null){
            return false;
        }
        this.index = attributeName;
        return true;
    }
}