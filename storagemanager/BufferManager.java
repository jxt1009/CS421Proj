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
    private int pageIDIndex = 0;

    public BufferManager() {
        this.pageLimit = ACatalog.getCatalog().getPageBufferSize();
        String location = ACatalog.getCatalog().getDbLocation();
        this.pageFolder = location + "/pages";
        this.pageDir = new File(pageFolder);
        if (!pageDir.exists()) {
            pageDir.mkdir();
        }
    }

    public ArrayList<Page> loadAllPages(Table table) {
        ArrayList<Page> pages = new ArrayList<>();
        for (Integer fileName : table.getPageList()) {
            Page page = findPageInBuffer(fileName);
            if (page == null) {
                page = new Page(table, pageDir.getPath() + "/" + fileName, fileName);
                addPageToBuffer(page);
            }
            pages.add(page);
        }
        Collections.sort(pages, (p1, p2) -> compareObjects(p1.getSmallestPrimaryKey(), p2.getSmallestPrimaryKey()) ? -1 : 1);
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
        for (Page page : buffer) {
            writeToDisk(page);
        }
        buffer.clear();
    }

    public void updateBuffer() {
        while (buffer.size() > pageLimit) {
            Page p = buffer.get(0);
            buffer.remove(0);
            writeToDisk(p);
        }
    }

    private void writeToDisk(Page p) {
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
                for (int j = 0; j < records.size(); j++) {
                    Object record = records.get(j);
                    String type = p.getTable().getAttributes().get(j).getAttributeType();
                    if (type.equals("Integer")) {
                        outputStream.writeInt((Integer) record);
                    } else if (type.equals("Double")) {
                        outputStream.writeDouble((Double) record);
                    } else if (type.equals("Boolean")) {
                        outputStream.writeBoolean((Boolean) record);
                    } else if (type.startsWith("Varchar")) {
                        String outputString = (String) record;
                        FileManager.writeChars(outputString, outputStream);
                    } else if (type.startsWith("Char")) {
                        int charLen = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                        String outputString = (String) record;
                        FileManager.writeChars(outputString, outputStream);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPageToBuffer(Page page) {
        if (buffer.contains(page)) {
            buffer.remove(page);
        }
        buffer.add(page);
        updateBuffer();
    }

    private Page addNewPage(Table table) {
        pageIDIndex += 1;
        Page page = new Page(table, pageIDIndex);
        addPageToBuffer(page);
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
            Page p = addNewPage(table);
            p.addRecord(table, record, 0);
            return true;

        }
        for (int i = 0; i < tablePages.size(); i++) {
            Page page = tablePages.get(i);
            int canAdd = canAddRecord(table, page, record);
            if (canAdd != -1) {
                if (!page.hasSpace(record)) {
                    page = cutRecords(table, page, canAdd);
                }
                return page.addRecord(table, record, canAdd);
            }
        }
        Page p = tablePages.get(tablePages.size()-1);
        p.addRecord(table, record, p.getRecords().size());
        return true;
    }

    private int canAddRecord(Table table, Page page, ArrayList<Object> record) {
        int primaryKeyIndex = table.getAttributes().indexOf(table.getPrimaryKey());
        Object recordVal = record.get(primaryKeyIndex);
        if (page.getRecords().size() == 1) {
            Object compareVal = page.getRecords().get(0).get(primaryKeyIndex);
            if (compareObjects(recordVal, compareVal)) {
                return 0;
            } else {
                return 1;
            }
        }
        for (int i = 1; i < page.getRecords().size(); i++) {
            Object previousVal = page.getRecords().get(i - 1).get(primaryKeyIndex);
            Object compareVal = page.getRecords().get(i).get(primaryKeyIndex);
            if (compareObjects(previousVal, recordVal) && compareObjects(recordVal, compareVal)) {
                return i;
            }
            if (i == 1 && compareObjects(recordVal, previousVal)) {
                return 0;
            }
        }
        return -1;
    }

    private boolean compareObjects(Object o1, Object o2) {
        if (o1 instanceof Integer) {
            if ((int) o1 < (int) o2) {
                return true;
            }
        } else if (o1 instanceof Double) {
            if ((double) o1 < (double) o2) {
                return true;
            }
        } else if (o1 instanceof String || o1 instanceof Character) {
            if (((String) o1).compareTo((String) o2) < 0) {
                return true;
            }
        }
        return false;
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

    public Page cutRecords(ITable itable, Page page, int cutIndex) {
        Table table = (Table) itable;
        ArrayList<ArrayList<Object>> firstHalfRecords = new ArrayList<>(page.getRecords().subList(0, cutIndex));
        ArrayList<ArrayList<Object>> secondHalfRecords = new ArrayList<>(page.getRecords().subList(cutIndex, page.getRecords().size()));
        removePageFromBuffer(table, page);
        pageIDIndex += 1;
        Page firstPage = new Page(table, pageIDIndex, firstHalfRecords);
        addPageToBuffer(firstPage);
        pageIDIndex += 1;
        Page secondPage = new Page(table, pageIDIndex, secondHalfRecords);
        addPageToBuffer(secondPage);
        return firstPage;
    }

    private void removePageFromBuffer(Table table, Page page) {
        table.removePage(page);
        buffer.remove(page);
        File pageFile = new File(pageFolder + "/" + page.getPageId());
        pageFile.delete();
    }
}
