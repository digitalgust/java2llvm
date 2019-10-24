package test;

/**
 * double primitive test
 */
public class Test_double {

    public static double add(double a, double b) {
        return a + b;
    }

    public static double sub(double a, double b) {
        return a - b;
    }

    public static double mul(double a, double b) {
        return a * b;
    }

    public static double div(double a, double b) {
        return a / b;
    }

    public static double neg(double a) {
        return -a;
    }

    // this function is not recommended
    public static double rem(double a, double b) {
        return a % b;
    }
}
