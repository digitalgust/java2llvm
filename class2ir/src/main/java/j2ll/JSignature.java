package j2ll;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Java Signature. Example: (IDJ)V
 */
public class JSignature {

    private String javaArgs;
    private String javaResult;
    private String result;
    private List<String> args;

    public JSignature(Resolver resolver, String str) {
        int posA = str.indexOf('(');
        if (posA == -1) throw new IllegalArgumentException();
        int posB = str.indexOf(')');
        if (posB == -1) throw new IllegalArgumentException();
        this.javaArgs = str.substring(posA + 1, posB);
        this.javaResult = str.substring(posB + 1);
        this.args = Util.javaSignatures2irTypes(resolver, this.javaArgs);
        this.result = Util.javaSignature2irType(resolver, this.javaResult);
    }

    public String getJavaArgs() {
        return javaArgs;
    }

    public String getJavaResult() {
        return javaResult;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public String getResult() {
        return this.result;
    }

    public String getSignatureCall(String className, String methodName, RuntimeStack stack, String prefix) {
        StringJoiner joiner = new StringJoiner(", ", this.result + " @" + getID(className, methodName) + "(", ")");
        if (prefix != null) joiner.add(prefix);
        List<String> pops = new ArrayList<String>();
        for (int i = 0; i < this.args.size(); i++) {
            pops.add(0, stack.pop().toString());
        }
        for (int i = 0; i < this.args.size(); i++) {
            String arg = this.args.get(i);
            joiner.add(arg + " " + pops.get(i));
        }
        return joiner.toString();
    }

    public String getSignatureDeclare(String className, String methodName, String prefix) {
        StringJoiner joiner = new StringJoiner(", ", this.result + " @" + getID(className, methodName) + "(", ")");
        if (prefix != null) joiner.add(prefix);
        for (String arg : this.args) {
            joiner.add(arg);
        }
        return joiner.toString();
    }

    public String getID(String className, String methodName) {
        return Util.classMethodSignature2id(className, methodName, this);
    }
}
