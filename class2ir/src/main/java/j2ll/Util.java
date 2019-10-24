package j2ll;

import classparser.ClassFile;
import classparser.ClassHelper;
import classparser.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static j2ll.Internals.*;

//import java.lang.reflect.Field;


public final class Util {
    static public ClassHelper helper;

    public static String javaSignature2irType(Resolver resolver, String str) {

        if (str.equals("B")) {
            return BYTE;
        } else if (str.equals("S")) {
            return SHORT;
        } else if (str.equals("C")) {
            return CHAR;
        } else if (str.equals("I")) {
            return INT;
        } else if (str.equals("J")) {
            return LONG;
        } else if (str.equals("F")) {
            return FLOAT;
        } else if (str.equals("D")) {
            return DOUBLE;
        } else if (str.equals("V")) {
            return "void";
        } else if (str.equals("Z")) {
            return BOOLEAN;
        } else if (str.startsWith("L")) {
            str = str.substring(1, str.length() - 1);
            return resolver.resolve(str);
        } else if (str.startsWith("[")) {
            String next = "" + str.substring(1);
            char nc = str.charAt(1);
            if (nc != '[') {
                switch (nc) {
                    case 'Z':
                        return "{i32, [0 x i8]}*";
                    case 'B':
                        return "{i32, [0 x i8]}*";
                    case 'S':
                        return "{i32, [0 x i16]*";
                    case 'I':
                        return "{i32, [0 x i32]*";
                    case 'J':
                        return "{i32, [0 x i64]}*";
                    case 'C':
                        return "{i32, [0 x i16]}*";
                    case 'F':
                        return "{i32, [0 x float]}*";
                    case 'D':
                        return "{i32, [0 x double]}*";
                    case 'L':
                        return "{i32, [0 x " + typetag2ir(resolver, next) + "*]}*";
                }
            }
        }
        //return null;
        throw new RuntimeException(str);
    }

    public static List<String> javaSignatures2irTypes(Resolver resolver, String str) {
        //System.out.print("Parse ");
        //System.out.println("signatures: \"" + str + "\"");

        List<String> result = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        char[] carr = str.toCharArray();
        for (int i = 0; i < carr.length; i++) {
            char c = carr[i];
            tmp.append(c);
            if (c == 'S' || c == 'C' || c == 'I' || c == 'J' || c == 'F' || c == 'D') { // todo all java signs
                result.add(javaSignature2irType(resolver, tmp.toString()));
                tmp.setLength(0);
            } else if (c == 'L') {
                String s = "";
                for (; ; i++) {
                    c = carr[i];
                    s += c;
                    if (c == ';') {
                        break;
                    }
                }
                result.add(javaSignature2irType(resolver, s));
                tmp.setLength(0);
            }
        }
        if (tmp.length() > 0) result.add(javaSignature2irType(resolver, tmp.toString()));
        return result;
    }

    public static String enumArgs(List<String> types, String prefix) {
        StringBuilder tmp = new StringBuilder();
        int index = 0;
        for (int i = 0; i < types.size(); i++) {
            if (i != 0) tmp.append(", ");
            String type = types.get(i);
            tmp.append(type);
            tmp.append(" ");
            tmp.append(prefix);
            tmp.append(index++);
            if ("i64".equals(type) || "double".equals(type)) index++; // long & double have 2 slots
        }
        return tmp.toString();
    }

    public static String classMethodSignature2id(String className, String methodName, JSignature signature) {
        StringBuilder result = new StringBuilder();
        String nm = methodName.replace('<', '_').replace('>', '_');
        //result.append('"');
        result.append(className.replace('/', '_'));
        result.append('_');
        result.append(nm);
        if (!signature.getJavaArgs().isEmpty()) {
            result.append('_');
            result.append(signature.getJavaArgs());
        }
        //result.append('"');
        String s = result.toString();
        s = s.replaceAll("[\\/\\;\\[]", "_");
        return s;
    }

    public static String class2struct(Resolver resolver, String className) {
        try {
            ClassFile c = helper.getClassFile(className);
            StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (Field f : c.getFields()) {
                joiner.add(Internals.typetag2ir(resolver, f.getDescription()));
            }
            String s = "%" + className.replace('/', '_') + " = type " + joiner; //todo struct -> internals
            //System.out.println("class2struct:" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return className + " is unknown";
        }
    }


    public static String class2irType(Resolver resolver, String className) {
        try {
            String cn = "L" + className.replace('.', '/') + ";";
            return javaSignature2irType(resolver, cn);
        } catch (Exception e) {
            e.printStackTrace();
            return className + " is unknown";
        }
    }


    public static int class2ptr(String className, String name) {
        try {
            ClassFile c = helper.getClassFile(className);
            int pos = 0;
            for (Field f : c.getFields()) {
                if (name.equals(f.getFieldName())) return pos;
                pos++;
            }
            return pos;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static String static2str(String className, String name) {
        String cn = className.replace('/', '_');
        String nm = name.replace('<', '_').replace('>', '_'); //todo
        return "@" + cn + "_" + nm + "";
    }

}