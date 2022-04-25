package indexing;

import catalog.ACatalog;
import common.Attribute;
import storagemanager.FileManager;
import storagemanager.RecordHelper;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

public class BPTreeNode {
    private final String location = ACatalog.getCatalog().getDbLocation();
    private final File indexPageDir = new File(location + "/index");;
    public int numKeys = 1;
    public Object[] keys;
    private final BPTreeNode[] children;
    private int keySize;
    private BPTreeNode parent = null;
    private BPTreeNode next;
    private boolean isLeaf = true;
    private final int pageIndex;
    private Attribute columnAttribute;

    public BPTreeNode(int pageIndex, int keySize, Attribute attribute){
        // TODO need to write out to hardware and update each time anything is changed.
        // Function will need to be outside of constructor and called for every set operation
        this.pageIndex = pageIndex;
        this.keySize = keySize;
        this.columnAttribute = attribute;
        keys = new Object[keySize+1];
        children = new BPTreeNode[keySize+1];
        if (!indexPageDir.exists()) {
            indexPageDir.mkdirs();
        }
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public int getPageIndex(){
        return pageIndex;
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
        if(this.next!=null) {
            BPTreeNode next = readFromDisk(this.next);
        }else{
        }
        return this.next;
    }

    private BPTreeNode readFromDisk(BPTreeNode next) {
        File finalPageFile = new File(indexPageDir.getPath() + "/" + next.getPageIndex());
        try {
            FileInputStream fileInputStream = new FileInputStream(finalPageFile);
            DataInputStream inputStream = new DataInputStream(fileInputStream);
            int pageIndex = inputStream.readInt();
            int keySize = inputStream.readInt();
            Object[] keys = new Object[keySize];
            for (int i = 0 ; i < numKeys; i ++) {
                    if (columnAttribute.getAttributeType().equalsIgnoreCase("integer")) {
                        keys[i] = inputStream.readInt();
                    } else if (columnAttribute.getAttributeType().equalsIgnoreCase("double")) {
                        keys[i] = inputStream.readDouble();

                } else if (columnAttribute.getAttributeType().equalsIgnoreCase("boolean")) {
                        keys[i] = inputStream.readBoolean();
                } else if (columnAttribute.getAttributeType().toLowerCase().contains("char")) {
                        keys[i] = FileManager.readChars(inputStream);
                }
            }
            int nextPage = inputStream.readInt();
            System.out.println(pageIndex + " " + Arrays.toString(keys) + " " + nextPage);
            // read in next value
            inputStream.close();
            fileInputStream.close();
            BPTreeNode newNode = new BPTreeNode(pageIndex,this.keySize,columnAttribute);
            return newNode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean writeToDisk(){
        File finalPageFile = new File(indexPageDir.getPath() + "/" + getPageIndex());
        try {
            if (!finalPageFile.exists()) {
                finalPageFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(finalPageFile);
            DataOutputStream outputStream = new DataOutputStream(fileOutputStream);
            outputStream.writeInt(getPageIndex());
            outputStream.writeInt(numKeys);
            for (int i = 0 ; i < numKeys; i ++) {
                Object key = keys[i];
                if (RecordHelper.isNumeric(key)) {
                    if (RecordHelper.returnNumeric(key) instanceof Integer) {
                        outputStream.writeInt((Integer) key);
                    } else if (RecordHelper.returnNumeric(key) instanceof Double) {
                        outputStream.writeDouble((Double) key);
                    }
                } else if (key instanceof Boolean outputBoolean) {
                    outputStream.writeBoolean(outputBoolean);
                } else if (key instanceof String outputString) {
                    FileManager.writeChars(outputString, outputStream);
                } else if (key instanceof Character outputString) {
                    FileManager.writeChars(String.valueOf(outputString), outputStream);
                }
            }
            if (next != null){
                outputStream.writeInt(next.getPageIndex());
        }else{
                outputStream.writeInt(-1);
        }
            outputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public void setNext(BPTreeNode next){
        if(this.next != null){
            new File(indexPageDir.getPath() + "/" + this.next.getPageIndex()).delete();
        }
        this.next = next;
    }

    public void setLeaf(boolean leaf) {
        this.isLeaf = leaf;
    }
}
