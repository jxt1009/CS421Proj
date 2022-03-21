package storagemanager;

import catalog.ACatalog;
import common.Attribute;
import common.ITable;
import common.Table;

import java.io.*;
import java.util.*;

public class BufferManager {

    private final ArrayList<Page> buffer = new ArrayList<>();
    private final int pageLimit;
    private final String pageFolder;
    private final File pageDir;
    private List<Integer> pageList = new ArrayList<Integer>();

    public BufferManager() {
        this.pageLimit = ACatalog.getCatalog().getPageBufferSize();
        String location = ACatalog.getCatalog().getDbLocation();
        this.pageFolder = location + "/pages";
        this.pageDir = new File(pageFolder);
        if (!pageDir.exists()) {
            pageDir.mkdir();
        }else{
            String[] pathnames = pageDir.list();

            // For each pathname in the pathnames array
            for (String pathname : pathnames) {
                // Print the names of files and directories
                pageList.add(Integer.parseInt(pathname));
            }
        }
    }

    public ArrayList<Page> loadAllPages(Table table) {
        ArrayList<Page> pages = new ArrayList<>();
        for (Integer fileName : table.getPageList()) {
            Page page = findPageInBuffer(fileName);
            if (page == null) {
                page = new Page(table, pageDir.getPath() + "/" + fileName, fileName);
                addPageToBuffer(page);
                table.addPage(fileName);
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
        boolean success = true;
        for (Page page : buffer) {
            success = success && writeToDisk(page);
        }
        if (!success) {
            System.err.println("Error purging page buffer");
        }
        buffer.clear();
    }

    public void updateBuffer() {
        while (buffer.size() > pageLimit) {
            Page p = buffer.get(0);
            if (!p.hasSpace()) {
                cutRecords(p.getTable(), p, p.getRecords().size() / 2);
                continue;
            }
            buffer.remove(0);
            writeToDisk(p);
        }
    }

    private boolean writeToDisk(Page p) {
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
                RecordHelper.formatRecord(p.getTable(),records);
                for (int j = 0; j < records.size(); j++) {
                    Object record = records.get(j);
                    String type = p.getTable().getAttributes().get(j).getAttributeType();
                    if (type.equalsIgnoreCase("Integer")) {
                        outputStream.writeInt((Integer) record);
                    } else if (type.equalsIgnoreCase("Double")) {
                        outputStream.writeDouble((Double) record);
                    } else if (type.equalsIgnoreCase("Boolean")) {
                        outputStream.writeBoolean((boolean) record);
                    } else if (type.toLowerCase().startsWith("varchar")) {
                        String outputString = (String) record;
                        FileManager.writeChars(outputString, outputStream);
                    } else if (type.toLowerCase().startsWith("char")) {
                        int charLen = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                        String outputString = (String) record;
                        outputStream.writeInt(charLen);
                        for (int readIndex = 0; readIndex < charLen; readIndex++) {
                            if (readIndex > outputString.length() - 1) {
                                outputStream.writeChar('\t');
                            } else {
                                outputStream.writeChar(outputString.charAt(readIndex));
                            }
                        }
                    }
                }

            }
            outputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing page file to disk: " + p);
            e.printStackTrace();
        }
        return false;
    }

    private void addPageToBuffer(Page page) {
        buffer.remove(page);
        buffer.add(page);
        pageList.add(page.getPageId());
    }

