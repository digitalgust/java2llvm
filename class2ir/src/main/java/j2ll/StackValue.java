package j2ll;

/**
 *
 */
public class StackValue {

    public static final int MODE_IMM = 0;
    public static final int MODE_REG = 1;
    public static final int MODE_OBJREF = 2;

    private int mode;
    private Object value;
    private String IR;

    public StackValue(int mode, String IR) {
        this.mode = mode;
        this.IR = IR;
    }

    public StackValue(int mode, Object value, String IR) {
        this.mode = mode;
        this.value = value;
        this.IR = IR;
    }

    public int getMode() {
        return mode;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setMode(int t) {
        this.mode = t;
    }

    public String getIR() {
        return IR;
    }

    public void setIR(String IR) {
        this.IR = IR;
    }

    public String toString() {
        if (this.mode == MODE_REG || this.mode == MODE_OBJREF) return "%stack" + value;
        if (value instanceof Float) {
            Float f = (Float) value;
            return "0x" + Integer.toHexString(Float.floatToRawIntBits(f)) + "00000000";
        }
        if (value instanceof Double) {
            Double f = (Double) value;
            return "0x" + Long.toHexString(Double.doubleToRawLongBits(f));
        }
        return value.toString(); // imm ??
    }

    public String fullName() { //todo
        return getIR() + " " + toString();
    }

}
