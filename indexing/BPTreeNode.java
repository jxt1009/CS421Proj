package indexing;

import common.RecordPointer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

public class BPTreeNode {

    private HashMap<Object, Object> records = new HashMap<>();
    public int numKeys = 1;
    public Object[] keys;
    private BPTreeNode[] children;
    private BPTreeNode parent = null;
    private BPTreeNode next = null;
    private boolean isLeaf = true;
    private int pageIndex;

    public BPTreeNode(int pageIndex, int keySize){
        this.pageIndex = pageIndex;
        keys = new Object[keySize+1];
        children = new BPTreeNode[keySize+1];
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public HashMap<Object, Object> getRecords() {
        return records;
    }
    public int getPageIndex(){
        return pageIndex;
    }

    public void insertRecord(RecordPointer rp, Object searchKey) {
        this.records.put(searchKey,rp);
    }

    public Object[] getKeys() {
        return keys;
    }

    public BPTreeNode[] getChildren() {
        return children;
    }

    public BPTreeNode getParent() {
        return parent;
    }

    public void setParent(BPTreeNode parent) {
        this.parent = parent;
    }

    public BPTreeNode getNext() {
        return next;
    }

    public void setNext(BPTreeNode next){
        this.next = next;
    }

    public void setLeaf(boolean leaf) {
        this.isLeaf = leaf;
    }
}
