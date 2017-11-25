package com.techshroom.lettar.routing;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@AutoValue
public abstract class KeyValuePredicate {

    private static final Splitter EQUALS_LIM_2 = Splitter.on('=').limit(2);

    public static KeyValuePredicate of(String[] values) {
        return of(Stream.of(values).map(v -> {
            Iterator<String> split = EQUALS_LIM_2.split(v).iterator();
            String key = split.next();
            checkState(split.hasNext(), "invalid value %s", v);
            String value = split.next();
            return Maps.immutableEntry(key, value);
        }).collect(toImmutableMap(Entry::getKey, Entry::getValue)));
    }

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
