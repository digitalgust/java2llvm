package j2ll.graph;


import j2ll.graph.inst.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class IrSentence extends IrObject {
    public static final int LOAD = 0;
    public static final int STORE = 1;
    public static final int GETPTR = 2;
    public static final int BR2 = 3;
    public static final int BR1 = 4;
    public static final int LABEL = 5;
    public static final int ALLOCA = 6;
    public static final int ICMP = 7;
    public static final int COMMENT = 8;
    public static final int RET = 9;
    public static final int CALL = 10;
    public static final int BITCAST = 11;//trunc sext zext
    public static final int ARITH = 12;//add sub mul div

    protected String line;
    protected String instName;
    protected int index;
    protected int stackSize;
    protected int instType;

    public IrSentence() {
    }

    public int getInstType() {
        return instType;
    }

    public int getStackSize() {
        return stackSize;
    }

    abstract public IrVariable getLeft();

    abstract public void parse(String st);

    abstract public void replaceVarName(IrVariable old, IrVariable newv);

    static public IrSentence parseInst(String str) {

        String s = str.indexOf(';') >= 0 ? str.substring(0, str.indexOf(';')) : str;
        s = s.trim();
        String right = s;

        IrSentence irs = null;
        if (s.indexOf('=') > 0) {
            right = s.split("=")[1].trim();
        }
        if (right.startsWith("load ")) {
            irs = new IrLoad();
            irs.parse(s);
        } else if (right.startsWith("store ")) {
            irs = new IrStore();
            irs.parse(s);
        } else if (right.startsWith("br i1")) {
            irs = new IrBranch2();
            irs.parse(s);
        } else if (right.startsWith("br label")) {
            irs = new IrBranch1();
            irs.parse(s);
        } else if (right.startsWith("alloca ")) {
            irs = new IrAlloca();
            irs.parse(s);
        } else if (right.startsWith("icmp ")) {
            irs = new IrIcmp();
            irs.parse(s);
        } else if (right.startsWith("getelementptr ")) {
            irs = new IrGetptr();
            irs.parse(s);
        } else if (right.endsWith(":")) {
            irs = new IrLabel();
            irs.parse(s);
        } else if (right.equals("")) {
            irs = new IrComment();
            irs.parse(s);
        } else if (right.startsWith("ret ")) {
            irs = new IrRet();
            irs.parse(s);
        } else if (right.startsWith("call ")) {
            irs = new IrCall();
            irs.parse(s);
        } else if (
                right.startsWith("bitcast ")
                        || right.startsWith("trunc ")
                        || right.startsWith("sext ")
                        || right.startsWith("zext ")
                        || right.startsWith("ptrtoint ")
                        || right.startsWith("inttoptr ")
        ) {
            irs = new IrBitcast();
            irs.parse(s);
        } else if (
                right.startsWith("add ")
                        || right.startsWith("sub ")
                        || right.startsWith("mul ")
                        || right.startsWith("div ")
                        || right.startsWith("rem ")
                        || right.startsWith("sadd ")
                        || right.startsWith("ssub ")
                        || right.startsWith("smul ")
                        || right.startsWith("sdiv ")
                        || right.startsWith("srem ")
        ) {
            irs = new IrArith();
            irs.parse(s);
        } else {
            System.out.println("unknow :" + s);
        }
        irs.line = s;
        return irs;
    }


    /**
     * split code line ,
     * ex:   %__tmpc6 = bitcast {i32, {i32, [0 x i16]}*, i32, i32}* %stack1 to {i32, [0 x i16]}*
     * the "{i32, {i32, [0 x i16]}*, i32, i32}*" and "{i32, [0 x i16]}*" as a whole entry
     *
     *  the method can process 2 level '{{}}'
     *
     * @param s
     * @param regex
     * @return
     */
    public static String[] split(String s, String regex) {

        String data = s.trim();
        //预处理include
        Pattern p = Pattern.compile("(\\{([^{}]+|\\{([^{}]+)*\\})*\\}[*]{0,})", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        int index = 0;

        Map<String, String> map = new HashMap<>();

        StringBuilder result = new StringBuilder();
        // 使用循环将句子里所有的匹配字符串找出
        while (m.find()) {
            //直接输出 html
            String tmps = data.substring(index, m.start());
            result.append(tmps);
            //解析
            tmps = data.substring(m.start(), m.end());
            String varName = "$$$TEMPVAR$$$_" + index;
            map.put(varName, tmps);
            result.append(varName);
            index = m.end();
        }
        result.append(data.substring(index));

        String s1 = result.toString();
        String[] ss = s1.split(regex);

        for (int i = 0; i < ss.length; i++) {
            String g = map.get(ss[i]);
            if (g != null) {
                ss[i] = g;
            }
        }
        return ss;
    }

    public String toString(int deep) {
        String space = "";
        for (int i = 0; i < deep; i++) {
            space += "    ";
        }
        return space + line + "\n";
    }
}
