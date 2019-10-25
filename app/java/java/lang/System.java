package java.lang;

import java.io.PrintStream;

public class System {
    static public PrintStream out;

    static public native long currentTimeMillis();

    static public native long nanoTime();

    static public void arraycopy(char[] src, int srcPos, char[] dest, int destPos, int arr_length) {
        if(src==null||dest==null){
            return;
        }
        char[] a = src;
        char[] b = dest;
        if (a.length >= srcPos + arr_length && b.length >= destPos + arr_length) {
            for (int i = 0; i < arr_length; i++) {
                b[destPos + i] = a[srcPos + i];
            }
        }
    }

}
