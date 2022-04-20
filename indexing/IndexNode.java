package indexing;

import common.RecordPointer;

import java.lang.reflect.RecordComponent;

public class IndexNode {

    private Object searchKey;
    private RecordPointer rp;

    public IndexNode(RecordPointer rp, Object searchKey){
        this.rp = rp;
        this.searchKey = searchKey;
    }

    public Object getSearchKey(){
        return searchKey;
    }

    public RecordPointer getRecordPointer(){
        return rp;
    }
}
