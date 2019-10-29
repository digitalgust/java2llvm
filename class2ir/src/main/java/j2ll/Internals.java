package j2ll;

/**
 * Internal Functions
 */
public class Internals {

    public static final String BOOLEAN = "i1";
    public static final String BYTE = "i8";
    public static final String CHAR = "i16";
    public static final String SHORT = "i16";
    public static final String INT = "i32";
    public static final String LONG = "i64";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String POINTER = "i8*";
    public static final String SLOT2 = "SLOT2";

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
                return "Z";
            case 5:
                return "C";
            case 6:
                return "F";
            case 7:
                return "D";
            case 8:
                return "B";
            case 9:
                return "S";
            case 10:
                return "I";
            case 11:
                return "J";
        }
        return null;
    }





}
