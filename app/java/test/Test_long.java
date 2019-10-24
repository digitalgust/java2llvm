package test;

/**
 * long primitive test
 */
public class Test_long {
    
    public static long add(long a, long b) {
        return a + b;
    }

    public static long sub(long a, long b) {
        return a - b;
    }

    public static long mul(long a, long b) {
        return a * b;
    }

    public static long div(long a, long b) {
        return a / b;
    }

    public static long rem(long a, long b) {
        return a % b;
    }

    public static long neg(long a) {
        return -a;
    }

    public static long mix(long a, long b, long c, long d, long e) {
        return (((a + b) - c) * d ) / e;
    }
    
}
