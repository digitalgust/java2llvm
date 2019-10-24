package j2ll;

/**
 * Stack
 */
public class RuntimeStack {

    int ord = 0;
    private final int SIZE = 1024;

    int[] names = new int[SIZE];
    StackValue[] imm2 = new StackValue[SIZE];
    int pos = 0;

    RuntimeStack() {

    }

    public String push(String type) {
        int v = ord;
        this.names[pos] = ord;
        this.imm2[pos] = new StackValue(StackValue.MODE_REG, v, type);
        ord++;
        pos++;
        return "%stack" + v;
    }

    public String pushObjRef(String type) {
        return push(new StackValue(StackValue.MODE_OBJREF, ord, type));
    }

    public String pushImm(Object value, String type) {
        return push(new StackValue(StackValue.MODE_IMM, value, type));
    }

    public String push(StackValue value) {
        int v = ord;
        this.names[pos] = ord;
        this.imm2[pos] = value;
        ord++;
        pos++;
        return "%stack" + v;
    }

    public StackValue pop() {
        if (pos == 0) {
            throw new RuntimeException("stack underflow");
        }
        pos--;
        return this.imm2[pos];
    }

    public StackValue peek(int pos2top) {
        int tmp = pos + pos2top;
        if (tmp == -1) {
            throw new RuntimeException("stack underflow");
        }
        return this.imm2[tmp];
    }

    public int size(){
        return pos;
    }
}
