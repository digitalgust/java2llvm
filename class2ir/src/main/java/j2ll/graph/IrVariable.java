package j2ll.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrVariable {
    public String type, name;

    public IrVariable(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof IrVariable) {
            IrVariable irv = (IrVariable) o;
            return irv.name.equals(this.name);
        }
        return false;
    }

    public String toString() {
        return type + " " + name;
    }

}
