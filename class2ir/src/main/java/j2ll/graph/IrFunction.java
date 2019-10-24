package j2ll.graph;

public class IrFunction {
    IrBlock block;
    public String define;
    public String end;

    public IrFunction() {
    }

    public void parse(String funcStr) {
        String[] strs = funcStr.split("\n");
        parse(strs);
    }

    public void parse(String[] strs) {
        SourceToken st = new SourceToken(strs);
        st.pos = 0;

        System.out.println(define);
        block = new IrBlock();
        block.parse(st);
        System.out.println(end);
    }


    //    public String toString() {
//        return define + "\n" + block.toString(0) + end + "\n\n";
//    }
    public String toString() {
        return block.toString(0);
    }
}
