package conditionals;

import storagemanager.RecordHelper;

import java.util.ArrayList;

public class ValueNode extends Node{

    private final Object value;
    public ValueNode(String value){
        Object isNumeric = RecordHelper.returnNumeric(value);
        if(!(isNumeric instanceof Boolean && !(boolean)isNumeric)){
            this.value = isNumeric;
            System.out.println(isNumeric);
        }else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            this.value = Boolean.parseBoolean(value);
        }else{
            this.value = value;
        }
    }

    public Object getValue(){
        return value;
    }

    @Override
    public ArrayList<ArrayList<Object>> evaluate() {
        //not used for value nodes, use getValue()
        return null;
    }

    public String toString(){
        return "Value " + value;
    }
}
