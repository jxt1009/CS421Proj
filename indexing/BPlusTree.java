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
    private ArrayList<BPTreeNode> tree = new ArrayList<BPTreeNode>();
    private int max_keys = 6;
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
    // TODO need a function to call after each action to write page to disk

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
                    int insertIndex = root.numKeys-1;
                    while(insertIndex > 0 && RecordHelper.greaterThan(keys[insertIndex-1],searchKey)){
                        keys[insertIndex] = keys[insertIndex-1];
                        insertIndex--;
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
        // TODO add something for splitting files on hardware
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
        for(int i = rightSplit; i < tree.numKeys + 1; i ++){
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

    @Override
    public boolean removeRecordPointer(RecordPointer rp, Object searchKey) {
        return deleteElement(this.root,searchKey);
    }

    private boolean deleteElement(BPTreeNode root, Object searchKey) {
        if(root != null){
            int i;
            for(i = 0; i < root.numKeys && RecordHelper.lessThan(root.keys[i],searchKey); i++);
            if(i == root.numKeys){
                if(!root.isLeaf()){
                    return deleteElement(root.getChildren()[root.numKeys],searchKey);
                }
            }else if(!root.isLeaf() && RecordHelper.equals(root.keys[i], searchKey)){
                return deleteElement(root.getChildren()[i+1],searchKey);
            }else if(!root.isLeaf()){
                return deleteElement(root.getChildren()[i],searchKey);
            }else if(root.isLeaf() && RecordHelper.equals(root.keys[i], searchKey)){
                for(int j = i; j < root.numKeys-1;j++){
                    root.keys[j] = root.keys[j+1];
                }
                root.numKeys--;

            }
            return repairAfterDelete(root);
        }
        return false;
    }

    public BPTreeNode mergeRight(BPTreeNode tree){
        BPTreeNode parentNode = tree.getParent();
        int parentIndex = 0;
        for(parentIndex= 0; parentNode.getChildren()[parentIndex] != tree; parentIndex++);
        BPTreeNode rightSib = parentNode.getChildren()[parentIndex + 1];
        int fromParentIndex = tree.numKeys;
        for(int i = 0; i < rightSib.numKeys;i++){
            int insertIndex = tree.numKeys + 1 + i;
            if(tree.isLeaf()){
                insertIndex -= 1;
            }
            tree.keys[insertIndex] = rightSib.keys[i];
        }
        if(!tree.isLeaf()){
                for(int i = 0; i <= rightSib.numKeys; i++){
                    tree.getChildren()[tree.numKeys+1+i] = rightSib.getChildren()[i];
                    tree.getChildren()[tree.numKeys+1+i].setParent(tree);
                }
                tree.numKeys = tree.numKeys + rightSib.numKeys + 1;
            }else{
                tree.numKeys = tree.numKeys + rightSib.numKeys;
                tree.setNext(rightSib.getNext());
            }

        for(int i = parentIndex + 1; i < parentNode.numKeys;i++){
            parentNode.getChildren()[i] = parentNode.getChildren()[i+1];
            parentNode.getKeys()[i-1] = parentNode.getKeys()[i];
        }
        parentNode.numKeys--;
        return tree;
    }

    private BPTreeNode stealFromRight(BPTreeNode tree, int parentIndex){
        BPTreeNode parentNode = tree.getParent();
        BPTreeNode rightSib = parentNode.getChildren()[parentIndex];
        tree.numKeys++;
        if(tree.isLeaf()){
            tree.keys[tree.numKeys-1] = rightSib.keys[0];
            parentNode.keys[parentIndex] = rightSib.keys[1];
        }else{
            tree.keys[tree.numKeys-1] = parentNode.keys[parentIndex];
            parentNode.keys[parentIndex] = rightSib.keys[0];
        }
        if(!tree.isLeaf()){
            tree.getChildren()[tree.numKeys] = rightSib.getChildren()[0];
            tree.getChildren()[tree.numKeys].setParent(tree);
            for(int i = 1; i < rightSib.numKeys + 1; i++){
                rightSib.getChildren()[i-1] = rightSib.getChildren()[i];
            }
        }
        for(int i = 1; i < rightSib.numKeys; i ++){
            rightSib.keys[i-1] = rightSib.keys[i];
        }
        rightSib.numKeys--;
        return tree;
    }

    private BPTreeNode stealFromLeft(BPTreeNode tree, int parentIndex){
        BPTreeNode parentNode = tree.getParent();
        tree.numKeys++;
        for(int i = tree.numKeys - 1; i > 0; i --){
            tree.keys[i] = tree.keys[i-1];
        }
        BPTreeNode leftSib = parentNode.getChildren()[parentIndex-1];
        if(tree.isLeaf()){
            tree.keys[0] = leftSib.keys[leftSib.numKeys-1];
            parentNode.keys[parentIndex-1] = parentNode.keys[leftSib.numKeys-1];
        }else{
            tree.keys[0] = parentNode.keys[leftSib.numKeys-1];
            parentNode.keys[parentIndex-1] = leftSib.keys[leftSib.numKeys-1];
        }
        if(!tree.isLeaf()){
            for(int i = tree.numKeys; i > 0; i--){
                tree.getChildren()[i] = tree.getChildren()[i-1];
            }
            tree.getChildren()[0] = leftSib.getChildren()[leftSib.numKeys];
            leftSib.getChildren()[leftSib.numKeys] = null;
            tree.getChildren()[0].setParent(tree);
        }
        leftSib.numKeys--;
        return tree;
    }


    private boolean repairAfterDelete(BPTreeNode tree) {
        if(tree.numKeys < this.min_keys){
            if(tree.getParent() == null){
                if(tree.numKeys == 0){
                    this.root = tree.getChildren()[0];
                    if(this.root != null){
                        this.root.setParent(null);
                    }
                }
            }else{
                BPTreeNode parentNode = tree.getParent();
                int parentIndex;
                for(parentIndex = 0; parentNode.getChildren()[parentIndex] != tree; parentIndex++);

                if(parentIndex > 0 && parentNode.getChildren()[parentIndex-1].numKeys + tree.numKeys <= this.max_keys){
                    BPTreeNode nextNode = mergeRight(parentNode.getChildren()[parentIndex-1]);
                    return repairAfterDelete(nextNode.getParent());
                }else if(parentIndex < parentNode.numKeys && parentNode.getChildren()[parentIndex+1].numKeys + tree.numKeys <= this.max_keys){
                    BPTreeNode nextNode = mergeRight(tree);
                    return repairAfterDelete(nextNode.getParent());
                }else if(parentIndex > 0 && parentNode.getChildren()[parentIndex-1].numKeys > this.min_keys){
                    stealFromLeft(tree,parentIndex);
                }else if(parentIndex < parentNode.numKeys && parentNode.getChildren()[parentIndex+1].numKeys > this.min_keys){
                    stealFromRight(tree,parentIndex);
                }else if(parentIndex == 0){
                    BPTreeNode nextNode = this.mergeRight(tree);
                    return repairAfterDelete(nextNode.getParent());
                }else{
                    BPTreeNode nextNode = this.mergeRight(parentNode.getChildren()[parentIndex-1]);
                    return repairAfterDelete(nextNode.getParent());
                }
            }
        }
        return true;
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
        tree.insertRecordPointer(new RecordPointer(0,1), 5);
        tree.insertRecordPointer(new RecordPointer(0,1), 6);
        tree.insertRecordPointer(new RecordPointer(0,1), 7);
        tree.insertRecordPointer(new RecordPointer(0,1), 8);
        tree.insertRecordPointer(new RecordPointer(0,1), 9);
        tree.insertRecordPointer(new RecordPointer(0,1), 0);
        tree.insertRecordPointer(new RecordPointer(0,1), 1);
        tree.insertRecordPointer(new RecordPointer(0,1), 2);
        tree.insertRecordPointer(new RecordPointer(0,1), 3);
        tree.insertRecordPointer(new RecordPointer(0,1), 4);
        tree.insertRecordPointer(new RecordPointer(0,1), 5);
        printTree(tree);
        System.out.println(tree.removeRecordPointer(new RecordPointer(0,1), 5));
        System.out.println(tree.removeRecordPointer(new RecordPointer(0,1), 6));
        System.out.println(tree.removeRecordPointer(new RecordPointer(0,1), 5));
        printTree(tree);
    }
}
