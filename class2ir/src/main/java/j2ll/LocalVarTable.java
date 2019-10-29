package j2ll;

import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Block of Local Vars (for 1 method)
 */
public final class LocalVarTable {


    List<LocalVar> all = new ArrayList<>(); // a slot maybe 2 or more var in deference bytecode range

    List<String> labelsForParse = new ArrayList<>();
    List<String> labelsForUse = new ArrayList<>();

    public void addVar(LocalVar localVar) {
        all.add(localVar);
    }

    public void addParseLabel(String lab) {
        labelsForParse.add(lab);
    }
    public void addUseLabel(String lab) {
        labelsForUse.add(lab);
    }

    public LocalVar get(int slot,String lab) {

        List<LocalVar> list=getBySlot( slot);
        Collections.sort(list, new Comparator<LocalVar>() {
            @Override
            public int compare(LocalVar o1, LocalVar o2) {
                if(o1.endAt==o2.startAt){
                    throw new RuntimeException("local var sort error,expect not overwrite.");
                }
                return o1.endAt-o2.startAt;
            }
        });

        int labIndex=labelsForUse.indexOf(lab);
        if(labIndex<0){
            throw new RuntimeException("can't found label: "+lab);
        }
        for(LocalVar lv:list){
            if(labIndex<lv.endAt){
                return lv;
            }
        }
        throw new RuntimeException("can't found localvar by slot: "+slot);
    }

    public List<LocalVar> getAll() {
        return all;
    }

    public List<LocalVar> getBySlot(int i) {
        List<LocalVar> list = new ArrayList<>();
        for (LocalVar lv : all) {
            if (lv.slot == i) {
                list.add(lv);
            }
        }
        return list;
    }

    /**
     * same slot would be more than 1 vars ,every var  have a rang in lab1 to lab2
     * so a lab vist , check this lab range var
     */
    public void parseRange(String name, String lab1, String lab2, int slot) {
        for (LocalVar lv : all) {
            if (lv.name.equals(name) && lv.slot == slot) {
                lv.startAt = labelsForParse.indexOf(lab1);
                lv.endAt = labelsForParse.indexOf(lab2);
                lv.name = lv.name + "_" + lv.startAt + "_" + lv.endAt;
            }
        }
    }


}
