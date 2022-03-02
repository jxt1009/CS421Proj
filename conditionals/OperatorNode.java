package conditionals;

import storagemanager.BufferManager;
import storagemanager.RecordHelper;
import storagemanager.StorageManager;
import common.Attribute;

import java.nio.Buffer;
import java.util.ArrayList;

public class OperatorNode extends Node {

    private final String operator;

    public OperatorNode(Node left, Node right,String operator) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public void setLeft(Node leftNode) {
        this.left = leftNode;
    }

    public void setRight(Node rightNode) {
        this.right = rightNode;
    }

    public ArrayList<ArrayList<Object>> evaluate() {
        ArrayList<ArrayList<Object>> results = new ArrayList<>();
        for (ArrayList<Object> leftRecord : left.evaluate()) {
            Object leftValue;
            if (left instanceof ColumnNode) {
                int columnIndex = ((ColumnNode) left).getColumnIndex();
                leftValue = leftRecord.get(columnIndex);
            } else {
                leftValue = null;
            }
            Object rightValue;
            if (right instanceof ValueNode) {
                rightValue = ((ValueNode) right).getValue();
            } else {
                rightValue = null;
            }
            Attribute columnAttribute = ((ColumnNode) left).getColumnAttribute();
            switch (operator) {
                case ">":
                    if (RecordHelper.greaterThan(leftValue, rightValue, columnAttribute)) {
                        results.add(leftRecord);
                    }
                    break;
                case "<":
                    if (RecordHelper.lessThan(leftValue, rightValue, columnAttribute)) {
                        results.add(leftRecord);
                    }
                    break;
                case "=":
                    if (RecordHelper.equals(leftValue, rightValue, columnAttribute)) {
                        results.add(leftRecord);
                    }
                    break;
            }


        }
        return results;
    }
}
