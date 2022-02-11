package storagemanager;

import catalog.ACatalog;
import common.Attribute;
import common.ITable;
import common.Table;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class BufferManager {

    private FileOutputStream fileOutputStream;
    private ArrayList<Page> buffer = new ArrayList<>();
    private int pageSize;
    private int pageLimit;
    private String location;
    private String pageFolder ;
    private File pageDir;
    private int pageIDIndex = 0;

    public BufferManager() {
        this.pageLimit = ACatalog.getCatalog().getPageBufferSize();
        this.pageSize = ACatalog.getCatalog().getPageSize();
        this.location = ACatalog.getCatalog().getDbLocation();
        this.pageFolder = location + "/pages";
        this.pageDir = new File(pageFolder);
        if (!pageDir.exists()) {
            pageDir.mkdir();
        }
    }

    public ArrayList<Page> loadAllPages(Table table) {
        ArrayList<Page> pages = buffer;
        for (Integer fileName : table.getPageList()) {
            Page page = findPageInBuffer(fileName);
            if (page == null) {
                page = new Page(table, pageDir.getPath() + "/" + fileName, fileName);
                addPageToBuffer(table, page);
            }
            pages.add(page);
        }
        return pages;
    }

    public Page findPageInBuffer(Integer searchPage) {
        for (Page page : buffer) {
            if (Objects.equals(searchPage, page.getPageId())) {
                return page;
            }
        }
        return null;
    }

    public void clearPageBuffer() {
        // TODO CLEAR PAGE BUFFER
        // clear page buffer
    }

    public void updateBuffer(Table table) {
        while (buffer.size() > pageLimit) {
            Page p = buffer.get(0);
            buffer.remove(0);
            writeToDisk(table, p);
        }
    }

    private void writeToDisk(Table table, Page p) {
        if (!pageDir.exists()) {
            pageDir.mkdirs();
        }
        File finalPageFile = new File(pageFolder + "/" + p.getPageId());
        try {
            if (!finalPageFile.exists()) {
                finalPageFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(finalPageFile);
            DataOutputStream outputStream = new DataOutputStream(fileOutputStream);
            outputStream.writeInt(p.getPageId());
            outputStream.writeInt(p.getRecords().size());
            for (int i = 0; i < p.getRecords().size(); i++) {
                ArrayList<Object> records = p.getRecords().get(i);
                for(int j = 0; j < records.size(); j++) {
                    Attribute type = table.getAttributes().get(j);
                    switch (type.getAttributeType()) {
                        case "Integer":
                            Integer intValue = (Integer) records.get(j);
                            outputStream.writeInt(intValue);
                            break;

                        case "Double":
                            Double dblValue = (Double) records.get(j);
                            outputStream.writeDouble(dblValue);
                            break;

                        case "Boolean":
                            Boolean boolValue = (Boolean) records.get(j);
                            outputStream.writeBoolean(boolValue);
                            break;

                        case "Varchar":
                        case "Char":
                            // TODO write out strings in files
                            String strValue = (String) records.get(j);
                            System.out.println("strValue");

                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPageToBuffer(Table table, Page page) {
        if(!buffer.contains(page)) {
            buffer.add(page);
            updateBuffer(table);
            pageIDIndex += 1;
        }
    }

    private Page addNewPage(Table table) {
        Page page = new Page(table, pageIDIndex);
        if (!table.addPage(page)) {
            return null;
        }
        addPageToBuffer(table, page);
        return page;
    }

    public ArrayList<Object> getRecord(ITable itable, Object pkValue) {
        Table table = (Table) itable;
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        Page searchPage = searchForPage(itable, pkValue);
        if (searchPage != null) {
            for (int i = 0; i < searchPage.getRecords().size(); i++) {
                ArrayList<Object> record = searchPage.getRecords().get(i);
                Object primaryKeyValue = record.get(primaryKeyIndex);
                if (primaryKeyValue != null && primaryKeyValue.equals(pkValue)) {
                    return record;
                }
            }
        }
        return null;
    }

    private Page searchForPage(ITable itable, Object pkValue) {
        Table table = (Table) itable;
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for (Page page : loadAllPages(table)) {
            if (page.getRecords().size() > 0) {
                for (int i = 0; i < page.getRecords().size(); i++) {
                    ArrayList<Object> record = page.getRecords().get(i);
                    Object primaryKeyValue = record.get(primaryKeyIndex);
                    if (primaryKeyValue != null && primaryKeyValue.equals(pkValue)) {
                        return page;
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<ArrayList<Object>> getAllRecords(ITable table) {
        Table castedTable = (Table) table;
        ArrayList<Page> tablePages = loadAllPages(castedTable);
        System.out.println(((Table) table).getPageList().size() + " " +tablePages.size());
        ArrayList<ArrayList<Object>> allRecords = new ArrayList<>();
        for (Page page : tablePages) {
            allRecords.addAll(page.getRecords());
        }
        return allRecords;
    }

    public boolean insertRecord(ITable itable, ArrayList<Object> record) {
        Table table = (Table) itable;
        ArrayList<Page> tablePages = loadAllPages(table);
        if (tablePages.size() == 0) {
            tablePages.add(addNewPage(table));
        }
        for (Page page : tablePages) {
            int canAdd = canAddRecord(table, page, record);
            if (canAdd != -1) {
                if (!page.hasSpace()) {
                    //TODO IMPLEMENT PAGE SPLITTING
                    //cutRecords(itable, page, canAdd);
                }
                return page.addRecord(table, record);
            }
        }
        return false;
    }

    private int canAddRecord(Table table, Page page, ArrayList<Object> record) {
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        if (page.getRecords().size() == 0) {
            return 0;
        }
        Object recordVal = record.get(primaryKeyIndex);
        int recordIndex = 0;
        for (ArrayList<Object> recordList : page.getRecords()) {
            if (recordVal instanceof Integer) {
                if ((int) recordList.get(primaryKeyIndex) > (int) recordVal) {
                    return recordIndex;
                }
            } else if (recordVal instanceof Double) {
                if ((double) recordList.get(primaryKeyIndex) > (double) recordVal) {
                    return recordIndex;
                }
            } else if (recordVal instanceof String || recordVal instanceof Character) {
                if (((String) recordVal).compareTo(recordList.get(primaryKeyIndex).toString()) > 0) {
                    return recordIndex;
                }
            }
            recordIndex += 1;
        }
        return recordIndex;
    }

    public boolean updateRecord(ITable table, ArrayList<Object> oldRecord, ArrayList<Object> newRecord) {
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        Object primaryKey = oldRecord.get(primaryKeyIndex);
        Page updatePage = searchForPage(table, primaryKey);
        if (updatePage != null) {
            return updatePage.updateRecord(table, primaryKey, newRecord);
        }
        return false;
    }

    public boolean deleteRecord(ITable itable, Object pkValue) {
        Table table = (Table) itable;
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        for (Page page : loadAllPages(table)) {
            if (page.getRecords().size() > 0) {
                for (int i = 0; i < page.getRecords().size(); i++) {
                    ArrayList<Object> record = page.getRecords().get(i);
                    Object primaryKeyValue = record.get(primaryKeyIndex);
                    if (primaryKeyValue != null && primaryKeyValue.equals(pkValue)) {
                        page.getRecords().remove(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean cutRecords(ITable itable, Page page, int cutIndex) {
        Table table = (Table) itable;
        ArrayList<ArrayList<Object>> firstHalfRecords = new ArrayList<ArrayList<Object>>(page.getRecords().subList(0, cutIndex));
        ArrayList<ArrayList<Object>> secondHalfRecords = new ArrayList<ArrayList<Object>>(page.getRecords().subList(cutIndex+1, page.getRecords().size()-1));
        Page firstPage = new Page(table, pageIDIndex, firstHalfRecords);
        Page secondPage = new Page(table, pageIDIndex + 1, secondHalfRecords);
        removePageFromBuffer(table, page);
        addPageToBuffer(table, firstPage);
        addPageToBuffer(table, secondPage);
        return true;
    }

    private void removePageFromBuffer(Table table, Page page) {
        table.removePage(page);
        buffer.remove(page);
    }
}
