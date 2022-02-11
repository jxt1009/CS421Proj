package catalog;

import common.Attribute;
import common.ITable;
import common.Table;
import storagemanager.Page;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Catalog extends ACatalog {

    private String location;
    private int pageSize;
    private int pageBufferSize;
    private File catalogFile;
    HashMap<String,Table> tables = new HashMap<String,Table>();


    public Catalog(String location, int pageSize, int pageBufferSize) {
        this.location = location;
        this.pageSize = pageSize;
        this.pageBufferSize = pageBufferSize;
        File[] listOfFiles = new File(location).listFiles();

        if(listOfFiles != null && listOfFiles.length > 0) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().equals("catalog")) {
                        catalogFile = file;
                        loadCatalogFromDisk();
                    }
                }
            }
        }
    }

    private void loadCatalogFromDisk(){
        try {
            FileInputStream fin  = new FileInputStream(catalogFile);

            DataInputStream dis = new DataInputStream(fin);
            pageBufferSize = dis.readInt();
            pageSize = dis.readInt();

            int numTables = dis.readInt();
            System.out.println(numTables);
            for(int i = 0; i < numTables;i++) {
                int tableNameLen = dis.readInt();
                String tableName = readChars(tableNameLen, dis);
                int primaryKeyNameLen = dis.readInt();
                String primaryKeyName = readChars(primaryKeyNameLen, dis);
                int primaryKeyTypeLen = dis.readInt();
                String primaryKeyType = readChars(primaryKeyTypeLen, dis);
                int numAttributes = dis.readInt();
                ArrayList<Attribute> tableAttributes = new ArrayList<>();
                for(int attrib = 0; attrib < numAttributes;attrib++){
                    int attribNameLen = dis.readInt();
                    String attribName = readChars(attribNameLen,dis);
                    int attribTypeLen = dis.readInt();
                    String attribType = readChars(attribTypeLen,dis);
                    tableAttributes.add(new Attribute(attribName,attribType));
                }
                int numPageIDs = dis.readInt();
                Table newTable = new Table(tableName,tableAttributes,new Attribute(primaryKeyName,primaryKeyType));
                for(int pageIndex = 0; pageIndex < numPageIDs; pageIndex++){
                    newTable.addPage(dis.readInt());
                }

                System.out.println(tableName + " " + primaryKeyName + " " + primaryKeyType);

                tables.put(tableName,newTable);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readChars(int len,DataInputStream in){
        String finalString = "";
        for(int i = 0; i < len; i ++){
            try {
                char c = in.readChar();
                finalString += c;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return finalString;
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
            saveToDisk();
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
            tables.remove(tableName);
            saveToDisk();
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
        // TODO
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
        try {
            catalogFile = new File(location + "/catalog");
            if(!catalogFile.exists()){
                new File(location).mkdirs();
                try {
                    catalogFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fout = new FileOutputStream(catalogFile);
            DataOutputStream dos = new DataOutputStream(fout);
            dos.writeInt(pageBufferSize);
            dos.writeInt(pageSize);
            dos.writeInt(tables.size());
            for(String t : tables.keySet()){
                Table table = tables.get(t);
                dos.writeInt(table.getTableName().length());
                dos.writeChars(table.getTableName());

                dos.writeInt(table.getPrimaryKey().attributeName().length());
                dos.writeChars(table.getPrimaryKey().attributeName());

                dos.writeInt(table.getPrimaryKey().attributeType().length());
                dos.writeChars(table.getPrimaryKey().attributeType());

                dos.writeInt(table.getAttributes().size());
                for(Attribute attribute : table.getAttributes()){
                    dos.writeInt(attribute.getAttributeName().length());
                    dos.writeChars(attribute.getAttributeName());
                    dos.writeInt(attribute.getAttributeType().length());
                    dos.writeChars(attribute.getAttributeType());
                }

                dos.writeInt(table.getPageList().size());
                for(Integer pageID : table.getPageList()) {
                    dos.writeInt(pageID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
