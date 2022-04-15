package indexing;

import common.RecordPointer;

import java.util.ArrayList;

public class BPlusTree implements IBPlusTree{

    private int pageSize;
    private String columnName;
    private ArrayList<RecordPointer> tree;

    public BPlusTree(String column, int pageSize){
        this.columnName = column;
        this.pageSize = pageSize;
        tree = new ArrayList<RecordPointer>();
    }

    @Override
    public boolean insertRecordPointer(RecordPointer rp, Object searchKey) {
        //TODO likely needs a recursive function to be called from here
        // to traverse tree and insert based on search key location
        return false;
    }

    @Override
    public boolean removeRecordPointer(RecordPointer rp, Object searchKey) {
        return false;
    }

    @Override
    public ArrayList<RecordPointer> search(Object searchKey) {
        return null;
    }

    @Override
    public ArrayList<RecordPointer> searchRange(Object searchKey, boolean lessThan, boolean equalTo) {
        return null;
    }
}
