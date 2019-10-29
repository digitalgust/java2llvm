package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrBitcast extends IrSentence {
    public IrVariable left;
    public String irtype1;
    public IrVariable operand;
    public String irtype2;


    {
        instType = BITCAST;
        stackSize = 0;
    }


    //  %__cast_0 = bitcast %test_Test* %stack0 to %test_TestParent*
    @Override
    public void parse(String st) {
        String[] strs = split(st, "=");
        String[] ss1 = split(strs[1], "[ ()]{1,}");
        instName = ss1[0];
        irtype1 = ss1[1];
        //3 = to
        irtype2 = ss1[4];
        left = new IrVariable(irtype2, strs[0].trim());
        operand = new IrVariable(irtype1, ss1[2]);
    }

    @Override
    public IrVariable getLeft() {
        return left;
    }

    @Override
    public void replaceVarName(IrVariable old, IrVariable newv) {
        if (left.equals(old)) {
            left = newv;
        }
        if (operand.equals(old)) {
            operand = newv;
        }
        line = toString();
    }

    @Override
    public String toString() {
        return left.name + " = " + instName + " " + operand + " to " + irtype2;
    }
}
