package j2ll;

import java.util.HashMap;
import java.util.Map;

/**
 * Statistics
 */
public final class Statistics {

    private Resolver resolver = new Resolver();
    private Map<String, LocalVarTable> vars = new HashMap<String, LocalVarTable>();

    public Resolver getResolver() {
        return resolver;
    }

    public void put(String name, LocalVarTable lv) {
        this.vars.put(name, lv);
    }

    public LocalVarTable get(String name) {
        return this.vars.get(name);
    }
}
