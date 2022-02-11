package catalog;

import common.Attribute;
import common.ITable;
import common.Table;

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
                    if (file.getName().equals("catalog.db")) {
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
            pageSize = dis.readInt();
            pageBufferSize = dis.readInt();

            System.out.println(pageBufferSize + " " + pageSize);
            String tableName = readChars(dis.readInt(),dis);
            System.out.println(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readChars(int len,DataInputStream in){
        String finalString = "";
        for(int i = 0; i < len; i ++){
            try {
                finalString += in.readChar();
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
            catalogFile = new File(location + "/catalog.db");
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
            for(String t : tables.keySet()){
                Table table = tables.get(t);
                dos.writeInt(table.getTableName().length());
                dos.writeChars(table.getTableName());
                System.out.println(table.getTableName());
                for(Attribute attribute : tables.get(t).getAttributes()){
                    dos.writeInt(attribute.getAttributeName().length());
                    dos.writeChars(attribute.getAttributeName());
                    dos.writeInt(attribute.getAttributeType().length());
                    dos.writeChars(attribute.getAttributeType());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
