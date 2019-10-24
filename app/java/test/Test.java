package test;


public class Test extends TestParent {

    boolean bl;
    byte bt;
    short sh;
    int in;
    long ln;
    float ft;
    double db;

    static long sl = 1111;
    static Test singleton = new Test();

    public Test() {
        this.bl = true;
        this.bt = 127;
        this.sh = 100;
        this.in = 142;
        this.ln = 42L;
        this.ft = 123.33f;
        this.db = 0.53;

        System.out.println(this.in);
    }

    @Override
    public int length() {
        return super.length();
    }

    public static void main() {
        long a = System.currentTimeMillis();
        System.out.println(a);
        System.out.println("Hello world");

        Test test = new Test();
        test.in = 9;
        System.out.println(test.in);

/*
        double v[] = new double[10000];
        int ii[] = new int[123];
        for (int i = 0; i < 10000; i++) v[i] = i * 1.2d;

        switch (test.in) {
            case 7:
            case 9:
            case 10: System.out.println(10);
                break;
            case 11: System.out.println(7);
                break;
            //case 16:
            case 18: System.out.println(5);
                break;
            default:
                System.out.println(5888);
        }
*/


        System.out.println(System.currentTimeMillis() - a);

        System.out.println(sl);
//        System.out.println(ii[6]);
        System.out.println(singleton.ln);


        //double d[][] = new double[101][201];
    }

    public void t1() {
        int a, b, c, d;
        a = 3;
        b = 4;
        c = a * 2 + b * b;
        d = 0;

        if (c > 0) {
            a = b > 3 ? 2 : 1;
        } else {
            if (b > 3) {
                while (d < a) {
                    if(d==a-1){
                        c++;
                    }
                    c++;
                    d++;
                }
                a = 100000;
            } else {
                a = 200000;
            }
        }
        d = a + c;

    }
}
