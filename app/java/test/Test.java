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
//         this.bl = true;
//         this.bt = 127;
//         this.sh = 100;
//         this.in = 142;
//         this.ln = 42L;
//         this.ft = 123.33f;
//         this.db = 0.53;
//
//         System.out.println(this.in);
    }

    @Override
    public int length() {
        return super.length();
    }

    public static void main() {
        long a = System.currentTimeMillis();
        System.out.println("begin: " + a);
        System.out.println("Hello world");

        Test test = new Test();
        test.in = 9;
        System.out.println(test.in);
        System.out.println(sl);
        System.out.println(singleton.ln);

        long b = System.currentTimeMillis() ;
        System.out.println("end: " + b);
        System.out.println("cost : " + (b - a));



    }

//     public void t1() {
//         int a, b, c, d;
//         a = 3;
//         b = 4;
//         c = a * 2 + b * b;
//         d = 0;
//
//         if (c > 0) {
//             a = b > 3 ? 2 : 1;
//         } else {
//             if (b > 3) {
//                 while (d < a) {
//                     if(d==a-1){
//                         c++;
//                     }
//                     c++;
//                     d++;
//                 }
//                 a = 100000;
//             } else {
//                 a = 200000;
//             }
//         }
//         d = a + c;
//
//     }
}
