package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrStore extends IrSentence {
    public String operandType1, operandType2;
    public IrVariable operand1, operand2;

    {
        instType = STORE;
        stackSize = -1;
    }


    //store i32 %__tmpv17, i32* %c_3_18
    @Override
    public void parse(String st) {
        String[] ss = split(st, "[ ,]{1,}");
        //[%__tmpc6, icmp, sle, i32, %stack11, %stack12]
        instName = ss[0];
        operandType1 = ss[1];
        operand1 = new IrVariable(operandType1, ss[2]);
        operandType2 = ss[3];
        operand2 = new IrVariable(operandType2, ss[4]);
    }

    @Override
    public IrVariable getLeft() {
        return null;
    }


    @Override
    public void replaceVarName(IrVariable old, IrVariable newv) {
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
        return instName + " " + operand1 + ", " + operand2;
    }
}
