package j2ll;

import org.objectweb.asm.*;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.*;

import static j2ll.Internals.*;

/**
 *
 */
public class MV extends MethodVisitor {

    // parent
    private CV cv;

    // state
    String methodName;
    String javaSignature;
    public int access;

    // arguments
    List<String> _argTypes;
    // res
    String _resType;

    // local vars
    LocalVarTable vars;
    // buffer
    IRBuilder out = new IRBuilder();
    // stack
    //RuntimeStack stack = new RuntimeStack();
    Stack<String> commands = new Stack<>();
    // labels
    List<String> labels = new ArrayList<>();
    List<String> usedLabels = new ArrayList<>();
    String curLabel;


    int max_local;
    int max_stack;
    int tmp;

    public MV(int access, String methodName, String javaSignature, CV cv) {
        super(Opcodes.ASM5);
        this.access = access;
        this.methodName = methodName;
        this.javaSignature = javaSignature;
        this.vars = cv.getStatistics().get(methodName + javaSignature);
        this.cv = cv;

        if(methodName.equals("main")){
            int debug=1;
        }

        // signature
        JSignature s = new JSignature(cv.getStatistics().getResolver(), this.javaSignature);
        _argTypes = s.getArgs();
        _resType = s.getResult();
        // constructor`s implicit parameter
        if ((access & Modifier.STATIC) == 0) {  //non static method add this at first
            this._argTypes.add(0, Util.class2irType(this.cv.getStatistics().getResolver(), cv.className));
        }
        if (isNative()) {
            String tmps = s.getSignatureDeclare(cv.className, methodName, null);
            this.cv.declares.add(tmps);
        }
    }

    public MV(int i, MethodVisitor methodVisitor) {
        super(i, methodVisitor);
    }


    boolean isNative() {
        return (access & Modifier.NATIVE) != 0;
    }

    boolean isStatic() {
        return (access & Modifier.STATIC) != 0;
    }

    @Override
    public void visitParameter(String s, int i) {
        //System.out.println("visitParameter " + s + " " + i);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        //System.out.println("visitAnnotationDefault");
        return null;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return super.visitAnnotation(s, b);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int i, TypePath typePath, String s, boolean b) {
        return super.visitTypeAnnotation(i, typePath, s, b);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b) {
        return super.visitParameterAnnotation(i, s, b);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        //System.out.println("visitAttribute " + attribute);
    }

