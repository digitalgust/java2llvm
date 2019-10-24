package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrCall extends IrSentence {
    public IrVariable left;
    public String irType;
    public String funcName;
    public String[] argsType;
    public IrVariable[] argsName;

    {
        instType = CALL;
        stackSize = 0;
    }


    @Override
    public int getStackSize() {
        return 0;
    }

    //    %stack5 = call i32 @java_lang_String_indexOf_Ljava_lang_String_I(%java_lang_String* %stack0, %java_lang_String* %stack1, i32 %stack2)
    //call  void @java_io_PrintStream_println_I(%java_io_PrintStream* %stack18, i32 %stack20)
    @Override
    public void parse(String p) {
        String st = p;
        String vn = "";
        if (st.indexOf("=") > 0) {
            String[] ss = split(st,"=");
            vn = ss[0].trim();
            st = ss[1];
        } else {
            vn = "";
        }
        String[] ss = split(st,"[ ,()]{1,}");
        //[call, void, @java_io_PrintStream_println_I, %java_io_PrintStream*, %stack18, i32, %stack20]
        instName = ss[0];
        irType = ss[1];
        funcName = ss[2];
        int base = 3;
        int argCount = (ss.length - base) / 2;
        argsType = new String[argCount];
        argsName = new IrVariable[argCount];
        for (int i = 0; i < argCount; i++) {
            argsType[i] = ss[base + i * 2];
            argsName[i] = new IrVariable(argsType[i], ss[base + i * 2 + 1]);
        }
        left = new IrVariable(irType, vn);
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
        s += " = " + irType + " " + instName + " (";
        for (int i = 0; i < argsName.length; i++) {
            IrVariable irv = argsName[i];
            s += irv;
            if (i != argsName.length - 1) {
                s += ", ";
            }
        }
        s += ")";
        return s;
    }
}
