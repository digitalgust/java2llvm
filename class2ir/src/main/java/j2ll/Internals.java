package j2ll;

/**
 * Internal Functions
 */
public class Internals {

    public static final String BOOLEAN = "i1";
    public static final String BYTE = "i8"; //todo
    public static final String CHAR = "i16"; // todo
    public static final String SHORT = "i16"; // todo
    public static final String INT = "i32";
    public static final String LONG = "i64";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String POINTER = "i32*";

    public static int sizeOf(String javaType) {
        if ("Z".equals(javaType)) return 1;
        if ("B".equals(javaType)) return 1;
        if ("C".equals(javaType)) return 2;
        if ("S".equals(javaType)) return 2;
        if ("I".equals(javaType)) return 4;
        if ("J".equals(javaType)) return 8;
        if ("F".equals(javaType)) return 4;
        if ("D".equals(javaType)) return 8;
        return 0;
    }

    public static String javacode2javatag(int type) {
        switch (type) {
            case 4:
                return "[Z";
            case 5:
                return "[C";
            case 6:
                return "[F";
            case 7:
                return "[D";
            case 8:
                return "[B";
            case 9:
                return "[S";
            case 10:
                return "[I";
            case 11:
                return "[J";
        }
        return null;
    }

    public static String arrayOf(String type) {
        return "{i32, [0 x " + type + "]}*";
    }

    public static String dearrayOf(String type) {
        try {
            return type.substring(11, type.length() - 3);
        } catch (Exception e) {
            e.printStackTrace();
            return type;
        }
    }



    public static String java2ir(Resolver resolver, Class c) {
        if ("boolean".equals(c.getName())) return BOOLEAN;
        if ("byte".equals(c.getName())) return BYTE;
        if ("short".equals(c.getName())) return SHORT;
        if ("int".equals(c.getName())) return INT;
        if ("long".equals(c.getName())) return LONG;
        if ("char".equals(c.getName())) return CHAR;
        if ("float".equals(c.getName())) return FLOAT;
        if ("double".equals(c.getName())) return DOUBLE;

        return resolver.resolve(c.getName().replace('.', '/'));
    }


    public static String typetag2ir(Resolver resolver, String tag) {
        if ("Z".equals(tag)) return BOOLEAN;
        if ("B".equals(tag)) return BYTE;
        if ("S".equals(tag)) return SHORT;
        if ("I".equals(tag)) return INT;
        if ("J".equals(tag)) return LONG;
        if ("C".equals(tag)) return CHAR;
        if ("F".equals(tag)) return FLOAT;
        if ("D".equals(tag)) return DOUBLE;
        if (tag.startsWith("[")) {
            String next = "" + tag.substring(1);
            char nc = tag.charAt(1);
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
                throw new RuntimeException(tag);
            }
            else {
                return typetag2ir(resolver, next) + "*";
            }
        }

        String objtag = tag.substring(1, tag.length() - 1);
        return resolver.resolve(objtag.replace('.', '/'));
    }

}
