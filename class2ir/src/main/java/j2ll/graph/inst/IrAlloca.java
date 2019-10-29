package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrAlloca extends IrSentence {
    public String irtype;
    public IrVariable left;

    {
        instType = ALLOCA;
        stackSize = 1;
    }


    @Override
    public IrVariable getLeft() {
        return left;
    }

    // %stack1 = alloca i32
    @Override
    public void parse(String st) {
        String[] strs = split(st, "=");
        irtype = split(strs[1], " ")[1];
        left = new IrVariable(irtype, strs[0].trim());
    }

    @Override
    public void replaceVarName(IrVariable old, IrVariable newv) {
        if (left.equals(old)) {
            left = newv;
        }
    }

    @Override
    public String toString() {
        return left.name + " = alloca " + irtype;
    }
}
