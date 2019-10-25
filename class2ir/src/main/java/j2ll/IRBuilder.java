package j2ll;

import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static j2ll.Internals.*;

/**
 * IR Builder
 */
public class IRBuilder {

    int tmp;

    private List<String> strings = new ArrayList<String>();

    public void add(String str) {
        strings.add(str);
    }

    public void comment(String str) {
        strings.add("; " + str);
    }

    public String floatToString(Object value) {
        if (value instanceof Float) {
            Float f = (Float) value;
            return "0x" + Integer.toHexString(Float.floatToRawIntBits(f)) + "00000000";
        }
        if (value instanceof Double) {
            Double f = (Double) value;
            return "0x" + Long.toHexString(Double.doubleToRawLongBits(f));
        }
        return value.toString(); // imm ??
    }

    public void addImm(Object value, String type, RuntimeStack stack) {
        String immname = "%_imm_" + tmp;
        add(immname + " = alloca " + type);
        add("store " + type + " " + floatToString(value) + ", " + type + "* " + immname);
        String sv = stack.push(type);
        add(sv + " = load " + type + ", " + type + "* " + immname);
        tmp++;
    }

    class SPair {
        StackValue p1;
        StackValue p2;
    }

    //convert op1 to type ir2
    public StackValue castP1ToP2(RuntimeStack stack, StackValue op1, String ir2) {
        String ir1 = op1.getIR();

        if (!ir2.equals(ir1)) {
            if (ir1.equals(LONG)) {
                if (ir2.equals(FLOAT)) {
                    throw new RuntimeException("  long  to float");
                } else {
                    String resvt = stack.push(ir2);
                    add(resvt + " = trunc " + op1.fullName() + " to " + ir2);
                }
            } else if (ir1.equals(INT)) {
                if (ir2.equals(FLOAT)) {
                    throw new RuntimeException("  long  to float");
                } else if (ir2.equals(LONG)) {
                    String resvt = stack.push(ir2);
                    add(resvt + " = sext " + op1.fullName() + " to " + ir2);
                } else {
                    String resvt = stack.push(ir2);
                    add(resvt + " = trunc " + op1.fullName() + " to " + ir2);
                }
            } else if (ir1.equals(FLOAT) || ir1.equals(DOUBLE)) {
                throw new RuntimeException(" float to int");
            } else if (ir1.equals(BOOLEAN)) {
                String resvt = stack.push(ir2);
                add(resvt + " = sext " + op1.fullName() + " to " + ir2);
            } else if (ir1.equals(BYTE)) {
                String resvt = stack.push(ir2);
                if (ir1.equals(BOOLEAN)) {
                    add(resvt + " = trunc " + op1.fullName() + " to " + ir2);
                } else {
                    add(resvt + " = sext " + op1.fullName() + " to " + ir2);
                }
            } else if (ir1.equals(CHAR)) {
                String resvt = stack.push(ir2);
                if (ir1.equals(BOOLEAN) || ir1.equals(BYTE)) {
                    add(resvt + " = trunc " + op1.fullName() + " to " + ir2);
                } else {
                    add(resvt + " = zext " + op1.fullName() + " to " + ir2);
                }
            } else if (ir1.equals(SHORT)) {
                String resvt = stack.push(ir2);
                if (ir1.equals(BOOLEAN) || ir1.equals(BYTE)) {
                    add(resvt + " = trunc " + op1.fullName() + " to " + ir2);
                } else {
                    add(resvt + " = sext " + op1.fullName() + " to " + ir2);
                }
            } else {
                String resvt = stack.push(ir2);
                add(resvt + " = bitcast " + op1.fullName() + " to " + ir2);
            }
            return stack.pop();
        }
        return op1;
    }