    private Page addNewPage(Table table) {
        Page page = new Page(table, getAvailablePageID());
        table.addPage(page.getPageId());
        addPageToBuffer(page);
        updateBuffer();
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
                    updateBuffer();
                    return record;
                }
            }
        }
        updateBuffer();
        return null;
    }

    private Page searchForPage(ITable itable, Object pkValue) {
        Table table = (Table) itable;
        int primaryKeyIndex = table.getPrimaryKeyIndex();
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
        ArrayList<ArrayList<Object>> allRecords = new ArrayList<>();
        for (Page page : tablePages) {
            allRecords.addAll(page.getRecords());
        }
        return allRecords;
    }

    public boolean insertRecord(ITable itable, ArrayList<Object> record) {
        Table table = (Table) itable;
        ArrayList<Page> tablePages = loadAllPages(table);
        if (record == null) {
            System.err.println("Record cannot be null.");
            return false;
        }
        if (RecordHelper.formatRecord(table, record) == null) {
            System.err.println("Record improperly formatted");
            return false;
        }
        if (tablePages.size() == 0) {
            Page p = addNewPage(table);
            return p.addRecord(table, record, 0);
        }

        if (!table.checkNonNullAttributes(record)) {
            System.err.println("Record contains null values in a non-null column.");
            return false;
        }
        int canAdd = -2;
        for (Page page : tablePages) {
            canAdd = canAddRecord(table, page, record);
            if (canAdd > -1) {
                if (!page.hasSpace()) {
                    page = cutRecords(table, page, canAdd);
                }
                return page.addRecord(table, record, canAdd);
            }
        }
        if (canAdd == -1) {
            Page p = tablePages.get(tablePages.size() - 1);
            updateBuffer();
            return p.addRecord(table, record, p.getRecords().size());
        } else {
            return false;
        }
    }

    private int canAddRecord(Table table, Page page, ArrayList<Object> record) {
        Object recordVal = record.get(table.getPrimaryKeyIndex());
        Attribute primaryKeyAttribute = table.getPrimaryKey();
        if (page.getRecords().size() <= 1) {
            Object compareVal = page.getRecords().get(0).get(table.getPrimaryKeyIndex());
            if (RecordHelper.equals(compareVal, recordVal, primaryKeyAttribute)) {
                return -2;
            }
            if (RecordHelper.compareObjects(recordVal, compareVal, primaryKeyAttribute)) {
                return 0;
            } else {
                return 1;
            }
        }
        for (int i = 1; i < page.getRecords().size(); i++) {
            Object previousVal = page.getRecords().get(i - 1).get(table.getPrimaryKeyIndex());
            Object compareVal = page.getRecords().get(i).get(table.getPrimaryKeyIndex());
            if (RecordHelper.equals(compareVal, recordVal, primaryKeyAttribute)
                    || RecordHelper.equals(previousVal, recordVal, primaryKeyAttribute)) {
                return -2;
            }
            if (RecordHelper.compareObjects(previousVal, recordVal, primaryKeyAttribute)
                    && RecordHelper.compareObjects(recordVal, compareVal, primaryKeyAttribute)) {
                return i;
            }
            if (i == 1 && RecordHelper.compareObjects(recordVal, previousVal, primaryKeyAttribute)) {
                return 0;
            }
        }
        return -1;
    }


    public boolean updateRecord(ITable table, ArrayList<Object> oldRecord, ArrayList<Object> newRecord) {
        Object primaryKey = oldRecord.get(((Table) table).getPrimaryKeyIndex());
        Page updatePage = searchForPage(table, primaryKey);
        if (updatePage != null) {
            updateBuffer();
            return updatePage.updateRecord(table, primaryKey, newRecord);
        }
        System.err.println("Could not find page for update");
        updateBuffer();
        return false;
    }

    public boolean deleteRecord(ITable itable, Object pkValue) {
        Table table = (Table) itable;
        Page updatePage = searchForPage(table, pkValue);

        if (updatePage != null) {
            updatePage.deleteRecord(table, pkValue);
            updateBuffer();
            return true;
        }
        return false;
    }

    public Page cutRecords(ITable itable, Page page, int cutIndex) {
        Table table = (Table) itable;
        ArrayList<ArrayList<Object>> firstHalfRecords = new ArrayList<>(page.getRecords().subList(0, cutIndex));
        ArrayList<ArrayList<Object>> secondHalfRecords = new ArrayList<>(page.getRecords().subList(cutIndex, page.getRecords().size()));
        removePageFromBuffer(page);
        Page firstPage = new Page(table, getAvailablePageID(), firstHalfRecords);
        addPageToBuffer(firstPage);
        Page secondPage = new Page(table, getAvailablePageID(), secondHalfRecords);
        table.insertPage(page.getPageId(), firstPage.getPageId(), secondPage.getPageId());
        addPageToBuffer(secondPage);
        return firstPage;
    }

    private int getAvailablePageID(){
        if(pageList.size() == 0){
            return 0;
        }
        return pageList.get(pageList.size() -1) + 1;
    }

    private void removePageFromBuffer(Page page) {
        buffer.remove(page);
        new File(pageFolder + "/" + page.getPageId()).delete();
    }


    public boolean addAttributeValue(ITable table1, Object defaultValue) {
        Table table = (Table) table1;
        for(ArrayList<Object> record : getAllRecords(table)){
            record.add(defaultValue);
        }
        return true;
    }
}
