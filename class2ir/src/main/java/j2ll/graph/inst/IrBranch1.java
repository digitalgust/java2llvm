package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrBranch1 extends IrSentence {
    public String label;

    {
        instType = BR1;
        stackSize = 0;
    }

    // br label %L1895143699
    public void parse(String st) {

        String[] ss = split(st,"%");
        instName = ss[0].trim();

        label = ss[1].trim();
    }

    @Override
    public IrVariable getLeft() {
        return null;
    }

    @Override
    public void replaceVarName(IrVariable old, IrVariable newv) {
        line = toString();
    }

    @Override
    public String toString() {
        return "br label %" + label;
    }
}