    public void extValueType(RuntimeStack stack, SPair pair) {
        StackValue op2 = pair.p2;
        StackValue op1 = pair.p1;
        String ir2 = op2.getIR();
        String ir1 = op1.getIR();

        if (!ir1.equals(ir2)) {
            String max = null;
            if (ir2.equals(LONG) || ir1.equals(LONG)) max = LONG;
            else if (ir2.equals(INT) || ir1.equals(INT)) max = INT;
            else if (ir2.equals(SHORT) || ir1.equals(SHORT)) max = SHORT;
            else if (ir2.equals(CHAR) || ir1.equals(CHAR)) max = CHAR;
            else throw new RuntimeException("oprand type mismartch");

            stack.push(max);
            StackValue tmpsv = stack.pop();
            if (ir2.equals(max)) {
                if (ir1.equals(CHAR))
                    add(tmpsv + " = zext " + op1.fullName() + " to " + max);
                else
                    add(tmpsv + " = sext " + op1.fullName() + " to " + max);
                pair.p1 = tmpsv;
            } else {
                if (ir2.equals(CHAR))
                    add(tmpsv + " = zext " + op2.fullName() + " to " + max);
                else
                    add(tmpsv + " = sext " + op2.fullName() + " to " + max);
                pair.p2 = tmpsv;
            }
        }
    }

    public void in2out1(RuntimeStack stack, String op, String type) {
        StackValue op2 = stack.pop();
        StackValue op1 = stack.pop();
        SPair pair = new SPair();
        pair.p1 = op1;
        pair.p2 = op2;
        extValueType(stack, pair);
        op1 = pair.p1;
        op2 = pair.p2;


        String res = stack.push(op2.getIR());
        StringBuilder tmp = new StringBuilder();
        tmp.append(res);
        tmp.append(" = ");
        tmp.append(op);
        tmp.append(' ');
        tmp.append(op1.fullName());
        tmp.append(", ");
        tmp.append(op2);
        add(tmp.toString());
    }


