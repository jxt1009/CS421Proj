package storagemanager;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BufferManager {

    private FileOutputStream fileOutputStream;
    private ArrayList<HashMap> buffer = new ArrayList<HashMap>();

    public HashMap<String,ArrayList<ArrayList<Object>>> findPage(Object primaryKey){
        return null;
    }

    public HashMap<String,ArrayList<ArrayList<Object>>> loadAllPages(){
        return null;
    }

    public void clearPageBuffer(){
        // clear page buffer
    }

    public void writePageToDisk(){

    }
}
