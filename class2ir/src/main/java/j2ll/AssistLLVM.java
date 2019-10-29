package j2ll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class AssistLLVM {
    static final String PRE_DEF_TYPE =
            "%struct.StackFrame = type { %union.StackEntry*, %union.StackEntry*, i32 }\n" +
            "%union.StackEntry = type { i64 }\n";

    static final String FUNC_DECLARES = "declare noalias i8* @malloc(i32)\n" +
            "declare void @free(i8*)\n" +
            "declare void @print_debug(i32)\n" +
            "declare void @print_ptr(i8*)\n" +
            "declare i32 @ptr_size()\n" +
            "declare %union.StackEntry* @get_sp(%struct.StackFrame*)\n" +
            "declare void @set_sp(%struct.StackFrame*, %union.StackEntry*) \n" +
            "declare void @chg_sp(%struct.StackFrame*, i32)\n" +
            "declare %union.StackEntry* @get_store(%struct.StackFrame*)\n" +
            "declare void @push_i64(%struct.StackFrame*, i64)\n" +
            "declare i64 @pop_i64(%struct.StackFrame*)\n" +
            "declare void @push_i32(%struct.StackFrame*, i32) \n" +
            "declare i32 @pop_i32(%struct.StackFrame*)\n" +
            "declare void @push_i16(%struct.StackFrame*, i16 signext)\n" +
            "declare signext i16 @pop_i16(%struct.StackFrame*)\n" +
            "declare void @push_u16(%struct.StackFrame*, i16 zeroext)\n" +
            "declare zeroext i16 @pop_u16(%struct.StackFrame*)\n" +
            "declare void @push_i8(%struct.StackFrame*, i8 signext)\n" +
            "declare signext i8 @pop_i8(%struct.StackFrame*)\n" +
            "declare void @push_double(%struct.StackFrame*, double)\n" +
            "declare double @pop_double(%struct.StackFrame*)\n" +
            "declare void @push_float(%struct.StackFrame*, float)\n" +
            "declare float @pop_float(%struct.StackFrame*)\n" +
            "declare void @push_ptr(%struct.StackFrame*, i8*)\n" +
            "declare i8* @pop_ptr(%struct.StackFrame*)\n" +
            "declare void @push_entry(%struct.StackFrame*, i64)\n" +
            "declare i64 @pop_entry(%struct.StackFrame*)\n" +
            "declare void @localvar_set_i64(%union.StackEntry*, i32, i64)\n" +
            "declare i64 @localvar_get_i64(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_i32(%union.StackEntry*, i32, i32)\n" +
            "declare i32 @localvar_get_i32(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_i16(%union.StackEntry*, i32, i16 signext)\n" +
            "declare signext i16 @localvar_get_i16(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_u16(%union.StackEntry*, i32, i16 zeroext)\n" +
            "declare zeroext i16 @localvar_get_u16(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_i8(%union.StackEntry*, i32, i8 signext)\n" +
            "declare signext i8 @localvar_get_i8(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_double(%union.StackEntry*, i32, double)\n" +
            "declare double @localvar_get_double(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_float(%union.StackEntry*, i32, float)\n" +
            "declare float @localvar_get_float(%union.StackEntry*, i32)\n" +
            "declare void @localvar_set_ptr(%union.StackEntry*, i32, i8*)\n" +
            "declare i8* @localvar_get_ptr(%union.StackEntry*, i32)\n" +
            "\n" +
            "\n" +
            "\n" +
            "";

    static final String GLOBAL_VAR_DECLARE ="" +
            "@pthd_stack = external global %struct.StackFrame*\n" +
            "\n" +
            "\n";

    static final String CHAR_ARR = "[C";

    static Resolver resolver = new Resolver();
    static List<String> clinitMethods = new ArrayList<>();

    static public void genClinits(String outpath) {
        try {
            File f = new File(outpath + "/clinits.ll");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);

            //declare
            ps.println(getAssistFuncDeclare());
            String carrIRtype = Util.javaSignature2irType(resolver, CHAR_ARR);
            ps.println("declare void @java_lang_String__init___C(%java_lang_String* %s0, " + carrIRtype + " %s1)");
            for (String s : clinitMethods) {
                if (s != null) ps.println("declare " + s);
            }
            ps.println("\n");

            ps.println(Util.class2struct(resolver, "java/lang/String"));
            ps.println(Util.class2struct(resolver, CHAR_ARR));
            ps.println(PRE_DEF_TYPE);
            ps.println("\n");

            //call
            ps.println("define void @classes_clinit(){");
            for (String s : clinitMethods) {
                if (s != null) ps.println("    call " + s);
            }
            ps.println("    ret void");
            ps.println("}");
            ps.println("\n");

            //var
            ps.println(GLOBAL_VAR_DECLARE);

            //define
            String funcName = getConstStringFuncName();
            String extFunc = "define " + funcName + "  {\n"
                    + "    %tmps0 = alloca " + carrIRtype + "\n"
                    + "    store " + carrIRtype + " %s0, " + carrIRtype + "* %tmps0\n"
                    + "    %stack0 = load " + carrIRtype + ", " + carrIRtype + "* %tmps0             \n"
                    + "    ; new %java_lang_String\n"
                    + "    %__objptr = getelementptr %java_lang_String, %java_lang_String* null, i32 1\n"
                    + "    %__memsize = ptrtoint %java_lang_String* %__objptr to i32 \n"
                    + "    ;call void @print_debug(i32 %__memsize) \n"
                    + "    %__tmp0 = call i8* @malloc(i32 %__memsize)\n"
                    + "    %stack1 = bitcast i8* %__tmp0 to %java_lang_String*\n"
                    + "    ;              \n"
                    + "    call void @java_lang_String__init___C(%java_lang_String* %stack1, " + carrIRtype + " %stack0) \n"
                    + "    ret %java_lang_String* %stack1\n"
                    + "}                  \n";
            ps.println(extFunc);
            ps.println("\n");

            ps.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("generate clinits.ll error");
        }
    }

    static public void addClinit(String s) {
        clinitMethods.add(s);
    }


    static public String getConstStringFuncName() {
        String ty = Util.javaSignature2irType(resolver, "Ljava/lang/String;"); //for add class java.lang.String declare
        String carrIRtype = Util.javaSignature2irType(resolver, CHAR_ARR);
        String funcName = ty + " @construct_string_with_char_arr_(" + carrIRtype + " %s0)";
        return funcName;
    }

    static public String getAssistFuncDeclare() {
        return FUNC_DECLARES;
    }
}
