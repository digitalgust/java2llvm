package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;

public class IrComment extends IrSentence {
    public String comment;

    {
        instType = COMMENT;
        stackSize = 0;
    }


    @Override
    public void parse(String st) {
        comment = st;
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
        return line;
    }
}
