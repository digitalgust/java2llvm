package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrArith extends IrSentence {
    public IrVariable left;
    public String irtype;
    public IrVariable operand1, operand2;


    {
        instType = ARITH;
        stackSize = -1;
    }


    //%stack8 = add i32 %stack4, %stack7
    @Override
    public void parse(String st) {
        String[] ss = split(st,"[ ,=]{1,}");
        //
        instName = ss[1];
        irtype = ss[2];
        operand1 = new IrVariable(irtype, ss[3]);
        operand2 = new IrVariable(irtype, ss[4]);
        left = new IrVariable(irtype, ss[0]);
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
        if (operand1.equals(old)) {
            operand1 = newv;
        }
        if (operand2.equals(old)) {
            operand2 = newv;
        }
        line = toString();
    }

    @Override
    public String toString() {
        return left.name + " = " + instName + " " + operand1 + ", " + operand2.name;
    }
}
