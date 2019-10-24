package test;

/**
 * int primitive test
 */
public class Test_int {

    public static int add(int a, int b) {
        return a + b;
    }

    public static int sub(int a, int b) {
        return a - b;
    }

    public static int mul(int a, int b) {
        return a * b;
    }

    public static int div(int a, int b) {
        return a / b;
    }

    public static int rem(int a, int b) {
        return a % b;
    }

    public static int neg(int a) {
        return -a;
    }
    
    public static int mix(int a, int b, int c, int d, int e) {
        return (((a + b) - c) * d ) / e;
    }

}
