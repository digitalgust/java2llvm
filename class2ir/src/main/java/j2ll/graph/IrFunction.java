package j2ll.graph;

import j2ll.graph.inst.IrLabel;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrFunction {
    List<IrObject> blocks = new CopyOnWriteArrayList<>();
    public String define;
    public String end;

    public String className, methodName, javaSignature;

    IrBlock first = null;

    //save all alloca stack var
    Set<IrVariable> stackVars = new LinkedHashSet<>();//left stack var

    public IrFunction(String className, String methodName, String javaSignature) {
        this.className = className;
        this.methodName = methodName;
        this.javaSignature = javaSignature;
    }

    public void parse(String funcStr) {
        String[] strs = funcStr.split("\n");
        parse(strs);
    }

    public void parse(String[] strs) {
        SourceToken st = new SourceToken(strs);
        st.pos = 0;

        //System.out.println();
        parse(st);
    }


    public void parse(SourceToken st) {
        int id = 0;
        IrBlock irb = null;
        for (st.pos = 0; st.pos < st.code.size(); st.pos++) {
            IrSentence irs = st.code.get(st.pos);
            switch (irs.getInstType()) {
                case IrSentence.LABEL: {
                    blocks.add(irs);
                    irb = new IrBlock(this);
                    blocks.add(irb);
                    irb.index = id;
                    id++;
                    break;
                }
                default: {
                    if (irb == null) throw new RuntimeException("irb can't null");
                    irb.lines.add(irs);
                }
            }
        }


        //find graph path
        for (IrObject b : blocks) {
            if (b instanceof IrBlock) {
                IrBlock ib = ((IrBlock) b);
                if (first == null) first = ib;

                ib.fintOuter();
                ib.findOperandCreatedByOther();
                ib.fixOperandNeeds();

            }
        }

        //for debug, print execute path ,may be lots of path
        if (false) {
            List<List<IrBlock>> paths = new ArrayList<>();
            List<IrBlock> cur = new ArrayList<>();
            first.iteratorForward(first, null, cur, paths);

            for (List<IrBlock> list : paths) {
                for (IrBlock ib : list) {
                    System.out.print(ib + " -> ");
                }
                System.out.println();
            }
        }
    }


    public IrBlock findBlock(String lab) {
        boolean found = false;
        for (IrObject irb : blocks) {
            if (irb instanceof IrSentence) {
                IrSentence irs = (IrSentence) irb;
                if (irs.getInstType() == IrSentence.LABEL) {
                    if (((IrLabel) irs).label.equals(lab)) {
                        found = true;
                        continue;
                    }
                }
            }
            if (found) {
                return (IrBlock) irb;
            }
        }
        return null;
    }

    public String getFuncDes() {
        return className + "." + methodName + javaSignature;
    }

    public String toString() {
        int deep = 0;
        String s = "";

        for (IrObject irb : blocks) {
            s += irb.toString(deep + 1);
        }
        return s;
    }
}