    public void newString(CV cv, RuntimeStack stack, String src) {
        try {
            char[] carr = src.toCharArray();
            String dest = "";
            for (int i = 0; i < carr.length; i++) {
                char sb = carr[i];
                dest += (i == 0 ? "" : ",");
                dest += "i16 " + Short.toString((short) sb);
            }

            String ty = Util.javaSignature2irType(cv.getStatistics().getResolver(), "Ljava/lang/String;"); //for add class java.lang.String declare
            String varName = "@str" + src.hashCode();
            String str = varName + " = internal global { i32, [" + carr.length + " x i16] } { i32 " + carr.length + ",[" + carr.length + " x i16] [" + dest + "]}, align 4 \n";
            String carrIRtype = Util.javaSignature2irType(cv.getStatistics().getResolver(), "[C \n");
            String strptrName = "@strptr" + src.hashCode();
            String strptr = strptrName + " = internal global " + carrIRtype + " bitcast ( { i32 ,[" + carr.length + " x i16]}* " + varName + " to " + carrIRtype + ")";
            if (!cv.staticStrs.containsKey(str)) {
                cv.staticStrs.put(src, str + strptr);
            }


            String funcName = AssistLLVM.getConstStringFuncName();
            cv.declares.add(funcName);
//            if (!cv.declares.contains(funcName)) {
//                if (!constFuncPusblished) {
//                    cv.assistFunc.add(extFunc);
//                    constFuncPusblished = true;
//                    cv.declares.remove(funcName);// not remove wuold redefine
//                }
//                if (!cv.className.equals("java/lang/String")) {
//                    cv.declares.add("void @java_lang_String__init___C(%java_lang_String*, {i32, [0 x i16]}*)");
//                }
//            }

            String v1s = stack.push(carrIRtype);
            add(v1s + " = load " + carrIRtype + " , " + carrIRtype + "* @strptr" + src.hashCode());
            stack.pop();
            String res = stack.push(ty);
            String reg = allocReg();
            add(reg + " = call " + ty + " @construct_string_with_char_arr_(" + carrIRtype + " " + v1s + ")");
            add(res + " = bitcast " + ty + " " + reg + " to " + ty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void arrayLength(RuntimeStack stack) {
        StackValue sv = stack.pop();
        String result = stack.push(Internals.INT);
        String reg = allocReg();
        // out
        comment("arraylength " + sv.fullName());
        getelementptr(reg, sv, 0, 0);
        load(result, Internals.INT, reg, stack);
    }


    public void _new(RuntimeStack stack, Resolver resolver, String name) {
        // state
        String struct = resolver.resolveStruct(name);
        String object = resolver.resolve(name);
        String res = stack.pushObjRef(object);
        String reg = allocReg();
        // out
        comment(resolver.resolveStruct(name));
        add("%__objptr" + tmp + " = getelementptr " + struct + ", " + struct + "* null, i32 1");
        add("%__memsize" + tmp + " = ptrtoint " + struct + "* %__objptr" + tmp + " to i32 ");
        add(reg + " = call i8* @malloc(i32 %__memsize" + tmp + ")");
        add(";call void @print_debug(i32 %__memsize" + tmp + ")");
        add(res + " = bitcast i8* " + reg + " to " + object);
    }

    public void neg(RuntimeStack stack, String type) {
        StackValue value = stack.pop();
        String res = stack.push(type);
        if (Internals.INT.equals(type) || Internals.LONG.equals(type)) {
            add(res + " = sub " + value.getIR() + " 0, " + value);
        } else {
            add(res + " = fsub " + value.getIR() + " 0.0, " + value);
        }
    }


    public void branch(RuntimeStack stack, Stack<String> commands, Label label, int op, int popCount) {
        if (commands.size() == 0) {
            if (popCount == 1) {
                StackValue op1 = stack.pop();
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + op1.fullName() + ", 0");
            } else {
                StackValue op2 = stack.pop();
                StackValue op1 = stack.pop();
                SPair pair = new SPair();
                pair.p1 = op1;
                pair.p2 = op2;
                extValueType(stack, pair);
                op1 = pair.p1;
                op2 = pair.p2;
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + op1.fullName() + ", " + op2);
            }
            add("br i1 %__tmpc" + tmp + ", label %" + label + ", label %_if.else" + tmp);
            add("_if.else" + tmp + ":");
            tmp++;
        } else {
            // POP prefix
            String cmd = commands.pop();
            if (Prefix.FCMPL.equals(cmd) || Prefix.FCMPG.equals(cmd) || Prefix.DCMPL.equals(cmd) || Prefix.DCMPG.equals(cmd)) {
                // double compare
                StackValue op2 = stack.pop();
                StackValue op1 = stack.pop();
                SPair pair = new SPair();
                pair.p1 = op1;
                pair.p2 = op2;
                extValueType(stack, pair);
                op1 = pair.p1;
                op2 = pair.p2;
                add("%__tmpc" + tmp + " = fcmp " + IR.FCMP[op] + " " + op1.fullName() + ", " + op2); // ordered compare
            } else if (Prefix.LCMP.equals(cmd)) {
                // long compare
                StackValue op2 = stack.pop();
                StackValue op1 = stack.pop();
                SPair pair = new SPair();
                pair.p1 = op1;
                pair.p2 = op2;
                extValueType(stack, pair);
                op1 = pair.p1;
                op2 = pair.p2;
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + op1.fullName() + ", " + op2);
            } else {
                System.err.println("Unknown prefix: " + cmd);
            }
            add("br i1 %__tmpc" + tmp + ", label %" + label + ", label %_if.else" + tmp);
            add("_if.else" + tmp + ":");
            tmp++;
        }
    }

    // todo split primitive and objects
    public void newArray(RuntimeStack stack, Resolver resolver, String javaArrayType) {
        StackValue op = stack.pop();
        comment("new array " + javaArrayType + " size: " + op);
        String ty = Util.javaSignature2irType(resolver, javaArrayType);
        String res = stack.pushObjRef(ty);
        String reg1 = allocReg();
        String reg2 = allocReg();//for arrlength
        String reg3 = allocReg();
        // size array in bytes
        int bytes = Internals.sizeOf(javaArrayType.substring(1));
        if (bytes == 0) { //object
            String reg4 = allocReg();
            add(reg4 + " = call i32 @ptr_size()");
            add(reg1 + " = mul " + op.fullName() + ", " + reg4);
        } else {
            add(reg1 + " = mul " + op.fullName() + ", " + bytes);
        }
        add(reg2 + " = add i32 " + reg1 + ", 4");
        add(reg3 + " = call i8* @malloc(i32 " + reg2 + ")");
        add(res + " = bitcast i8* " + reg3 + " to " + ty);

        //save array length to struct first  {i32,[0 x type]}
        String reg5 = allocReg();
        getelementptr(reg5, ty.substring(0, ty.length() - 1), res, 0, 0);
        store(INT, op, reg5, stack);
    }