    @Override
    public void visitCode() {
        out.add("__MethodEntry:");
        out.add("%func_stack = alloca %struct.StackFrame*");
        out.add("%thrd_stack_reg = load %struct.StackFrame*, %struct.StackFrame** @pthd_stack");
        out.add("store %struct.StackFrame* %thrd_stack_reg, %struct.StackFrame** %func_stack");
        out.add("%local_var = call %union.StackEntry* @get_sp(%struct.StackFrame* %thrd_stack_reg)");
        int para_this = 1;
        if (isStatic()) {
            para_this = 0;
        }
        out.add("call void @chg_sp(%struct.StackFrame* %thrd_stack_reg, i32 " + (vars.getMaxSlot() + para_this) + ")");

        if(methodName.equals("main")){
            int debug=1;
        }

        int slotCount = 0;
        for (int i = 0; i < this._argTypes.size(); i++, slotCount++) {
            String argType = this._argTypes.get(i);
            out.genSetLocalVarCode(argType, slotCount, " %s" + slotCount);
            if (argType.equals(LONG) || argType.equals(DOUBLE)) {
                slotCount += 1;
            }
        }

    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stackitems) {
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.NOP: // 0
                break;
            // =============================================== Constants ==
            case Opcodes.ACONST_NULL: // 1
                out.genPushCode(POINTER, "null");
                break;
            case Opcodes.ICONST_M1: // 2
            case Opcodes.ICONST_0: // 3
            case Opcodes.ICONST_1: // 4
            case Opcodes.ICONST_2: // 5
            case Opcodes.ICONST_3: // 6
            case Opcodes.ICONST_4: // 7
            case Opcodes.ICONST_5: // 8
            {
                int value = opcode - Opcodes.ICONST_0;
                out.add("; iconst_" + value);
                out.genPushCode(INT, "" + value);
                break;
            }
            case Opcodes.LCONST_0: // 9
            case Opcodes.LCONST_1: // 10
            {
                int value = opcode - Opcodes.LCONST_0;
                out.add("; lconst " + value);
                out.genPushCode(LONG, "" + value);
                break;
            }
            case Opcodes.FCONST_0: // 11
            case Opcodes.FCONST_1: // 12
            case Opcodes.FCONST_2: // 13
            {
                int value = opcode - Opcodes.FCONST_0;
                out.add("; fconst " + value);
                out.genPushCode(FLOAT, out.floatToString((float) value));
                break;
            }
            case Opcodes.DCONST_0: // 14
            case Opcodes.DCONST_1: // 15
            {
                int value = opcode - Opcodes.DCONST_0;
                out.add("; dconst " + value);
                out.genPushCode(DOUBLE, out.floatToString((double) value));
                break;
            }
            // =============================================== Array Load ==
            case Opcodes.IALOAD: // 46
                out.arrload(cv.getStatistics().getResolver(), "I");
                break;
            case Opcodes.LALOAD: // 47
                out.arrload(cv.getStatistics().getResolver(), "J");
                break;
            case Opcodes.FALOAD: // 48
                out.arrload(cv.getStatistics().getResolver(), "F");
                break;
            case Opcodes.DALOAD: // 49
                out.arrload(cv.getStatistics().getResolver(), "D");
                break;
            case Opcodes.AALOAD: // 50
                out.arrload(cv.getStatistics().getResolver(), "Ljava/lang/Object;");
                break;
            case Opcodes.BALOAD: // 51
                out.arrload(cv.getStatistics().getResolver(), "B");
                break;
            case Opcodes.CALOAD: // 52
                out.arrload(cv.getStatistics().getResolver(), "C");
                break;
            case Opcodes.SALOAD: // 53
                out.arrload(cv.getStatistics().getResolver(), "S");
                break;
            // =============================================== Array Store ==
            case Opcodes.IASTORE: // 79
                out.arrstore(cv.getStatistics().getResolver(), "I");
                break;
            case Opcodes.LASTORE: // 80
                out.arrstore(cv.getStatistics().getResolver(), "J");
                break;
            case Opcodes.FASTORE: // 81
                out.arrstore(cv.getStatistics().getResolver(), "F");
                break;
            case Opcodes.DASTORE: // 82
                out.arrstore(cv.getStatistics().getResolver(), "D");
                break;
            case Opcodes.AASTORE: // 83
                out.arrstore(cv.getStatistics().getResolver(), "Ljava/lang/Object;");
                break;
            case Opcodes.BASTORE: // 84
                out.arrstore(cv.getStatistics().getResolver(), "B");
                break;
            case Opcodes.CASTORE: // 85
                out.arrstore(cv.getStatistics().getResolver(), "C");
                break;
            case Opcodes.SASTORE: // 86
                out.arrstore(cv.getStatistics().getResolver(), "S");
                break;
            // =============================================== Array Store ==
            case Opcodes.POP: // 87 todo
//                stack.pop();
                out.add("; pop");
                out.add("call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                break;
            case Opcodes.POP2: // 88 todo
//                stack.pop();
//                stack.pop();
                out.add("; pop2");
                out.add("call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                out.add("call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                break;
            case Opcodes.DUP: // 89 todo
            {
//                StackValue _op = stack.pop();
//                stack.push(_op);
//                stack.push(_op);

                out.add("; dup");

                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
            }
            break;
            case Opcodes.DUP_X1: // 90 todo
            {
//                StackValue _op1 = stack.pop();
//                StackValue _op2 = stack.pop();
//                stack.push(_op1);
//                stack.push(_op2);
//                stack.push(_op1);
                out.add("; dup x1");
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");

            }
            break;
            case Opcodes.DUP_X2: // 91 todo
            {
//                StackValue _op1 = stack.pop();
//                StackValue _op2 = stack.pop();
//                StackValue _op3 = stack.pop();
//                stack.push(_op1);
//                stack.push(_op3);
//                stack.push(_op2);
//                stack.push(_op1);
                out.add("; dup x2");
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v3 = out.allocReg();
                out.add(v3 + " = call i64 @pop_entry(%struct.StackFrame* thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v3 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");

            }
            break;
            case Opcodes.DUP2: // 92 todo
            {
                out.add("; dup2");
//                StackValue op1 = stack.peek(-1);
//                StackValue op2 = stack.peek(-2);
//                stack.push(op2);
//                stack.push(op1);
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");

            }
            break;
            case Opcodes.DUP2_X1: // 93 todo
            {
//                StackValue _op1 = stack.pop();
//                StackValue _op2 = stack.pop();
//                StackValue _op3 = stack.pop();
//                stack.push(_op2);
//                stack.push(_op1);
//                stack.push(_op3);
//                stack.push(_op2);
//                stack.push(_op1);
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v3 = out.allocReg();
                out.add(v3 + " = call i64 @pop_entry(%struct.StackFrame* thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v3 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");

            }
            break;
            case Opcodes.DUP2_X2: // 94 todo
            {
//                StackValue _op1 = stack.pop();
//                StackValue _op2 = stack.pop();
//                StackValue _op3 = stack.pop();
//                StackValue _op4 = stack.pop();
//                stack.push(_op2);
//                stack.push(_op1);
//                stack.push(_op4);
//                stack.push(_op3);
//                stack.push(_op2);
//                stack.push(_op1);
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v3 = out.allocReg();
                out.add(v3 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v4 = out.allocReg();
                out.add(v4 + " = call i64 @pop_entry(%struct.StackFrame* thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v4 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v3 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");

            }
            break;
            case Opcodes.SWAP: // 95 (Swap only first class values)
            {
//                StackValue _op1 = stack.pop();
//                StackValue _op2 = stack.pop();
//                stack.push(_op1);
//                stack.push(_op2);
                String v1 = out.allocReg();
                out.add(v1 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                String v2 = out.allocReg();
                out.add(v2 + " = call i64 @pop_entry(%struct.StackFrame* %thrd_stack_reg)");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v1 + ")");
                out.add("call void @push_entry(%struct.StackFrame* %thrd_stack_reg, i64 " + v2 + ")");

            }
            break;
            // =============================================== ADD ==
            case Opcodes.IADD: // 96
                out.in2out1("add", INT);
                break;
            case Opcodes.LADD: // 97
                out.in2out1("add", LONG);
                break;
            case Opcodes.FADD: // 98
                out.in2out1("fadd", FLOAT);
                break;
            case Opcodes.DADD: // 99
                out.in2out1("fadd", DOUBLE);
                break;
            // =============================================== SUB ==
            case Opcodes.ISUB: // 100
                out.in2out1("sub", INT);
                break;
            case Opcodes.LSUB: // 101
                out.in2out1("sub", LONG);
                break;
            case Opcodes.FSUB: // 102
                out.in2out1("fsub", FLOAT);
                break;
            case Opcodes.DSUB: // 103
                out.in2out1("fsub", DOUBLE);
                break;
            // =============================================== MUL ==
            case Opcodes.IMUL: // 104
                out.in2out1("mul", INT);
                break;
            case Opcodes.LMUL: // 105
                out.in2out1("mul", LONG);
                break;
            case Opcodes.FMUL: // 106
                out.in2out1("fmul", FLOAT);
                break;
            case Opcodes.DMUL: // 107
                out.in2out1("fmul", DOUBLE);
                break;
            // =============================================== DIV ==
            case Opcodes.IDIV: // 108
                out.in2out1("sdiv", INT);
                break;
            case Opcodes.LDIV: // 109
                out.in2out1("sdiv", LONG);
                break;
            case Opcodes.FDIV: // 110
                out.in2out1("fdiv", FLOAT);
                break;
            case Opcodes.DDIV: // 111
                out.in2out1("fdiv", DOUBLE);
                break;
            // =============================================== REM ==
            case Opcodes.IREM: // 112
                out.in2out1("srem", INT);
                break;
            case Opcodes.LREM: // 113
                out.in2out1("srem", LONG);
                break;
            case Opcodes.FREM: // 114
                out.in2out1("frem", FLOAT);
                break;
            case Opcodes.DREM: // 115
                out.in2out1("frem", DOUBLE);
                break;
            // =============================================== NEG ==
            case Opcodes.INEG: // 116
                out.neg(INT);
                break;
            case Opcodes.LNEG: // 117
                out.neg(LONG);
                break;
            case Opcodes.FNEG: // 118
                out.neg(FLOAT);
                break;
            case Opcodes.DNEG: // 119
                out.neg(DOUBLE);
                break;
            // =============================================== SH* ==
            case Opcodes.ISHL: // 120
                out.in2out1("shl", INT);
                break;
            case Opcodes.LSHL: // 121
                out.operationto("sext", INT, LONG); // extend stack to long (!)
                out.in2out1("shl", LONG);
                break;
            case Opcodes.ISHR: // 122
                out.in2out1("ashr", INT);
                break;
            case Opcodes.LSHR: // 123
                out.operationto("sext", INT, LONG); // extend stack to long (!)
                out.in2out1("ashr", LONG);
                break;
            case Opcodes.IUSHR: // 124
                out.in2out1("lshr", INT);
                break;
            case Opcodes.LUSHR: // 125
                out.operationto("sext", INT, LONG); // extend stack to long (!)
                out.in2out1("lshr", LONG);
                break;
            // =============================================== AND ==
            case Opcodes.IAND: // 126
                out.in2out1("and", INT);
                break;
            case Opcodes.LAND: // 127
                out.in2out1("and", LONG);
                break;
            // =============================================== OR ==
            case Opcodes.IOR: // 128
                out.in2out1("or", INT);
                break;
            case Opcodes.LOR: // 129
                out.in2out1("or", LONG);
                break;
            // =============================================== XOR ==
            case Opcodes.IXOR: // 130
                out.in2out1("xor", INT);
                break;
            case Opcodes.LXOR: // 131
                out.in2out1("xor", LONG);
                break;
            // =============================================== converts ==
            case Opcodes.I2L: // 133
                out.operationto("sext", INT, LONG);
                break;
            case Opcodes.I2F: // 134
                out.sitofp(INT, FLOAT);
                break;
            case Opcodes.I2D: // 135
                out.sitofp(INT, DOUBLE);
                break;
            case Opcodes.L2I: // 136
                out.operationto("trunc", LONG, INT);
                break;
            case Opcodes.L2F: // 137
                out.sitofp(LONG, FLOAT);
                break;
            case Opcodes.L2D: // 138
                out.sitofp(LONG, DOUBLE);
                break;
            case Opcodes.F2I: // 139
                out.fptosi(FLOAT, INT);
                break;
            case Opcodes.F2L: // 140
                out.fptosi(FLOAT, LONG);
                break;
            case Opcodes.F2D: // 141
                out.operationto("fpext", FLOAT, DOUBLE);
                break;
            case Opcodes.D2I: // 142
                out.fptosi(DOUBLE, INT);
                break;
            case Opcodes.D2L: // 143
                out.fptosi(DOUBLE, LONG);
                break;
            case Opcodes.D2F: // 144
                out.operationto("fptrunc", DOUBLE, FLOAT);
                break;
            case Opcodes.I2B: // 145
                out.operationto("trunc", INT, BYTE); //todo ?
                break;
            case Opcodes.I2C: // 146
                out.operationto("trunc", INT, CHAR); //todo ? strunc ?
                break;
            case Opcodes.I2S: // 147
                out.operationto("trunc", INT, SHORT); //todo ? strunc ?
                break;
            // =============================================== Long compares (use with IF* command) ==
            case Opcodes.LCMP: // 148
                commands.push(Prefix.LCMP);
                break;
            // =============================================== Float compares (use with IF* command) ==
            case Opcodes.FCMPL: // 149
                commands.push(Prefix.FCMPL);
                break;
            case Opcodes.FCMPG: // 150
                commands.push(Prefix.FCMPG);
                break;
            // =============================================== Double compares (use with IF* command) ==
            case Opcodes.DCMPL: // 151
                commands.push(Prefix.DCMPL);
                break;
            case Opcodes.DCMPG: // 152
                commands.push(Prefix.DCMPG);
                break;
            // =============================================== returns ==
            case Opcodes.IRETURN: // 172
            case Opcodes.LRETURN: // 173
            case Opcodes.FRETURN: // 174
            case Opcodes.DRETURN: // 175
            case Opcodes.ARETURN: {// 176
                String type = null;
                switch (opcode) {
                    case Opcodes.IRETURN: // 172
                        type = INT;
                        break;
                    case Opcodes.LRETURN: // 173
                        type = LONG;
                        break;
                    case Opcodes.FRETURN: // 174
                        type = FLOAT;
                        break;
                    case Opcodes.DRETURN: // 175
                        type = DOUBLE;
                        break;
                    case Opcodes.ARETURN: // 176
                        type = POINTER;
                        break;
                }

                String v = out.allocReg();
                out.genPopCode(type, v);
                String castv = v;
                if (!type.equals(_resType)) {
                    castv = out.allocReg();
                    out.genCastCode(castv, type, v, _resType);
                }

                out.genRestoreStackCode();
                out.add("ret " + _resType + " " + castv);
                break;
            }
            case Opcodes.RETURN: // 177
                out.genRestoreStackCode();
                out.add("ret void");
                break;
            // =============================================== misc ==
            case Opcodes.ARRAYLENGTH: // 190

                String arrtype = Util.javaSignature2irType(cv.getStatistics().getResolver(), "[I");
                out.arrayLength(arrtype);
                break;
            case Opcodes.ATHROW: // 191
                out.add("athrow"); //
                break;
            case Opcodes.MONITORENTER: // 194
                out.add("monitorenter");
                break;
            case Opcodes.MONITOREXIT: // 195
                out.add("monitorexit");
                break;
            default:
                //System.out.println("IN " + opcode);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int value) {
        switch (opcode) {
            case Opcodes.BIPUSH: // 16
                out.add("; bipush " + value);
                out.genPushCode(INT, "" + value);
                break;
            case Opcodes.SIPUSH: // 17
                out.add("; sipush " + value);
                out.genPushCode(INT, "" + value);
                break;
            case Opcodes.NEWARRAY: // 188
                out.newArray(this.cv.getStatistics().getResolver(), Internals.javacode2javatag(value));
                break;
            default:
                //System.out.println("visitIntInsn " + opcode + " " + value);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int slot) {
        switch (opcode) {
            // =============================================== Load ==
            case Opcodes.ILOAD: // 21
            case Opcodes.LLOAD: // 22
            case Opcodes.FLOAD: // 23
            case Opcodes.DLOAD: // 24
            case Opcodes.ALOAD: // 25
            {
                LocalVar lv = this.vars.get(slot, curLabel);
                String type = Util.javaSignature2irType(this.cv.getStatistics().getResolver(), lv.signature);
                out.comment(type + "load " + slot);

                out.genLoadLocalVarCode(type, slot);
            }
            break;
            // =============================================== Store (Store stack into local variable) ==
            case Opcodes.ISTORE: // 54
            case Opcodes.LSTORE: // 55
            case Opcodes.FSTORE: // 56
            case Opcodes.DSTORE: // 57
            case Opcodes.ASTORE: // 58
            {
                LocalVar lv = this.vars.get(slot, curLabel);
                String type = Util.javaSignature2irType(this.cv.getStatistics().getResolver(), lv.signature);
                out.comment(type + "store " + slot);

                out.genStoreLocalVarCode(type, slot);
            }
            break;
            default:
                //System.out.println("visitVarInsn " + opcode + " " + slot);
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String s) {
        switch (opcode) {
            case Opcodes.NEW: // 187
                out.comment("; new " + s);
                out._new(this.cv.getStatistics().getResolver(), s);
                break;
            case Opcodes.ANEWARRAY: // 189
                if (!s.startsWith("[")) {
                    s = "L" + s + ";";
                }
                out.comment("; anewarray " + s);
                out.newArray(this.cv.getStatistics().getResolver(), s);
                break;
            case Opcodes.CHECKCAST: {// 192
                //out.add("checkcast " + s);
                String sv = out.allocReg();
                String irtype = Util.javaSignature2irType(cv.getStatistics().getResolver(), "L" + s + ";");
                out.genPopCode(POINTER, sv);
                out.genPushCode(POINTER, sv);
                //todo gust
                break;
            }
            case Opcodes.INSTANCEOF: {// 193
                //out.add("instanceof " + s);
                String sv = out.allocReg();
                String irtype = Util.javaSignature2irType(cv.getStatistics().getResolver(), "L" + s + ";");
                out.genPopCode(irtype, sv);
                out.genPushCode(INT, "1");
                //todo gust
                break;
            }
            default:
                //System.out.println("visitTypeInsn " + opcode + " " + s);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String className, String name, String signature) {
        switch (opcode) {
            case Opcodes.GETSTATIC: // 178
                out.getstatic(this.cv.getStatistics().getResolver(), className, name, signature);
                if (!className.equals(this.cv.className)) {
                    this.cv.getStaticFields().add(new JField(className, name, signature));
                }
                break;
            case Opcodes.PUTSTATIC: // 179
                out.putstatic(this.cv.getStatistics().getResolver(), className, name, signature);
                break;
            case Opcodes.GETFIELD: // 180
                out.getfield(this.cv.getStatistics().getResolver(), className, name, signature);
                break;
            case Opcodes.PUTFIELD: // 181
                out.putfield(this.cv.getStatistics().getResolver(), className, name, signature);
                break;
            default:
                //System.out.println("visitFieldInsn " + opcode + " " + className);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String s, String s1, String s2) {
        //System.out.println("visitMethodInsn " + opcode + " " + s);
        visitMethodInsn(opcode, s, s1, s2, true);
    }

    @Override
    public void visitMethodInsn(int opcode, String className, String methodName, String signature, boolean b) {
        out.comment("invoke " + className + "." + methodName);
        switch (opcode) {
            case Opcodes.INVOKEINTERFACE:
            case Opcodes.INVOKEVIRTUAL: // 182
            {

                JSignature s = new JSignature(this.cv.getStatistics().getResolver(), signature);
                String classIr = Util.class2irType(this.cv.getStatistics().getResolver(), className);
                s.getArgs().add(0, classIr);

                String call = out.getSignatureCall(className, methodName, s, null);
                if ("void".equals(s.getResult())) {
                    out.add("call  " + call);
                } else {
                    String ret = out.allocReg();
                    out.add(ret + " = call " + call);
                    out.genPushCode(s.getResult(), ret);
                }
                // declare
                boolean inClass = this.cv.className.equals(className);
                if (!inClass) {
                    String tmps = s.getSignatureDeclare(className, methodName, null);
//                    call = call.replaceAll("\\%stack[0-9]{1,3}", "");
                    this.cv.declares.add(tmps);
                }
            }
            break;
            case Opcodes.INVOKESPECIAL: // 183
            {
                JSignature s = new JSignature(this.cv.getStatistics().getResolver(), signature);
                String classIr = Util.class2irType(this.cv.getStatistics().getResolver(), className);
                s.getArgs().add(0, classIr);

                String call = out.getSignatureCall(className, methodName, s, null);
                if ("void".equals(s.getResult())) {
                    out.add("call " + call + " ; special call private or <init>");
                } else {
                    String ret = out.allocReg();
                    out.add(ret + " = call " + call + " ; special call private");
                    out.genPushCode(s.getResult(), ret);
                }
                // declare
                boolean inClass = this.cv.className.equals(className);
                if (!inClass) {
                    String tmps = s.getSignatureDeclare(className, methodName, null);
                    this.cv.declares.add(tmps);
                }
            }
            break;
            case Opcodes.INVOKESTATIC: // 184
            {
                if (methodName.equals("abs")) {
                    int debug = 1;
                }
                JSignature s = new JSignature(this.cv.getStatistics().getResolver(), signature);
                String classIr = Util.class2irType(this.cv.getStatistics().getResolver(), className);
                String call = out.getSignatureCall(className, methodName, s, null);
                if ("void".equals(s.getResult())) {
                    out.add("call " + call);
                } else {
                    String ret = out.allocReg();
                    out.add(ret + " = call " + call + " ; static");
                    out.genPushCode(s.getResult(), ret);

                }
                // declare
                boolean inClass = this.cv.className.equals(className);
                if (!inClass) {
                    String tmps = s.getSignatureDeclare(className, methodName, null);
                    this.cv.declares.add(tmps);
                }
            }
            break;
            case Opcodes.INVOKEDYNAMIC: // 186
            {
                JSignature s = new JSignature(this.cv.getStatistics().getResolver(), signature);
                String classTypeName = Util.class2irType(this.cv.getStatistics().getResolver(), className);
                String call = out.getSignatureCall(className, methodName, s, null);
                if ("void".equals(s.getResult())) {
                    out.add("call " + call);
                } else {
                    String ret = out.allocReg();
                    out.add(ret + " = call " + call + " ; dyna");
                    out.genPushCode(s.getResult(), ret);
                }
                // declare
                boolean inClass = this.cv.className.equals(className);
                if (!inClass) {
                    String tmps = s.getSignatureDeclare(className, methodName, classTypeName);
                    this.cv.declares.add(tmps);
                }
            }
            break;
            default:
                System.out.println("visitMethodInsn " + opcode);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
        //System.out.println("visitInvokeDynamicInsn " + s + " " + s1);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        switch (opcode) {
            case Opcodes.IFEQ: // 153
            case Opcodes.IFNE: // 154
            case Opcodes.IFLT: // 155
            case Opcodes.IFGE: // 156
            case Opcodes.IFGT: // 157
            case Opcodes.IFLE: // 158
            {
                usedLabels.add(label.toString());
                out.branch(commands, label, opcode - Opcodes.IFEQ, 1);//pop 1 oprand
                break;
            }
            case Opcodes.IF_ICMPEQ: // 159
            case Opcodes.IF_ICMPNE: // 160
            case Opcodes.IF_ICMPLT: // 161
            case Opcodes.IF_ICMPGE: // 162
            case Opcodes.IF_ICMPGT: // 163
            case Opcodes.IF_ICMPLE: // 164
            {
                usedLabels.add(label.toString());
                out.branch(commands, label, opcode - Opcodes.IF_ICMPEQ, 2);//pop 2 oprand
                break;
            }
            case Opcodes.IF_ACMPEQ: // 165
            case Opcodes.IF_ACMPNE: // 166
            {
                usedLabels.add(label.toString());


                String sv1 = out.allocReg();
                String sv2 = out.allocReg();
                out.genPopCode(POINTER, sv1);
                out.genPopCode(POINTER, sv2);
                out.add("%__tmpc" + out.tmp + " = icmp " + IR.ICMP[opcode == Opcodes.IF_ACMPEQ ? IR.EQ : IR.NE] + " " + POINTER + " " + sv1 + ", " + sv2);
                out.add("br i1 %__tmpc" + out.tmp + ", label %" + label + ", label %__tmpl" + out.tmp);
                out.add("__tmpl" + out.tmp + ":");
                out.tmp++;
                break;
            }
            case Opcodes.GOTO: // 167
                usedLabels.add(label.toString());
                out.add("br label %" + label);
                break;
            case Opcodes.JSR: // 168
                out.add("jsr*"); // todo
                break;
            case Opcodes.RET: // 169
                out.add("ret*"); // todo
                break;
            case Opcodes.IFNULL: // 198
            case Opcodes.IFNONNULL: // 199
                String sv = out.allocReg();
                out.genPopCode(POINTER, sv);
                out.add("%__tmpc" + out.tmp + " = icmp " + IR.ICMP[opcode == Opcodes.IFNULL ? IR.EQ : IR.NE] + " " + POINTER + " " + sv + ", null");
                out.add("br i1 %__tmpc" + out.tmp + ", label %" + label + ", label %__tmpl" + out.tmp);
                out.add("__tmpl" + out.tmp + ":");
                out.tmp++;
                usedLabels.add(label.toString());
                break;
            default:
                out.add("visitJumpInsn " + opcode + " " + label.toString());
        }
    }

    @Override
    public void visitLabel(Label label) {
        labels.add(label.toString());
        //out.add("; label " + labels.indexOf(label) + " :" + label);
        //vars.activeVars(label.toString());
        curLabel = label.toString();
        vars.addUseLabel(label.toString());
        out.add(label.toString() + ":");
    }

    @Override
    public void visitLineNumber(int i, Label label) {
        //out.add("; line no " + i + " , " + label);
        //System.out.println("LN " + i + " " + label);
        //super.visitLineNumber(i, label);
    }

    @Override
    public void visitLdcInsn(Object o) {

        if (o instanceof String) {
            out.add("; ldc " + o);
            out.newString(cv, (String) o);
        } else if (o instanceof Integer) {
            Integer value = (Integer) o;
            out.add("; ldc " + value);
            out.genPushCode(INT, o.toString());
        } else if (o instanceof Long) {
            Long value = (Long) o;
            out.add("; ldc " + value);
            out.genPushCode(LONG, o.toString());
        } else if (o instanceof Float) {
            Float value = (Float) o;
            out.add("; ldc " + value);
            out.genPushCode(FLOAT, out.floatToString((float) o));
        } else if (o instanceof Double) {
            Double value = (Double) o;
            out.add("; ldc " + value);
            out.genPushCode(DOUBLE, out.floatToString((double) o));
        } else {
            out.add("; todo add const " + o);
        }
    }

    @Override
    public void visitIincInsn(int slot, int value) {

        LocalVar var = this.vars.get(slot, curLabel);
        String type = Util.javaSignature2irType(cv.getStatistics().getResolver(), var.signature);
        String iv = out.allocReg();
        out.genGetLocalVarCode(type, slot, iv);
        String rv = out.allocReg();
        out.add(rv + " = add i32 " + iv + ", " + value);
        out.genSetLocalVarCode(type, slot, rv);
    }


    @Override
    public void visitTableSwitchInsn(int from, int to, Label label, Label... labels) {
        usedLabels.add(label.toString());
        String sv = out.allocReg();
        out.genPopCode(INT, sv);
        out.add("switch " + INT + " " + sv + ", label %" + label + " [");
        for (Label l : labels) {
            usedLabels.add(l.toString());
            out.add("    i32 " + from + ", label %" + l);
            from++;
        }
        out.add("]");
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] values, Label[] labels) {
        usedLabels.add(label.toString());
        String sv = out.allocReg();
        out.genPopCode(INT, sv);
        out.add("switch " + INT + " " + sv + ", label %" + label + " [");
        for (int i = 0; i < values.length; i++) {
            usedLabels.add(labels[i].toString());
            out.add("    i32 " + values[i] + ", label %" + labels[i]);
        }
        out.add("]");
    }

    @Override
    public void visitMultiANewArrayInsn(String s, int dims) {
        out.comment("; Multi Dimension Array: " + s + " " + dims);
        List<String> dimms = new ArrayList<>();
        for (int i = 0; i < dims; i++) {
            String sv = out.allocReg();
            out.genPopCode(INT, sv);
            dimms.add(sv);
        }
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int i, TypePath typePath, String s, boolean b) {
        //System.out.println("visitInsnAnnotation");
        return null;
    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {
        //System.out.println("visitTryCatchBlock " + label + " " + label1 + " " + s);
        out.add(";try catch :" + label + " " + label1 + " " + label2);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int i, TypePath typePath, String s, boolean b) {
        //System.out.println("visitTryCatchAnnotation");
        return null;
    }

    @Override
    public void visitLocalVariable(String name, String sign, String s2, Label label, Label label1, int slot) {
/*
        vars.put(slot, new _LocalVar(name, sign));
        Util.javaSignature2irType(this.cv.getStatistics().getResolver(), sign);
*/
//        if (name.equals("j") && slot == 7) {
//            int debug = 1;
//            //out.add("vistLocalVariable "+name +" jcount" );
//            System.out.println("visitLocalVariable + " + name + " / " + sign + " " + label + " " + " " + label1 + " " + slot);
//        }
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int i, TypePath typePath, Label[] labels, Label[] labels1, int[] ints, String s, boolean b) {
//        for(int d=0;d<i;d++){
//            out.add("; localvar annotation "+labels[i]+" "+labels1[i]+ " "+i+" "+s+" "+b);
//        }
        return super.visitLocalVariableAnnotation(i, typePath, labels, labels1, ints, s, b);
    }


    @Override
    public void visitMaxs(int stack, int local) {
        // Maximums
        this.max_stack = stack;
        this.max_local = local;
    }

    @Override
    public void visitEnd() {
    }


    public void out(PrintStream ps) {
        // 0) info
        JSignature ss = new JSignature(this.cv.getStatistics().getResolver(), this.javaSignature);

        ps.print("; locals: ");
        ps.println(max_local);
        ps.print("; stack: ");
        ps.println(max_stack);
        ps.print("; args: ");
        ps.println(this._argTypes.size());


        String methodIrName = this._resType + " @" + Util.classMethodSignature2id(this.cv.className, this.methodName, ss) + "(" + Util.enumArgs(this._argTypes, "%s") + ")";
        if (methodName.equals("<clinit>")) {
            AssistLLVM.addClinit(methodIrName);
        }

        String define = "define " + methodIrName + "{";
        ps.println(define);


        String[] result = out.getStrings().toArray(new String[0]);

        labels.removeAll(usedLabels);
        for (int i = 0; i < result.length; i++) {
            for (String lb : labels) {
                if (result[i].indexOf(lb) >= 0)
                    result[i] = result[i].replace(lb + ":", "; ");
            }
        }

        discardDoubleLabel(result);

        fixNoBr(result);

        String ms = mergeString(result);
        result = ms.split("\n");
        discardDoubleLabel(result);

//        IrFunction irf = new IrFunction(cv.className, methodName, javaSignature);
//        irf.define = define;
//        irf.end = "}";
//        irf.parse(result);
//        String str = irf.toString();
//        result = str.split("\n");

        for (int i = 0; i < result.length; i++) {
            ps.print("    ");
            ps.println(result[i]);
        }
        // 3) end
        ps.println("}");
        ps.println("");
    }


    /**
     * clear the code line ,remove comment ,remove space
     *
     * @param s
     * @return
     */
    String codeClear(String s) {
        String r = s;
        if (s.indexOf(";") >= 0) {
            r = s.substring(0, s.indexOf(";"));
        }
        r = r.trim();
        return r;
    }

    String mergeString(String[] ss) {
        String r = "";
        for (String s : ss) {
            r += s + "\n";
        }
        return r;
    }

    void discardDoubleLabel(String[] result) {
        //found tow label nearly, merge to one label
        Map<String, String> replacepair = new HashMap<>();
        String preLine = null;
        for (int i = 0; i < result.length; i++) {
            String cs = codeClear(result[i]);
            if (cs.endsWith(":")) {
                if (preLine != null) {
                    cs = cs.replace(":", "");
                    replacepair.put(preLine, cs);
                    preLine = cs;
                } else {
                    cs = cs.replace(":", "");
                    preLine = cs;
                }
            } else if (cs.length() == 0) {
                continue;
            } else {
                preLine = null;
            }
        }
        for (int i = 0; i < result.length; i++) {
            String str = result[i];
            for (String s1 : replacepair.keySet()) {
                String s2 = replacepair.get(s1);
                if (str.indexOf(s1 + ":") >= 0) {
                    result[i] = str.replace(s1 + ":", "; replaced " + s1 + " with " + s2);
                } else {
                    result[i] = str.replace(s1, s2);
                }
            }

        }
    }

    void fixNoBr(String[] result) {

        // find all label but the preline not br , add br line before label line
        String preLine = null;
        for (int i = 0; i < result.length; i++) {
            String cs = codeClear(result[i]);
            if (cs.startsWith("br") || cs.startsWith("ret") || cs.startsWith("define ") || i == 0) {
                preLine = cs;
                continue;
            }
            if (cs.endsWith(":")) {
                cs = cs.replace(":", "");
                if (preLine == null) {
                    result[i] = "br label %" + cs + "\n" + result[i];
                    preLine = null;
                }
            } else if (cs.length() == 0) {
                continue;
            } else {
                preLine = null;
            }
        }
    }
}
