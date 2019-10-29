package j2ll;

import classparser.ClassFile;
import classparser.ClassHelper;
import classparser.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static j2ll.Internals.*;


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
            return javaArr2irType(resolver, str);
        }
        //return null;
        throw new RuntimeException(str);
    }

    static final String ARRAY_PREFIX = "{i32, [0 x ";
    static final String ARRAY_APPENDIX = "]}";

    public static String detype(String type) {
        try {
            if (type.endsWith("*")) {
                return type.substring(0, type.length() - 1);
            }else if(type.startsWith("%..")){
                String s = "%"+type.substring(2)+"*";
                return s;
            } else {
                throw new RuntimeException("detype error:"+type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return type;
        }
    }

    public static String javaArr2irType(Resolver resolver, String signature) {
        resolver.resolve(signature);
        String next = "" + signature.substring(1);
        if (next.startsWith("[")) javaArr2irType(resolver, next);//recur resolve

        String s = "%" + signature.replace("[", ".") + "*";
        s = s.replaceAll("[\\/\\;]", "_");
        return s;
    }

    public static String javaArr2struct(Resolver resolver, String signature) {
        resolver.resolve(signature);
        String next = "" + signature.substring(1);
        char nc = signature.charAt(1);
        switch (nc) {
            case 'Z':
                return ARRAY_PREFIX + BYTE + ARRAY_APPENDIX;
            case 'B':
                return ARRAY_PREFIX + BYTE + ARRAY_APPENDIX;
            case 'S':
                return ARRAY_PREFIX + SHORT + ARRAY_APPENDIX;
            case 'I':
                return ARRAY_PREFIX + INT + ARRAY_APPENDIX;
            case 'J':
                return ARRAY_PREFIX + LONG + ARRAY_APPENDIX;
            case 'C':
                return ARRAY_PREFIX + CHAR + ARRAY_APPENDIX;
            case 'F':
                return ARRAY_PREFIX + FLOAT + ARRAY_APPENDIX;
            case 'D':
                return ARRAY_PREFIX + DOUBLE + ARRAY_APPENDIX;
            case 'L':
            case '[':
                return ARRAY_PREFIX + javaSignature2irType(resolver, next) + ARRAY_APPENDIX;
        }
        throw new RuntimeException(signature);
    }


    public static List<String> javaSignatures2irTypes(Resolver resolver, String str) {
        //System.out.print("Parse ");
        //System.out.println("signatures: \"" + str + "\"");

        List<String> result = new ArrayList<>();
        String sa = str;
        while (sa.length() > 0) {
            char c = sa.charAt(0);
            if (c == 'S' || c == 'B' || c == 'C' || c == 'I' || c == 'J' || c == 'F' || c == 'D') {
                String tmp = sa.substring(0, 1);
                result.add(javaSignature2irType(resolver, tmp));
                sa = sa.substring(1);
            } else if (c == 'L') {
                int pos = sa.indexOf(';');
                String tmp = sa.substring(0, pos + 1);
                result.add(javaSignature2irType(resolver, tmp));
                sa = sa.substring(pos + 1);
            } else { //'['

                String tmp = "";
                //find first not '['
                for (int i = 0; i < sa.length(); i++) {
                    c = sa.charAt(i);
                    tmp += c;
                    if (sa.charAt(i) != '[') {
                        break;
                    }
                }
                if (c == 'L') {
                    int pos = sa.indexOf(';');
                    tmp = sa.substring(0, pos + 1);
                }
                result.add(javaSignature2irType(resolver, tmp));
                sa = sa.substring(tmp.length());
            }
        }



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
            if (className.startsWith("[")) {
                String s = javaArr2irType(resolver, className).replace("*", "");
                s += " = type " + javaArr2struct(resolver, className);
                return s;
            } else {
                ClassFile c = helper.getClassFile(className);
//                if (c == null) {
//                    int debug = 1;
//                }
                StringJoiner joiner = new StringJoiner(", ", "{", "}");
                for (Field f : c.getFields()) {
                    joiner.add(javaSignature2irType(resolver, f.getDescription()));
                }
                String s = "%" + className.replace('/', '_') + " = type " + joiner; //todo struct -> internals
                //System.out.println("class2struct:" + s);
                return s;
            }
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


    public static int fieldIndexInClass(String className, String name) {
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
