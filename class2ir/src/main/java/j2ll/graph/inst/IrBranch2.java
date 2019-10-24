package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrBranch2 extends IrSentence {
    public String lab1;
    public String lab2;
    public String irType;
    public IrVariable operand;


    {
        instType = BR2;
        stackSize = 0;
    }

    // br i1 %__tmpc4, label %L1638435724, label %_if.else4
    @Override
    public void parse(String st) {

        String[] ss1 = split(st,"[ ,]{1,}");
        instName = ss1[0];
        irType = ss1[1];
        operand = new IrVariable(irType, ss1[2]);
        lab1 = ss1[4].replace("%", "");
        lab2 = ss1[6].replace("%", "");
    }

    @Override
    public IrVariable getLeft() {
        return null;
    }

    @Override
    public void replaceVarName(IrVariable old, IrVariable newv) {
        if (operand.equals(old)) {
            operand = newv;
        }
        line = toString();
    }

    @Override
    public String toString() {
        return instName + " " + operand + ", label " + lab1 + ", label " + lab2;
    }

}
