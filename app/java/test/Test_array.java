package test;

/**
 * arrays test
 */
public class Test_array {

    public static void set(int[] a, int index, int value) {
        a[index] = value;
    }

    public static int get(int[] a, int index) {
        return a[index];
    }

    public static int get(int[][] a, int index0, int index1) {
        return a[index0][index1];
    }

    public static int[] aa(int[][] a) {
        return a[10];
    }
}
