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
        if(getAttrByName(name) != null){
            return false;
        }
        lstAttribute.add(new Attribute(name, type));
        return false;
    }

    @Override
    public boolean dropAttribute(String name) {
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
        if(foreignKey != null){
            return false;
        }
        this.foreignKey = fk;
        return true;
    }

    @Override
    public boolean addIndex(String attributeName) {
        if(index != null){
            return false;
        }
        this.index = attributeName;
        return true;
    }
}