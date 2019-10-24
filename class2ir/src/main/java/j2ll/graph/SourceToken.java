package j2ll.graph;

import java.util.ArrayList;
import java.util.List;

public class SourceToken {
    public SourceToken(String[] strs) {
        for (int i = 0; i < strs.length; i++) {
            IrSentence irs = IrSentence.parseInst(strs[i]);
            code.add(irs);
        }
    }

    List<IrSentence> code = new ArrayList<>();
    public int pos;
}
