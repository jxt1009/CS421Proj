package indexing;

import common.RecordPointer;
import storagemanager.RecordHelper;

import java.util.ArrayList;

public class BPlusTree implements IBPlusTree{

    private int pageSize;
    private String columnName;
    // Hold all pages created by this specific tree
    private ArrayList<Integer> treePages = new ArrayList<Integer>();
    // TODO Temp implementation without tree, will need to be changed
    private ArrayList<IndexNode> tree = new ArrayList<IndexNode>();

    public BPlusTree(String column, int pageSize){
        this.columnName = column;
        this.pageSize = pageSize;
        // TODO write first index page to disk if starting from scratch
    }

    public BPlusTree(String column){
        //TODO load in page from disk if only a column name is specified
    }

    @Override
    //TODO create wrapper record pointer object to include search key for tree traversal
    public boolean insertRecordPointer(RecordPointer rp, Object searchKey) {
        //TODO likely needs a recursive function to be called from here
        // to traverse tree and insert based on search key location
        if(tree.isEmpty()){
            return tree.add(new IndexNode(rp, searchKey));
        }else{
            for(IndexNode node : tree){
                if(RecordHelper.compareObjects(node.getSearchKey(),searchKey)){
                    tree.add(tree.indexOf(node),new IndexNode(rp, searchKey));
                }
            }
        }
        return false;
    }

    // TODO will need a page splitting method

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
