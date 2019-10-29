package j2ll.graph.inst;

import j2ll.graph.IrSentence;
import j2ll.graph.IrVariable;
import j2ll.graph.SourceToken;

public class IrLabel extends IrSentence {
    public String label;

    {
        instType = LABEL;
        stackSize = 0;
    }


    @Override
    public void parse(String st) {
        label = st.substring(0, st.indexOf(":")).trim();
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
