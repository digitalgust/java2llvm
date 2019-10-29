package j2ll;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * External class resolver
 */
public final class Resolver {

    private Set<String> classes = new CopyOnWriteArraySet<>();

    public String resolve(String str) {

        if (classes.add(str)) {
            //System.out.println("resolve " + str);
        }
        return "%" + str.replace('/', '_') + "*";
    }

    public String resolveStruct(String str) {
        if (classes.add(str)) {
            //System.out.println("resolve " + str);
        }
        return "%" + str.replace('/', '_') + "";
    }

    public Set<String> getClasses() {
        return classes;
    }
}
