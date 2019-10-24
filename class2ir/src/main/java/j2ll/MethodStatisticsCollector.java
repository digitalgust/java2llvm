package j2ll;

import org.objectweb.asm.*;

/**
 * Vars Collector
 */
public final class MethodStatisticsCollector extends MethodVisitor {

    LocalVarTable vars;
    Resolver resolver;

    public MethodStatisticsCollector(int i, LocalVarTable vars, Resolver resolver) {
        super(i);
        this.vars = vars;
        this.resolver = resolver;
    }

    @Override
    public void visitLocalVariable(String name, String sign, String s2, Label label, Label label1, int slot) {
        vars.addVar(new LocalVar(slot, name, sign));
        Util.javaSignature2irType(this.resolver, sign);
        //System.out.println(" vist localvar:%%" + name + " " + slot + " = " + sign + " from " + label + " to " + label1 + " " + s2);
        vars.parseRange(name, label.toString(), label1.toString(), slot);
    }

    @Override
    public void visitLabel(Label label) {
//        System.out.println(" vist label:" + label + ":");
        vars.addLabel(label.toString());
    }

    @Override
    public void visitVarInsn(int opcode, int slot) {
        switch (opcode) {
            // =============================================== Load ==
            case Opcodes.ILOAD: // 21
//                System.out.println("ILOAD " + slot);
                break;
            case Opcodes.LLOAD: // 22
//                System.out.println("LLOAD " + slot);
                break;
            case Opcodes.FLOAD: // 23
//                System.out.println("FLOAD " + slot);
                break;
            case Opcodes.DLOAD: // 24
//                System.out.println("DLOAD " + slot);
                break;
            case Opcodes.ALOAD: // 25
//                System.out.println("ALOAD " + slot);
                break;
            // =============================================== Store (Store stack into local variable) ==
            case Opcodes.ISTORE: // 54
            case Opcodes.LSTORE: // 55
            case Opcodes.FSTORE: // 56
            case Opcodes.DSTORE: // 57
            case Opcodes.ASTORE: // 58
//                System.out.println("STORE " + slot);
                break;
        }
    }

}
