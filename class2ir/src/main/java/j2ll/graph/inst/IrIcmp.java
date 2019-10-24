package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrIcmp extends IrSentence {
    public IrVariable left;
    public String irtype;
    public IrVariable operand1, operand2;

    {
        instType = ICMP;
        stackSize = -1;
    }

    // %__tmpc6 = icmp sle i32 %stack11, %stack12
    @Override
    public void parse(String st) {
        String[] ss = split(st,"[ ,=]{1,}");
        //[%__tmpc6, icmp, sle, i32, %stack11, %stack12]
        instName = ss[1] + " " + ss[2];
        irtype = ss[3];
        left = new IrVariable(irtype, ss[0]);
        operand1 = new IrVariable(irtype, ss[4]);
        operand2 = new IrVariable(irtype, ss[5]);
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
