package j2ll;

/**
 *
 */
public final class IR {

    public static final int EQ = 0; // equals
    public static final int NE = 1; // not equals
    public static final int LT = 2; // less than
    public static final int GE = 3; // greater equals
    public static final int GT = 4; // greater than
    public static final int LE = 5; // less equals

    public static String[] ICMP = {"eq", "ne", "slt", "sge", "sgt", "sle"}; // integer signed compare
    public static String[] FCMP = {"oeq", "one", "olt", "oge", "ogt", "ole"}; // float ordered compare (need check)

}
