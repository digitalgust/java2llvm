package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrRet extends IrSentence {
    public String operandType;
    public IrVariable operand;

    {
        instType = RET;
        stackSize = 0;
    }


    //ret i32 %stack5
    //ret void
    @Override
    public void parse(String st) {
        String[] ss = split(st,"[ ]{1,}");
        //
        instName = ss[0];
        operandType = ss[1];
        operand = new IrVariable(operandType, ss.length > 2 ? ss[2] : "");
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
        return instName + " " + operand;
    }
}
