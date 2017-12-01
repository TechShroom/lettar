package com.techshroom.lettar.routing;

import java.util.Map;
import java.util.Map.Entry;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

@AutoValue
public abstract class KeyValuePredicate {

    public static KeyValuePredicate of(Map<String, String> values) {
        return new AutoValue_KeyValuePredicate(ImmutableMap.copyOf(values));
    }

    KeyValuePredicate() {
    }

    abstract ImmutableMap<String, String> getMap();

    public final boolean matches(Map<String, String> values) {
        // fast-check keyset before iterating
        if (!values.keySet().containsAll(getMap().keySet())) {
            return false;
        }
        for (Entry<String, String> e : getMap().entrySet()) {
            String valuesValue = values.get(e.getKey());
            if (!e.getValue().equals(valuesValue)) {
                return false;
            }
        }
        return true;
    }

}
