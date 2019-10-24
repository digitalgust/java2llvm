package java.lang;

class StringBuilder {

    char[] value = new char[16];
    int count = 0;

    public int length() {
        return count;
    }

    public StringBuilder append(String s) {
        if (s == null) {
            s = "null";
        }
        int len = s.length();
        expand(len);
        s.getChars(0, len, value, count);
        count += len;
        return this;
    }


    static char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public StringBuilder append(long i) {
        String s = "";
        while (i != 0) {
            int v = (int) (i % 10);
            s = chars[v] + s;
            i /= 10;
        }
        if (i < 0) {
            s = '-' + s;
        }
        return append(s);
    }

    public StringBuilder append(int i) {
        return append((long) i);
    }

    public StringBuilder append(char c) {
        expand(1);
        value[count] = c;
        count++;
        return this;
    }
//=============================================================

    public String toString() {
        return new String(value, 0, count);
    }

    void expand(int need) {
        if (value.length < count + need) {
            char[] v = value;
            value = new char[(count + need) * 2];
            System.arraycopy(v, 0, value, 0, count);
        }
    }

    public void reverse() {
        for (int i = 0; i < count / 2; i++) {
            char ch = value[i];
            value[i] = value[count - 1 - i];
            value[count - 1 - i] = ch;
        }
    }
}