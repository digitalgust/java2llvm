package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;
import j2ll.graph.SourceToken;

public class IrLoad extends IrSentence {
    public String irtype, operandType;
    public IrVariable left, operand;

    {
        instType = LOAD;
        stackSize = 1;
    }


    //%__tmpv16 = load i32, i32* %c_3_18
    @Override
    public void parse(String st) {
        String[] ss = split(st,"[ ,=]{1,}");
        //
        instName = ss[1];
        irtype = ss[2];
        left = new IrVariable(irtype, ss[0]);
        operandType = ss[3];
        operand = new IrVariable(operandType, ss[4]);
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
        return left.name + " = " + instName + " " + irtype + ", " + operand;
    }
}
