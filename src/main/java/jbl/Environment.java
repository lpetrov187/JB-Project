package jbl;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Value> bindings = new HashMap<>();
    private final Environment parent;

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Value get(String name) {
        if (bindings.containsKey(name)) return bindings.get(name);
        if (parent != null) return parent.get(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    public void set(String name, Value value) {
        bindings.put(name, value);
    }
}
