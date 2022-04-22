package indexing;

import common.RecordPointer;
import storagemanager.RecordHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class BPlusTree implements IBPlusTree{

    private int pageSize;
    private String columnName;
    // Hold all pages created by this specific tree
    private ArrayList<Integer> treePages = new ArrayList<Integer>();
    // TODO Temp implementation without tree, will need to be changed
    private ArrayList<BPTreeNode> tree = new ArrayList<BPTreeNode>();
    private int max_keys = 4;
    private int min_keys = 1;
    private int split_index = 1;

    private BPTreeNode root;
    private int index;

    public BPlusTree(String column, int pageSize){
        this.columnName = column;
        this.pageSize = pageSize;
        // TODO write first index page to disk if starting from scratch
    }

    public BPlusTree(String column){
        //TODO load in page from disk if only a column name is specified
    }

    public static void printTree(BPlusTree tree){
        if(tree.getRoot() != null){
            BPTreeNode tmp = tree.getRoot();
            while(!tmp.isLeaf()){
                tmp = tmp.getChildren()[0];
            }
            while(tmp != null){
                for(int i = 0; i < tmp.numKeys; i++){
                    System.out.println(tmp.getKeys()[i]);
                }
                tmp = tmp.getNext();
            }
        }
    }

    private BPTreeNode getRoot() {
        return root;
    }

    @Override
    // TODO create wrapper record pointer object to include search key for tree traversal
    public boolean insertRecordPointer(RecordPointer rp, Object searchKey) {
        // TODO likely needs a recursive function to be called from here
        // to traverse tree and insert based on search key location
        if(root == null){
            root = new BPTreeNode(this.index,max_keys);
            this.index++;
            root.getKeys()[0] = searchKey;
            root.numKeys++;
            root.insertRecord(rp, searchKey);
            return true;
        }else {
            return insert(root, rp, searchKey);
        }
    }

    private boolean insert(BPTreeNode root, RecordPointer rp, Object searchKey){
            if(root.isLeaf()){
                boolean hasKey = false;
                for(Object key : root.getKeys()){
                    if(RecordHelper.equals(key,searchKey)){
                        hasKey = true;
                        break;
                    }
                }
                if(!hasKey){
                    Object[] keys = root.getKeys();
                    root.numKeys++;
                    int insertIndex = root.numKeys - 1;
                    while(insertIndex > 0 && RecordHelper.greaterThan(keys[insertIndex-1],searchKey)){
                        keys[insertIndex] = keys[insertIndex-1];
                        insertIndex-=1;
                    }
                    keys[insertIndex] = searchKey;
                    root.insertRecord(rp, searchKey);
                }
                insertRepair(root);
            }else{
                int findIndex = 0;
                while(findIndex < root.numKeys && RecordHelper.lessThanEquals(root.getKeys()[findIndex],searchKey)){
                    findIndex++;
                }
                return insert(root.getChildren()[findIndex],rp, searchKey);
            }
            return true;
    }

    private void insertRepair(BPTreeNode root) {
        if (root.numKeys < this.max_keys)
        {
            return;
        }
        else if (root.getParent() == null)
        {
            this.root = split(root);
            return;
        }
        else
        {
            BPTreeNode newNode  = split(root);
            insertRepair(newNode);
        }
    }

    private BPTreeNode split(BPTreeNode tree) {
        BPTreeNode rightNode = new BPTreeNode(index++,max_keys);
        Object rising = tree.getKeys()[split_index];
        int parentIndex;
        if(tree.getParent() != null){
            BPTreeNode currentParent = tree.getParent();
            for(parentIndex = 0; parentIndex < currentParent.numKeys + 1 && currentParent.getChildren()[parentIndex] != tree; parentIndex++);
            if(parentIndex == currentParent.numKeys + 1){
                System.err.println("Error could not find child");
                return null;
            }
            for(int i = currentParent.numKeys; i > parentIndex; i--){
                currentParent.getChildren()[i+1] = currentParent.getChildren()[i];
                currentParent.getKeys()[i] = currentParent.getKeys()[i-1];
            }
            currentParent.numKeys++;
            currentParent.getKeys()[parentIndex] = rising;
            currentParent.getChildren()[parentIndex + 1] =  rightNode;
            rightNode.setParent(currentParent);
        }

        int rightSplit;

        if(tree.isLeaf()){
            rightSplit = split_index;
            rightNode.setNext(tree.getNext());
            tree.setNext(rightNode);
        }else{
            rightSplit = split_index + 1;
        }
        rightNode.numKeys = tree.numKeys - rightSplit;
        for(int i = rightSplit; i < tree.numKeys; i ++){
            rightNode.getChildren()[i - rightSplit] = tree.getChildren()[i];
            if(tree.getChildren()[i] != null){
                rightNode.setLeaf(false);
                tree.getChildren()[i].setParent(rightNode);
            }
            tree.getChildren()[i] = null;
        }
        for(int i = rightSplit; i < tree.numKeys; i++){
            rightNode.getKeys()[i-rightSplit] = tree.getKeys()[i];
        }
        BPTreeNode leftNode = tree;
        leftNode.numKeys = split_index;
        if(tree.getParent() != null){
            return tree.getParent();
        }else{
            this.root = new BPTreeNode(index++,max_keys);
            this.root.getKeys()[0] = rising;
            this.root.getChildren()[0] = leftNode;
            this.root.getChildren()[1] = rightNode;
            leftNode.setParent(this.root);
            rightNode.setParent(this.root);
            this.root.setLeaf(false);
            return this.root;
        }
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

    public static void main(String[] args){
        BPlusTree tree = new BPlusTree("aa",5);
        tree.insertRecordPointer(new RecordPointer(0,1), "4");
        tree.insertRecordPointer(new RecordPointer(0,1), "5");
        tree.insertRecordPointer(new RecordPointer(0,1), "6");
        tree.insertRecordPointer(new RecordPointer(0,1), "7");
        tree.insertRecordPointer(new RecordPointer(0,1), "8");
        tree.insertRecordPointer(new RecordPointer(0,1), "9");
        tree.insertRecordPointer(new RecordPointer(0,1), "10");
        tree.insertRecordPointer(new RecordPointer(0,1), "11");
        tree.insertRecordPointer(new RecordPointer(0,1), "12");
        tree.insertRecordPointer(new RecordPointer(0,1), "13");
        printTree(tree);
    }
}