    public void putfield(RuntimeStack stack, Resolver resolver, String className, String name, String signature) {
        // state
        StackValue value = stack.pop();
        StackValue inst = stack.pop();
        String ty = Util.javaSignature2irType(resolver, signature);
        String sp = allocReg();
        // out
        comment("putfield " + className + " " + name + " " + signature + " ( " + inst.fullName() + " := " + value.fullName() + " )");
        getelementptr(sp, inst, 0, Util.class2ptr(className, name));
        store(ty, value, sp, stack);
    }

    public void putstatic(RuntimeStack stack, Resolver resolver, String className, String name, String signature) {
        // state
        StackValue value = stack.pop();
        String ty = Util.javaSignature2irType(resolver, signature);
        String sp = allocReg();
        // out
        comment("putstatic " + className + " " + name + " " + signature + " ( " + signature + " := " + value.fullName() + " )");
        getelementptr(sp, ty, Util.static2str(className, name));
        store(ty, value, sp, stack);
    }


    public void getfield(RuntimeStack stack, Resolver resolver, String className, String name, String signature) {
        // state
        StackValue inst = stack.pop();
        String ty = Util.javaSignature2irType(resolver, signature);
        String result = stack.push(ty);
        String sp = allocReg();
        // out
        comment("getfield " + className + " " + name + " " + signature + " ( " + inst.fullName() + " )");
        getelementptr(sp, inst, 0, Util.class2ptr(className, name));
        load(result, ty, sp, stack);
    }

    public void getstatic(RuntimeStack stack, Resolver resolver, String className, String name, String signature) {
        // state
        String ty = Util.javaSignature2irType(resolver, signature);
        String result = stack.push(ty);
        String sp = allocReg();
        // out
        comment("getstatic " + className + " " + name + " " + signature + " ( " + result + " := " + signature + " )");
        getelementptr(sp, ty, Util.static2str(className, name));
        load(result, ty, sp, stack);

    }

    public void arrstore(RuntimeStack stack, String type) {
        // state
        StackValue value = stack.pop();
        StackValue index = stack.pop();
        StackValue arrayRef = stack.pop();
        String sp = allocReg();
        // out
        comment(type + "astore ");
        getelementptr(sp, arrayRef, 0, 1, index.fullName()); // pointer to element of array
        store(type, value, sp, stack);
    }

    public void aastore(RuntimeStack stack) {
        // state
        StackValue value = stack.pop();
        StackValue index = stack.pop();
        StackValue arrayRef = stack.pop();
        String sp = allocReg();
        // out
        comment("aastore " + value.getIR());
        getelementptr(sp, arrayRef, 0, 1, index.fullName()); // pointer to element of array
        store(value.getIR(), value, sp, stack);
    }

    public void arrload(RuntimeStack stack, Resolver resolver, String javatype) {
        String type = Util.javaSignature2irType(resolver, javatype);
        // state
        StackValue index = stack.pop();
        StackValue arrayRef = stack.pop();
        String value = stack.push(type);
        String sp = allocReg();
        // out
        comment(type + "aload " + javatype);
        getelementptr(sp, arrayRef, 0, 1, index.fullName()); // pointer to element of array
        load(value, type, sp, stack);

    }

    public void aaload(RuntimeStack stack) {
        // state
        StackValue index = stack.pop();
        StackValue arrayRef = stack.pop();
        String ty = Internals.dearrayOf(arrayRef.getIR());
        String value = stack.push(ty);
        String sp = allocReg();
        // out
        comment("aaload " + ty);
        getelementptr(sp, arrayRef, 0, 1, index.fullName()); // pointer to element of array
        load(value, ty, sp, stack);
    }

