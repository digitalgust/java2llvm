package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrGetptr extends IrSentence {
    public IrVariable left;
    public String irType;
    public String operandType;
    public IrVariable operandName;
    public String[] argsType;
    public IrVariable[] argsName;

    {
        instType = GETPTR;
        stackSize = 0;
    }


    //%__tmp15 = getelementptr %test_Test , %test_Test* %stack19, i32 0, i32 3
    @Override
    public void parse(String st) {
        String[] ss = split(st,"[ ,=()]{1,}");
        //[%__tmp15, getelementptr, %test_Test, %test_Test*, %stack19, i32, 0, i32, 3]
        instName = ss[1];
        irType = ss[2];
        left = new IrVariable(irType, ss[0]);
        operandType = ss[3];
        operandName = new IrVariable(operandType, ss[4]);
        int base = 5;
        int argCount = (ss.length - base) / 2;
        argsType = new String[argCount];
        argsName = new IrVariable[argCount];
        for (int i = 0; i < argCount; i++) {
            argsType[i] = ss[base + i * 2];
            argsName[i] = new IrVariable(argsType[i], ss[base + i * 2 + 1]);
        }
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
        for (int i = 0; i < argsName.length; i++) {
            IrVariable irv = argsName[i];
            if (irv.equals(old)) {
                argsName[i] = newv;
            }
        }
        line = toString();
    }

    @Override
    public String toString() {
        String s = "";
        if (left.name.length() > 0) s += left.name;
        s += " = " + instName + " " + irType + ", " + operandName + ", ";
        for (int i = 0; i < argsName.length; i++) {
            IrVariable irv = argsName[i];
            s += irv;
            if (i != argsName.length - 1) {
                s += ", ";
            }
        }
        return s;
    }
}
