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
        String s = push_impl(type);
        if (type.equals(Internals.LONG) || type.equals(Internals.DOUBLE)) {
            push(new StackValue(StackValue.MODE_SLOT2, 0, Internals.SLOT2));
        }
        return s;
    }

    private String push_impl(String type) {
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


    public String push(StackValue value) {
        String s = push_impl(value);
        if (value.getIR().equals(Internals.LONG) || value.getIR().equals(Internals.DOUBLE)) {
            push(new StackValue(StackValue.MODE_SLOT2, 0, Internals.SLOT2));
        }
        return s;
    }

    private String push_impl(StackValue value) {
        int v = ord;
        this.names[pos] = ord;
        this.imm2[pos] = value;
        ord++;
        pos++;
        return "%stack" + v;
    }

    public StackValue pop() {
        StackValue sv = pop_impl();
        if (sv.getMode() == StackValue.MODE_SLOT2) {
            sv = pop_impl();
        }
        return sv;
    }

    private StackValue pop_impl() {
        if (pos == 0) {
            //return new StackValue(StackValue.MODE_REG, "NEEDFIX", Internals.INT);
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

    public int size() {
        return pos;
    }
}
