package conditionals;

import java.util.ArrayList;

public class ValueNode extends Node{

    private final String value;
    public ValueNode(String value){
        this.value = value;
    }

    public Object getValue(){
        return value;
    }

    @Override
    public ArrayList<ArrayList<Object>> evaluate() {
        //not used for value nodes, use getValue()
        return null;
    }
}
