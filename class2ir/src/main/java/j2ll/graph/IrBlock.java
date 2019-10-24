package j2ll.graph;


import j2ll.graph.inst.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrBlock extends IrObject {
    int index;
    IrBlock first = null;
    List<IrObject> lines = new CopyOnWriteArrayList<>();
    //
    List<IrBlock> inner = new ArrayList<>();
    List<IrBlock> outer = new ArrayList<>();

    //lefts
    List<IrVariable> operandNeed = new ArrayList<>();
    List<IrVariable> operandRemain = new ArrayList<>();
    Set<IrVariable> stackVars = new LinkedHashSet<>();//left stack var


    public IrBlock() {
    }

    public void parse(SourceToken st) {
        int id = 0;
        IrBlock irb = null;
        for (st.pos = 0; st.pos < st.code.size(); st.pos++) {
            IrSentence irs = st.code.get(st.pos);
            switch (irs.getInstType()) {
                case IrSentence.LABEL: {
                    lines.add(irs);
                    irb = new IrBlock();
                    lines.add(irb);
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
        for (IrObject b : lines) {
            if (b instanceof IrBlock) {
                IrBlock ib = ((IrBlock) b);
                if (first == null) first = ib;

                ib.fintOuter(this);
                ib.findOperandCreatedByOther(this);
                ib.fixOperandNeeds(this);

            }
        }

        //for debug, print execute path ,may be lots of path
        if (false) {
            List<List<IrBlock>> paths = new ArrayList<>();
            List<IrBlock> cur = new ArrayList<>();
            iteratorTree(first, cur, paths);

            for (List<IrBlock> list : paths) {
                for (IrBlock ib : list) {
                    System.out.print(ib + " -> ");
                }
                System.out.println();
            }
        }
    }

    void iteratorTree(IrBlock block, List<IrBlock> cur, List<List<IrBlock>> paths) {

        if (count(cur, this) > 1) {// while follow stream
            return;
        }
        cur.add(this);
        if (block.outer.isEmpty()) {
            paths.add(cur);
        } else {
            for (IrBlock irb : block.outer) {
                List<IrBlock> list = new ArrayList<>();
                list.addAll(cur);
                irb.iteratorTree(irb, list, paths);
            }
        }
    }

    int count(List<IrBlock> cur, IrBlock irb) {
        int c = 0;
        for (IrBlock b : cur) {
            if (irb == b) {
                c++;
            }
        }
        return c;
    }

    void fintOuter(IrBlock top) {
        for (IrObject irb : lines) {
            if (irb instanceof IrSentence) {
                IrSentence irs = (IrSentence) irb;
                switch (irs.getInstType()) {
                    case IrSentence.RET: {
                        break;
                    }
                    case IrSentence.BR2: {
                        IrBranch2 ibr2 = (IrBranch2) irs;
                        IrBlock b1 = findBlock(top, ibr2.lab1);
                        if (b1 != null) {
                            outer.add(b1);
                            b1.inner.add(this);
                        }
                        IrBlock b2 = findBlock(top, ibr2.lab2);
                        if (b2 != null) {
                            outer.add(b2);
                            b2.inner.add(this);
                        }
                        break;
                    }
                    case IrSentence.BR1: {
                        IrBranch1 ibr1 = (IrBranch1) irs;
                        IrBlock b1 = findBlock(top, ibr1.label);
                        if (b1 != null) {
                            outer.add(b1);
                            b1.inner.add(this);
                        }

                        break;
                    }
                    default: {
                    }
                }
            }
        }
    }

    IrBlock findBlock(IrBlock top, String lab) {
        boolean found = false;
        for (IrObject irb : top.lines) {
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

    public String toString() {
        return "" + index;
    }

    public String toString(int deep) {
        String s = "";

        for (IrObject irb : lines) {
            s += irb.toString(deep + 1);
        }
        return ";" + index + "\n" + s;
    }


    public void findOperandCreatedByOther(IrBlock top) {
        Set<IrVariable> lefts = new LinkedHashSet<>();//left register var

        Set<IrVariable> operands = new LinkedHashSet<>();//right register var

        for (IrObject irb : lines) {
            if (irb instanceof IrSentence) {
                IrSentence irs = (IrSentence) irb;
                switch (irs.getInstType()) {
                    case IrSentence.LOAD: {
                        IrLoad ir = (IrLoad) irs;
                        lefts.add(ir.left);
                        operands.add(ir.operand);
                        break;
                    }
                    case IrSentence.STORE: {
                        IrStore ir = (IrStore) irs;
                        operands.add(ir.operand1);
                        operands.add(ir.operand2);
                        break;
                    }
                    case IrSentence.GETPTR: {
                        IrGetptr ir = (IrGetptr) irs;
                        lefts.add(ir.left);
                        operands.add(ir.operandName);
                        for (int i = 0; i < ir.argsName.length; i++) {
                            operands.add(ir.argsName[i]);
                        }
                        break;
                    }
                    case IrSentence.BR2: {
                        IrBranch2 ir = (IrBranch2) irs;
                        operands.add(ir.operand);
                        break;
                    }
                    case IrSentence.BR1: {
                        IrBranch1 ir = (IrBranch1) irs;
                        break;
                    }

                    case IrSentence.LABEL: {
                        break;
                    }
                    case IrSentence.ALLOCA: {
                        IrAlloca ir = (IrAlloca) irs;
                        top.stackVars.add(ir.left);
                        break;
                    }
                    case IrSentence.ICMP: {
                        IrIcmp ir = (IrIcmp) irs;
                        lefts.add(ir.left);
                        operands.add(ir.operand1);
                        operands.add(ir.operand2);
                        break;
                    }
                    case IrSentence.COMMENT: {
                        break;
                    }
                    case IrSentence.RET: {
                        IrRet ir = (IrRet) irs;
                        operands.add(ir.operand);
                        break;
                    }
                    case IrSentence.CALL: {
                        IrCall ir = (IrCall) irs;
                        if (ir.left != null) lefts.add(ir.left);
                        for (int i = 0; i < ir.argsName.length; i++) {
                            operands.add(ir.argsName[i]);
                        }
                        break;
                    }
                    case IrSentence.BITCAST: {
                        IrBitcast ir = (IrBitcast) irs;
                        lefts.add(ir.left);
                        operands.add(ir.operand);
                        break;
                    }
                    case IrSentence.ARITH: {
                        IrArith ir = (IrArith) irs;
                        lefts.add(ir.left);
                        operands.add(ir.operand1);
                        operands.add(ir.operand2);
                        break;
                    }

                    default: {
                        System.out.println("not found register name" + irs.line);
                    }
                }
            }
        }
        //remove imm operand
        for (Iterator<IrVariable> it = lefts.iterator(); it.hasNext(); ) {
            IrVariable s = it.next();
            if (s.name == null || !s.name.startsWith("%")) {
                it.remove();
            }
        }

        for (Iterator<IrVariable> it = operands.iterator(); it.hasNext(); ) {
            IrVariable s = it.next();
            if (s.name == null || !s.name.startsWith("%")) {
                it.remove();
            }
        }

        //
        Set<IrVariable> tmp = new LinkedHashSet<>();
        tmp.addAll(operands);
        tmp.removeAll(lefts);
        tmp.removeAll(top.stackVars);
        operandNeed.addAll(tmp);
        Collections.reverse(operandNeed);
        if (!tmp.isEmpty()) {
            //System.out.println("; " + index + " need :" + Arrays.toString(tmp.toArray(new IrVariable[0])));
        }
        tmp.clear();
        tmp.addAll(lefts);
        tmp.removeAll(operands);
        operandRemain.addAll(tmp);
        Collections.reverse(operandRemain);
        if (!tmp.isEmpty()) {
            //System.out.println("; " + index + " remain :" + Arrays.toString(tmp.toArray(new IrVariable[0])));
        }
    }

    /**
     * repair block need register,
     * resolve: change register var to stack var
     * <p>
     * iterate need , trace pre block 's remain
     */
    void fixOperandNeeds(IrBlock top) {
        if (index == 0) return;
        for (IrVariable need : operandNeed) {
            String needStackName = need.name + "_stack";
            IrVariable stackVar = new IrVariable(need.type, needStackName);
            String s = needStackName + " = alloca " + need.type;
            IrSentence alloc = IrSentence.parseInst(s);
            top.first.lines.add(0, alloc);

            for (IrBlock irb : getPreBlock(this)) {  //if pre is branch, need all of pre fix
                List<IrBlock> path = new ArrayList<>();
                if (!fixPreOfPre(irb, need, stackVar, path)) {
                    //throw new RuntimeException("can't fix " + need);
                }
            }

            //load varname
            String needRegisterName = need.name + "_register";
            String tms = needRegisterName + " = load " + need.type + ", " + need.type + "* " + needStackName;
            IrVariable registerVar = new IrVariable(need.type, needRegisterName);
            IrSentence load = IrSentence.parseInst(tms);
            lines.add(0, load);

            //replace varname
            for (IrObject ti : lines) {
                if (ti instanceof IrSentence) {
                    IrSentence irs = ((IrSentence) ti);
                    irs.replaceVarName(need, registerVar);
                }
            }
        }
    }

    boolean fixPreOfPre(IrBlock tmp, IrVariable need, IrVariable stackVar, List<IrBlock> path) {
        path.add(tmp);
        if (count(path, tmp) > 1) return false;
        if (tmp.operandRemain.size() > 0) {
            for (Iterator<IrVariable> it = tmp.operandRemain.iterator(); it.hasNext(); ) {
                IrVariable rems = it.next();
                if (rems.type.equals(need.type)) {// match ed
                    for (IrObject ti : tmp.lines) {
                        if (ti instanceof IrSentence) {
                            IrSentence tis = (IrSentence) ti;
                            if (tis.getLeft() == rems) {
                                String s1 = "store " + need.type + " " + rems.name + ", " + need.type + "* " + stackVar.name;
                                IrSentence store = IrSentence.parseInst(s1);
                                tmp.lines.add(tmp.lines.indexOf(ti) + 1, store);
                                it.remove();
                                System.out.println("regvar fixed from " + need + " to " + stackVar);
                                return true;
                            }
                        }
                    }
                }
            }

        }
        for (IrBlock irb : tmp.inner) {
            if (fixPreOfPre(irb, need, stackVar, path)) {
                return true;
            }
        }
        return false;
    }

    List<IrBlock> getPreBlock(IrBlock b) {
        return inner;
    }
}
