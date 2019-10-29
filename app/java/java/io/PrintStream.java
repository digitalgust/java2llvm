package java.io;

public class PrintStream {

    native public void println(int v);

    native public void println(long v);

    native public void print(char v);

    native public void println(double v);

    public void println(){
        print('\n');
    }

    public void print(String s) {
        if (s == null) return;
        for (int i = 0; i < s.length(); i++) {
            print(s.charAt(i));
        }
    }

    public void println(String s) {
        if (s == null) return;
        for (int i = 0; i < s.length(); i++) {
            print(s.charAt(i));
        }
        print('\n');
    }
//
//    native public void println_J(long v);
}
