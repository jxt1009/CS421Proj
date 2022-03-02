package conditionals;

import common.Table;

import java.util.ArrayList;
import java.util.Arrays;

public class ConditionalNode extends Node{

    private String conditional;
    private Table table;

    public ConditionalNode(Node left, Node right, String conditional, Table table){
        this.table= table;
        this.conditional = conditional;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<ArrayList<Object>> evaluate() {
        ArrayList<ArrayList<Object>> results = new ArrayList<>();
        ArrayList<ArrayList<Object>> leftResults = left.evaluate();
        ArrayList<ArrayList<Object>> rightResults = right.evaluate();
        if(conditional.equalsIgnoreCase("and")){
            for(ArrayList<Object> leftRecord : leftResults){
                Object leftKey = leftRecord.get(table.getPrimaryKeyIndex());
                for(ArrayList<Object> rightRecord : rightResults){
                    Object rightKey = rightRecord.get(table.getPrimaryKeyIndex());
                    if (leftKey.equals(rightKey)) {
                        results.add(leftRecord);
                        break;
                    }
                }
            }
        }
        if(conditional.equalsIgnoreCase("or")){
            for(ArrayList<Object> leftRecord : leftResults){
                Object leftKey = leftRecord.get(table.getPrimaryKeyIndex());
                boolean found = false;
                for(ArrayList<Object> rightRecord : rightResults){
                    Object rightKey = rightRecord.get(table.getPrimaryKeyIndex());
                    if (leftKey.equals(rightKey)) {
                        found = true;
                        break;
                    }
                }
                if(!found){
                    results.add(leftRecord);
                    results.addAll(rightResults);
                }
            }
        }
        return results;
    }
}
