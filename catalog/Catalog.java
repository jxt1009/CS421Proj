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
            // Write out global vars
            dos.writeInt(pageBufferSize);
            dos.writeInt(pageSize);
            // Write out the number of tables so load loop can correctly read in
            dos.writeInt(tables.size());
            // For all the tables
            for(String t : tables.keySet()){
                Table table = tables.get(t);
                // Write out the length of the table name and the table name
                dos.writeInt(table.getTableName().length());
                dos.writeChars(table.getTableName());
                // Write out the length of the attribute name and the name
                dos.writeInt(table.getPrimaryKey().attributeName().length());
                dos.writeChars(table.getPrimaryKey().attributeName());

                // Write out the length of the attribute type and the type
                dos.writeInt(table.getPrimaryKey().attributeType().length());
                dos.writeChars(table.getPrimaryKey().attributeType());

                // Write out the number of attributes before the attribute section
                dos.writeInt(table.getAttributes().size());

                // Iterate through attributes and write out the len/value for each
                for(Attribute attribute : table.getAttributes()){
                    // Write the length of the attribute name and value
                    dos.writeInt(attribute.getAttributeName().length());
                    dos.writeChars(attribute.getAttributeName());

                    // Write the length of the attribute type and value
                    dos.writeInt(attribute.getAttributeType().length());
                    dos.writeChars(attribute.getAttributeType());
                }

                // Write out length of page ID list
                dos.writeInt(table.getPageList().size());
                for(Integer pageID : table.getPageList()) {
                    // Write out pageIDs to file
                    dos.writeInt(pageID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadCatalogFromDisk(){
        try {
            FileInputStream fin  = new FileInputStream(catalogFile);

            DataInputStream dis = new DataInputStream(fin);
            // First two ints are pageBuf size and pageSize
            pageBufferSize = dis.readInt();
            pageSize = dis.readInt();

            // Third int is the num of tables
            int numTables = dis.readInt();
            for(int i = 0; i < numTables;i++) {
                // Get the length of string to read in
                int tableNameLen = dis.readInt();
                // Read in table name based on # of chars to parse
                String tableName = readChars(tableNameLen, dis);

                // Read in the length of the primaryKeyName
                int primaryKeyNameLen = dis.readInt();
                // Read in primary key name based on len
                String primaryKeyName = readChars(primaryKeyNameLen, dis);

                // Read in length of primary key type
                int primaryKeyTypeLen = dis.readInt();
                // Read in primary key type based on len
                String primaryKeyType = readChars(primaryKeyTypeLen, dis);

                // Parse num of attributes to read in
                int numAttributes = dis.readInt();
                ArrayList<Attribute> tableAttributes = new ArrayList<>();
                for(int attrib = 0; attrib < numAttributes;attrib++){
                    // Get length of attrib name to read in
                    int attribNameLen = dis.readInt();
                    // Read in attrib name based on len
                    String attribName = readChars(attribNameLen,dis);

                    // Get length of attrib type
                    int attribTypeLen = dis.readInt();
                    // Read in attrib type with given len
                    String attribType = readChars(attribTypeLen,dis);

                    // Add new attribute to list for new table
                    tableAttributes.add(new Attribute(attribName,attribType));
                }

                // Parse how many page ID values to read in
                int numPageIDs = dis.readInt();
                // Create the new table to add the pages to
                Table newTable = new Table(tableName,tableAttributes,new Attribute(primaryKeyName,primaryKeyType));
                for(int pageIndex = 0; pageIndex < numPageIDs; pageIndex++){
                    // Read in page ID and add it to the table
                    newTable.addPage(dis.readInt());
                }

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

}
