package j2ll.graph;


import j2ll.graph.inst.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrBlock extends IrObject {
    public IrFunction func;
    int index;
    List<IrSentence> lines = new CopyOnWriteArrayList<>();

    //
    List<IrBlock> inner = new ArrayList<>();
    List<IrBlock> outer = new ArrayList<>();

    List<IrVariable> operandNeed = new ArrayList<>();
    List<IrVariable> operandRemain = new ArrayList<>();

    public IrBlock(IrFunction irf) {
        func = irf;
    }


    void iteratorForward(IrBlock block, IrBlock stop, List<IrBlock> curPath, List<List<IrBlock>> allPaths) {

        if (count(curPath, this) > 1) {// while follow stream
            return;
        }
        curPath.add(this);
        if (this == stop) {
            return;
        }
        if (block.outer.isEmpty()) {
            allPaths.add(curPath);
        } else {
            for (IrBlock irb : block.outer) {
                List<IrBlock> list = new ArrayList<>();
                list.addAll(curPath);
                irb.iteratorForward(irb, stop, list, allPaths);
            }
        }
    }

    void iteratorBackward(IrBlock block, IrBlock stop, List<IrBlock> curPath, List<List<IrBlock>> allPaths) {

        if (count(curPath, this) > 1) {// while follow stream
            return;
        }
        curPath.add(this);
        if (this == stop) {
            return;
        }
        if (block.inner.isEmpty()) {
            allPaths.add(curPath);
        } else {
            for (IrBlock irb : block.inner) {
                List<IrBlock> list = new ArrayList<>();
                list.addAll(curPath);
                irb.iteratorBackward(irb, stop, list, allPaths);
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

    void fintOuter() {
        for (IrObject irb : lines) {
            if (irb instanceof IrSentence) {
                IrSentence irs = (IrSentence) irb;
                switch (irs.getInstType()) {
                    case IrSentence.RET: {
                        break;
                    }
                    case IrSentence.BR2: {
                        IrBranch2 ibr2 = (IrBranch2) irs;
                        IrBlock b1 = func.findBlock(ibr2.lab1);
                        if (b1 != null) {
                            outer.add(b1);
                            b1.inner.add(this);
                        }
                        IrBlock b2 = func.findBlock(ibr2.lab2);
                        if (b2 != null) {
                            outer.add(b2);
                            b2.inner.add(this);
                        }
                        break;
                    }
                    case IrSentence.BR1: {
                        IrBranch1 ibr1 = (IrBranch1) irs;
                        IrBlock b1 = func.findBlock(ibr1.label);
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


    /**
     * find this block register(%stack16) used but created by other block
     */
    public void findOperandCreatedByOther() {
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
                        func.stackVars.add(ir.left);
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
        //remove imm operand in lefts
        for (Iterator<IrVariable> it = lefts.iterator(); it.hasNext(); ) {
            IrVariable s = it.next();
            if (s.name == null || !s.name.startsWith("%")) {
                it.remove();
            }
        }
        //remove imm operand in operands
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
        tmp.removeAll(func.stackVars);
        operandNeed.addAll(tmp);
        Collections.reverse(operandNeed);
        if (!tmp.isEmpty() && index != 0) {
            System.out.println("[----]" + func.getFuncDes() + " block " + index + " need :" + Arrays.toString(tmp.toArray(new IrVariable[0])));
        }
        tmp.clear();
        tmp.addAll(lefts);
        tmp.removeAll(operands);
        operandRemain.addAll(tmp);
        Collections.reverse(operandRemain);
        if (!tmp.isEmpty()) {
            System.out.println("[----]" + func.getFuncDes() + " block " + index + " remain :" + Arrays.toString(tmp.toArray(new IrVariable[0])));
        }
    }

    /**
     * repair block need register,
     * resolve: change register var to stack var
     * <p>
     * iterate need , trace pre block 's remain
     */
    void fixOperandNeeds() {
        if (index == 0) return;
        for (IrVariable need : operandNeed) {
            //find all path to define
            if (need2remainPass(need, this)) {
                System.out.println("[----]" + func.getFuncDes() + " : block " + index + ", ignored " + need);
                continue;
            }

            if(need.name.equals("stack139")){
                int debug=1;
            }

            String needStackName = need.name + "_stack";
            IrVariable stackVar = new IrVariable(need.type, needStackName);
            String s = needStackName + " = alloca " + need.type;
            IrSentence alloc = IrSentence.parseInst(s);
            func.first.lines.add(0, alloc);

            for (IrBlock irb : getPreBlock(this)) {  //if pre is branch, need all of pre fix
                List<IrBlock> path = new ArrayList<>();
                path.add(this);
                if (!fixPreOfPre(irb, need, stackVar, path)) {
                    //throw new RuntimeException("can't fix " + need);
                    System.out.println("[WARN] " + func.getFuncDes() + " : block " + index + " " + need + " not fix, come from block " + irb.index);
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
                                String s1 = "store " + need.type + " " + rems.name + ", " + need.type + "* " + stackVar.name +"  ;FIXED BRANCH BLOCK VAR";
                                IrSentence store = IrSentence.parseInst(s1);
                                tmp.lines.add(tmp.lines.indexOf(ti) + 1, store);
                                it.remove();
                                System.out.println("[----]" + func.getFuncDes() + " : block " + path.get(0).index + ", reg fixed :" + need + " to " + stackVar + " ,reverse path:" + Arrays.toString(path.toArray(new IrBlock[0])));
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
        return b.inner;
    }


    boolean need2remainPass(IrVariable need, IrBlock from) {
        for (IrObject b : func.blocks) {
            if (b instanceof IrBlock) {
                IrBlock ib = ((IrBlock) b);
                for (IrVariable var : ib.operandRemain) {
                    if (var.type.equals(need.type) && var.name.equals(need.name)) {
                        if (backwardArrive(from, ib)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * from -> to  ,all path arrived is true, else false
     *
     * @param from
     * @param to
     * @return
     */
    boolean backwardArrive(IrBlock from, IrBlock to) {
        List<List<IrBlock>> all = new ArrayList<>();
        List<IrBlock> curpath = new ArrayList<>();
        iteratorBackward(from, to, curpath, all);
        int count = 0;
        for (List<IrBlock> list : all) {
            if (list.contains(to)) {
                count++;
            }
        }
        if (count == all.size()) {
            return true;
        }
        return false;
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

}
