package j2ll;

import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;

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


    class SPair {
        StackValue p1;
        StackValue p2;
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

    public void in2out1(String op, String type) {
        String v1 = allocReg();
        genPopCode(type, v1);
        String v2 = allocReg();
        genPopCode(type, v2);

        String result = allocReg();
        add(result + " = " + op + " " + type + " " + v2 + ", " + v1);
        genPushCode(type, result);

    }

    public void in2out1(String op, String type1, String type2) {
        String v1 = allocReg();
        genPopCode(type1, v1);
        String v2 = allocReg();
        genPopCode(type2, v2);

        String result = allocReg();
        add(result + " = " + op + " " + type2 + " " + v2 + ", " + v1);
        genPushCode(type2, result);
    }


    public void newString(CV cv, String src) {
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
            String carrIRtype = Util.javaSignature2irType(cv.getStatistics().getResolver(), "[C");
            String strptrName = "@strptr" + src.hashCode();
            String strptr = strptrName + " = internal global " + carrIRtype + " bitcast ( { i32 ,[" + carr.length + " x i16]}* " + varName + " to " + carrIRtype + ")";
            if (!cv.staticStrs.containsKey(str)) {
                cv.staticStrs.put(src, str + strptr);
            }


            String funcName = AssistLLVM.getConstStringFuncName();
            cv.declares.add(funcName);

            String v1s = allocReg();
            add(v1s + " = load " + carrIRtype + " , " + carrIRtype + "* @strptr" + src.hashCode());
            String res = allocReg();
            String reg = allocReg();
            add(reg + " = call " + ty + " @construct_string_with_char_arr_(" + carrIRtype + " " + v1s + ")");
            add(res + " = bitcast " + ty + " " + reg + " to " + POINTER);
            genPushCode(POINTER, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void arrayLength(String arrtype) {
        String ptr = allocReg();
        genPopCode(POINTER, ptr);
        String arr = allocReg();
        genCastCode(arr, POINTER, ptr, arrtype);

        String result = allocReg();
        String reg = allocReg();
        // out
        comment("arraylength " + arrtype);
        getelementptr(reg, arrtype, arr, 0, 0);
        load(result, INT, reg);
        genPushCode(INT, result);
    }


    public void _new(Resolver resolver, String name) {
        // state
        String objStruct = resolver.getIrStruct(name);
        String object = resolver.getIrType(name);
        String reg = allocReg();
        // out
        comment(resolver.getIrStruct(name));
        add("%__objptr" + tmp + " = getelementptr " + objStruct + ", " + objStruct + "* null, i32 1");
        add("%__memsize" + tmp + " = ptrtoint " + objStruct + "* %__objptr" + tmp + " to i32 ");
        add(reg + " = call i8* @malloc(i32 %__memsize" + tmp + ")");
        genPushCode(POINTER, reg);
        add(";call void @print_debug(i32 %__memsize" + tmp + ")");
    }

    public void neg(String type) {
        String v = allocReg();
        String res = allocReg();
        genPopCode(type, v);
        if (Internals.INT.equals(type) || Internals.LONG.equals(type)) {
            add(res + " = sub " + type + " 0, " + v);
        } else {
            add(res + " = fsub " + type + " 0.0, " + v);
        }
        genPushCode(type, res);
    }


    public void branch(Stack<String> commands, Label label, int op, int popCount) {
        if (commands.size() == 0) {
            if (popCount == 1) {
                String op1 = allocReg();
                genPopCode(INT, op1);
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + INT + " " + op1 + ", 0");
            } else {
                String op2 = allocReg();
                genPopCode(INT, op2);
                String op1 = allocReg();
                genPopCode(INT, op1);
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + INT + " " + op1 + ", " + op2);
            }
            add("br i1 %__tmpc" + tmp + ", label %" + label + ", label %_if.else" + tmp);
            add("_if.else" + tmp + ":");
            tmp++;
        } else {
            // POP prefix
            String cmd = commands.pop();
            if (Prefix.FCMPL.equals(cmd) || Prefix.FCMPG.equals(cmd)) {
                // double compare
                String op2 = allocReg();
                genPopCode(FLOAT, op2);
                String op1 = allocReg();
                genPopCode(FLOAT, op1);
                add("%__tmpc" + tmp + " = fcmp " + IR.FCMP[op] + " " + FLOAT + " " + op1 + ", " + op2); // ordered compare
            } else if (Prefix.DCMPL.equals(cmd) || Prefix.DCMPG.equals(cmd)) {
                // double compare
                String op2 = allocReg();
                genPopCode(DOUBLE, op2);
                String op1 = allocReg();
                genPopCode(DOUBLE, op1);
                add("%__tmpc" + tmp + " = fcmp " + IR.FCMP[op] + " " + DOUBLE + " " + op1 + ", " + op2); // ordered compare
            } else if (Prefix.LCMP.equals(cmd)) {
                // long compare
                String op2 = allocReg();
                genPopCode(LONG, op2);
                String op1 = allocReg();
                genPopCode(LONG, op1);
                add("%__tmpc" + tmp + " = icmp " + IR.ICMP[op] + " " + LONG + " " + op1 + ", " + op2);
            } else {
                System.err.println("Unknown prefix: " + cmd);
            }
            add("br i1 %__tmpc" + tmp + ", label %" + label + ", label %_if.else" + tmp);
            add("_if.else" + tmp + ":");
            tmp++;
        }
    }

    // todo split primitive and objects
    public void newArray(Resolver resolver, String javaArrayType) {
        String length = allocReg();
        genPopCode(INT, length);
        comment("new array " + javaArrayType + " size: " + length);
        String ty = Util.javaSignature2irType(resolver, javaArrayType);
        String reg1 = allocReg();
        String reg2 = allocReg();//for arrlength
        String reg3 = allocReg();
        // size array in bytes
        int bytes = Internals.sizeOf(javaArrayType);
        if (bytes == 0) { //object
            String reg4 = allocReg();
            add(reg4 + " = call i32 @ptr_size()");
            add(reg1 + " = mul i32" + length + ", " + reg4);
        } else {
            add(reg1 + " = mul i32" + length + ", " + bytes);
        }
        add(reg2 + " = add i32 " + reg1 + ", 4");
        add(reg3 + " = call i8* @malloc(i32 " + reg2 + ")");
        genPushCode(POINTER, reg3);
        String parent = Util.javaSignature2irType(resolver, "[" + javaArrayType);
        String arr = allocReg();
        genCastCode(arr, POINTER, reg3, parent);

        //save array length to struct first  {i32,[0 x type]}
        String reg5 = allocReg();
        getelementptr(reg5, parent, arr, 0, 0);
        store(INT, length, reg5);
    }

    public void putfield(Resolver resolver, String className, String name, String signature) {
        // state
        String ty = Util.javaSignature2irType(resolver, signature);
        String value = allocReg();
        genPopCode(ty, value);
        String classIr = Util.javaSignature2irType(resolver, "L" + className + ";");
        String inst = allocReg();
        genPopCode(classIr, inst);
        String sp = allocReg();
        // out
        comment("putfield " + className + " " + name + " " + signature + " ( " + classIr + " := " + ty + " )");
        getelementptr(sp, classIr, inst, 0, Util.fieldIndexInClass(className, name));
        store(ty, value, sp);
    }

    public void putstatic(Resolver resolver, String className, String name, String signature) {
        // state
        String ty = Util.javaSignature2irType(resolver, signature);
        String value = allocReg();
        genPopCode(ty, value);
        String sp = allocReg();
        // out
        comment("putstatic " + className + " " + name + " " + signature + " ( " + signature + " := " + ty + " )");
        getelementptr(sp, ty + "*", Util.static2str(className, name));
        store(ty, value, sp);
    }


    public void getfield(Resolver resolver, String className, String name, String signature) {
        // state
        String classIr = Util.javaSignature2irType(resolver, "L" + className + ";");
        String inst = allocReg();
        genPopCode(classIr, inst);
        String ty = Util.javaSignature2irType(resolver, signature);
        String sp = allocReg();

        if (signature.equals("[C")) {
            int debug = 1;
        }
        // out
        comment("getfield " + className + " " + name + " " + signature + " ( " + ty + " )");
        getelementptr(sp, classIr, inst, 0, Util.fieldIndexInClass(className, name));
        String result = allocReg();
        load(result, ty, sp);
        genPushCode(ty, result);
    }

    public void getstatic(Resolver resolver, String className, String name, String signature) {
        // state
        String ty = Util.javaSignature2irType(resolver, signature);
        String result = allocReg();
        String sp = allocReg();
        // out
        comment("getstatic " + className + " " + name + " " + signature + " ( " + result + " := " + signature + " )");
        getelementptr(sp, ty + "*", Util.static2str(className, name));
        load(result, ty, sp);
        genPushCode(ty, result);
    }

    public String getSignatureCall(String className, String methodName, JSignature sig, String prefix) {
        StringJoiner joiner = new StringJoiner(", ", sig.getResult() + " @" + sig.getID(className, methodName) + "(", ")");
        if (prefix != null) joiner.add(prefix);
        List<String> pops = new ArrayList<String>();
        for (int i = sig.getArgs().size() - 1; i >= 0; i--) {
            String reg = allocReg();
            String type = sig.getArgs().get(i);
            genPopCode(type, reg);
            pops.add(0, reg);
        }
        for (int i = 0; i < sig.getArgs().size(); i++) {
            String arg = sig.getArgs().get(i);
            joiner.add(arg + " " + pops.get(i));
        }
        return joiner.toString();
    }

    public void arrstore(Resolver resolver, String javatype) {
        String resultType = Util.javaSignature2irType(resolver, javatype);
        String parent = Util.javaSignature2irType(resolver, "[" + javatype);
        comment(resultType + "astore ");
        // state
        String value = allocReg();
        genPopCode(resultType, value);
        String index = allocReg();
        genPopCode(INT, index);
        String arrayRef = allocReg();
        genPopCode(POINTER, arrayRef);
        String arrayRefCast = allocReg();
        genCastCode(arrayRefCast, POINTER, arrayRef, parent);
        String elemPtr = allocReg();
        // out
        comment(parent + "aload ");
        getelementptr(elemPtr, parent, arrayRefCast, 0, 1, "i32 " + index); // pointer to element of array
        store(resultType, value, elemPtr);
    }

    public void arrload(Resolver resolver, String javatype) {
        String resultType = Util.javaSignature2irType(resolver, javatype);
        String parentType = Util.javaSignature2irType(resolver, "[" + javatype);
        comment(resultType + "aload ");
        // state
        String index = allocReg();
        genPopCode(INT, index);
        String arrayRef = allocReg();
        genPopCode(POINTER, arrayRef);
        String arrayRefCast = allocReg();
        genCastCode(arrayRefCast, POINTER, arrayRef, parentType);
        String elemPtr = allocReg();
        // out
//        String resultType = Util.detype(javatype);
        getelementptr(elemPtr, parentType, arrayRefCast, 0, 1, "i32 " + index); // pointer to element of array
        String result = allocReg();
        load(result, resultType, elemPtr);
        if (Util.isPtr(resultType)) {
            String resultCast = allocReg();
            genCastCode(resultCast, resultType, result, POINTER);
            genPushCode(POINTER, resultCast);
        } else {
            genPushCode(resultType, result);
        }
    }


    public void fptosi(String type1, String type2) {
        operationto("fptosi", type1, type2);
    }

    public void sitofp(String type1, String type2) {
        operationto("sitofp", type1, type2);
    }


    public void operationto(String op, String type1, String type2) {
        String v1 = allocReg();
        genPopCode(type1, v1);
        String result = allocReg();
        add(result + " = " + op + " " + type1 + " " + v1 + " to " + type2);
        genPushCode(type2, result);

    }

    public void genCastCode(String to, String ir1, String from, String ir2) {

        if (!ir2.equals(ir1)) {
            if (ir1.equals(LONG)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  long  to float");
                } else {
                    add(to + " = trunc " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(INT)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  int  to float");
                } else if (ir2.equals(LONG)) {
                    add(to + " = sext " + ir1 + " " + from + " to " + ir2);
                } else {
                    add(to + " = trunc " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(BOOLEAN)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  boolean  to float");
                } else {
                    add(to + " = sext " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(BYTE)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  byte  to float");
                } else if (ir1.equals(BOOLEAN)) {
                    add(to + " = trunc " + ir1 + " " + from + " to " + ir2);
                } else {
                    add(to + " = sext " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(CHAR)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  char  to float");
                } else if (ir1.equals(BOOLEAN) || ir1.equals(BYTE)) {
                    add(to + " = trunc " + ir1 + " " + from + " to " + ir2);
                } else {
                    add(to + " = zext " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(SHORT)) {
                if (ir2.equals(FLOAT) || ir2.equals(DOUBLE)) {
                    throw new RuntimeException("  short  to float");
                } else if (ir1.equals(BOOLEAN) || ir1.equals(BYTE)) {
                    add(to + " = trunc " + ir1 + " " + from + " to " + ir2);
                } else {
                    add(to + " = sext " + ir1 + " " + from + " to " + ir2);
                }
            } else if (ir1.equals(FLOAT)) {
                if (ir2.equals(DOUBLE)) {
                    add(to + " = fpext " + ir1 + " " + from + " to " + ir2);
                } else {
                    throw new RuntimeException(" float to " + ir2);
                }
            } else if (ir1.equals(DOUBLE)) {
                if (ir2.equals(FLOAT)) {
                    add(to + " = fptrunc " + ir1 + " " + from + " to " + ir2);
                } else {
                    throw new RuntimeException(" double to " + ir2);
                }
            } else {
                add(to + " = bitcast " + ir1 + " " + from + " to " + ir2);
            }
        }
    }

    public void genPushCode(String type, String value) {
        String funcName = type;
        if (Util.isPtr(type)) {
            funcName = "ptr";
            if (!type.equals(POINTER)) {
                String cast = allocReg();
                genCastCode(cast, type, value, POINTER);
                value = cast;
            }
            type = POINTER;
        } else if (type.equals(BOOLEAN)) {
            funcName = INT;
            String cast = allocReg();
            genCastCode(cast, type, value, INT);
            type = INT;
            value = cast;
        }
        add("call void @push_" + funcName + "(%struct.StackFrame* %thrd_stack_reg,  " + type + " " + value + ")");
    }

    public void genPopCode(String type, String result) {
        if (Util.isPtr(type)) {
            String resultcast = result;
            if (!type.equals(POINTER)) {
                resultcast = allocReg();
            }
            add(resultcast + " = call " + POINTER + " @pop_ptr(%struct.StackFrame* %thrd_stack_reg)");
            if (!type.equals(POINTER)) {
                genCastCode(result, POINTER, resultcast, type);
            }
        } else if (type.equals(BOOLEAN)) {
            String resultcast = allocReg();
            add(resultcast + " = call " + INT + " @pop_" + INT + "(%struct.StackFrame* %thrd_stack_reg)");
            genCastCode(result, INT, resultcast, type);
        } else {
            add(result + " = call " + type + " @pop_" + type + "(%struct.StackFrame* %thrd_stack_reg)");
        }
    }

    public void genLoadLocalVarCode(String type, int slot) {
        String funcName = type;
        if (Util.isPtr(type)) {
            funcName = "ptr";
            type = POINTER;
        } else if (type.equals(BOOLEAN)) {
            funcName = INT;
            type = INT;
        }
        String value = allocReg();
        add(value + " = call " + type + " @localvar_get_" + funcName + "(%union.StackEntry* %local_var, i32 " + slot + ")");
        genPushCode(type, value);
    }

    public void genStoreLocalVarCode(String type, int slot) {
        String funcName = type;
        if (Util.isPtr(type)) {
            funcName = "ptr";
            type = POINTER;
        } else if (type.equals(BOOLEAN)) {
            funcName = INT;
            type = INT;
        }
        String result = allocReg();
        genPopCode(type, result);
        add("call void @localvar_set_" + funcName + "(%union.StackEntry* %local_var, i32 " + slot + ", " + type + " " + result + ")");
    }

    public void genGetLocalVarCode(String type, int slot, String result) {
        if (Util.isPtr(type)) {
            String resultcast = result;
            if (!type.equals(POINTER)) {
                resultcast = allocReg();
            }
            add(resultcast + " = call " + POINTER + " @localvar_get_ptr(%union.StackEntry* %local_var, i32 " + slot + ")");
            if (!type.equals(POINTER)) {
                genCastCode(result, POINTER, resultcast, type);
            }
        } else if (type.equals(BOOLEAN)) {
            String resultcast = allocReg();
            add(resultcast + " = call " + INT + " @localvar_get_" + INT + "(%union.StackEntry* %local_var, i32 " + slot + ")");
            genCastCode(result, INT, resultcast, BOOLEAN);
        } else {
            add(result + " = call " + type + " @localvar_get_" + type + "(%union.StackEntry* %local_var, i32 " + slot + ")");
        }
    }

    public void genSetLocalVarCode(String type, int slot, String value) {
        String funcName = type;
        if (Util.isPtr(type)) {
            funcName = "ptr";
            if (!type.equals(POINTER)) {
                String cast = allocReg();
                genCastCode(cast, type, value, POINTER);
                value = cast;
            }
            type = POINTER;
        } else if (type.equals(BOOLEAN)) {
            funcName = INT;
            String cast = allocReg();
            genCastCode(cast, type, value, INT);
            type = INT;
            value = cast;
        }
        add("call void @localvar_set_" + funcName + "(%union.StackEntry* %local_var, i32 " + slot + ", " + type + " " + value + ")");
    }

    public void genRestoreStackCode() {
        add("call void @set_sp(%struct.StackFrame* %thrd_stack_reg, %union.StackEntry* %local_var)");
    }
    // =================================================================================================================

    public String allocReg() {
        String result = "%__tmp" + tmp;
        tmp++;
        return result;
    }

    // <result> = getelementptr <pty>* <ptrval>{, <ty> <idx>}*
    public void getelementptr(String result, String ptrType, String ptrName, Object... idx) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(result);
        tmp.append(" = getelementptr ");
        tmp.append(Util.detype(ptrType));
        tmp.append(", ");
        tmp.append(ptrType);
        tmp.append(" ");
        tmp.append(ptrName);
        for (Object id : idx) {
            if (id instanceof Integer) {
                tmp.append(", i32 ");
            } else if (id instanceof Long) {
                tmp.append(", i64 ");
            } else {
                tmp.append(", ");
            }
            tmp.append(id);
        }
        add(tmp.toString());
    }


    // <result> = load [volatile] <ty>* <pointer>
    public void load(String to, String ty, String from) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(to);
        tmp.append(" = load ");
        tmp.append(ty);
        tmp.append(", ");
        tmp.append(ty);
        tmp.append("* ");
        tmp.append(from);
        add(tmp.toString());
    }
//


    // store [volatile] <ty> <value>, <ty>* <pointer>
    public void store(String ty, String from, String to) {

        StringBuilder tmp = new StringBuilder();
        tmp.append("store ");
        tmp.append(ty);
        tmp.append(" ");
        tmp.append(from);
        tmp.append(", ");
        tmp.append(ty);
        tmp.append("* ");
        tmp.append(to);
        add(tmp.toString());
    }


    public List<String> getStrings() {
        return strings;
    }
}