    public void fptosi(RuntimeStack stack, String type) {
        operationto(stack, "fptosi", type);
    }

    public void sitofp(RuntimeStack stack, String type) {
        operationto(stack, "sitofp", type);
    }


    public void operationto(RuntimeStack stack, String op, String type) {
        StackValue f = stack.pop();
        String res = stack.push(type);
        add(res + " = " + op + " " + f.fullName() + " to " + type);
    }

    // =================================================================================================================

    public String allocReg() {
        String result = "%__tmp" + tmp;
        tmp++;
        return result;
    }

    // <result> = getelementptr <pty>* <ptrval>{, <ty> <idx>}*
    public void getelementptr(String result, String pty, String ptrval, Object... idx) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(result);
        tmp.append(" = getelementptr ");
        tmp.append(pty);
        tmp.append(", ");
        tmp.append(pty);
        tmp.append("* ");
        tmp.append(ptrval);
        for (Object id : idx) {
            if (id instanceof Integer) {
                tmp.append(", i32 ");
            } else if (id instanceof Long) {
                tmp.append(", i64 ");
            }
            tmp.append(id);
        }
        add(tmp.toString());
    }

    public void getelementptr(String result, StackValue sv, Object... idx) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(result);
        tmp.append(" = getelementptr ");
        tmp.append(sv.getIR().replace('*', ' '));
//        tmp.append(sv.getIR().replace('*',' '));
        tmp.append(", ");
        tmp.append(sv.fullName());
        for (Object id : idx) {
            tmp.append(", ");
            if (id instanceof Integer) {
                tmp.append("i32 ");
                tmp.append(id);
            } else if (id instanceof Long) {
                tmp.append("i64 ");
                tmp.append(id);
            } else {
                tmp.append(id);
            }
        }
        add(tmp.toString());
    }

    // <result> = load [volatile] <ty>* <pointer>
    public void load(String result, String ty, String pointer, RuntimeStack stack) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(result);
        tmp.append(" = load ");
        tmp.append(ty);
        tmp.append(", ");
        tmp.append(ty);
        tmp.append("* ");
        tmp.append(pointer);
        add(tmp.toString());
//        if (ty.equals(SHORT) || ty.equals(BYTE)) {
//            StackValue sv = stack.pop();
//            String resvt = stack.push(INT);
//            add(resvt + " = sext " + sv.fullName() + " to " + INT);
//        } else if (ty.equals(BOOLEAN) || ty.equals(CHAR)) {
//            StackValue sv = stack.pop();
//            String resvt = stack.push(INT);
//            add(resvt + " = zext " + sv.fullName() + " to " + INT);
//        }
    }

    // store [volatile] <ty> <value>, <ty>* <pointer>
    public void store(String ty, StackValue sv, String result, RuntimeStack stack) {
//        String resvt = sv.toString();
//        if (!ty.equals(sv.getIR())) {
//            if (ty.equals(BOOLEAN)) {
//                resvt = stack.push(BOOLEAN);
//                stack.pop();
//                add(resvt + " = trunc " + sv.fullName() + " to " + BOOLEAN);
//            } else if (ty.equals(CHAR)) {
//                resvt = stack.push(CHAR);
//                stack.pop();
//                add(resvt + " = trunc " + sv.fullName() + " to " + CHAR);
//            } else if (ty.equals(SHORT)) {
//                resvt = stack.push(SHORT);
//                stack.pop();
//                add(resvt + " = trunc " + sv.fullName() + " to " + SHORT);
//            } else if (ty.equals(BYTE)) {
//                resvt = stack.push(BYTE);
//                stack.pop();
//                add(resvt + " = trunc " + sv.fullName() + " to " + BYTE);
//            }
//        }
        sv = castP1ToP2(stack, sv, ty);

        StringBuilder tmp = new StringBuilder();
        tmp.append("store ");
        tmp.append(ty);
        tmp.append(" ");
        tmp.append(sv.toString());
        tmp.append(", ");
        tmp.append(ty);
        tmp.append("* ");
        tmp.append(result);
        add(tmp.toString());
    }


    public List<String> getStrings() {
        return strings;
    }
}
