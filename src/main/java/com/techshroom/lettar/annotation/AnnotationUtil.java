package com.techshroom.lettar.annotation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class AnnotationUtil {

    private static final Splitter EQUALS_LIM_2 = Splitter.on('=').limit(2);

    public static Map<String, String> parseMap(String[] values) {
        return Stream.of(values).map(v -> {
            Iterator<String> split = EQUALS_LIM_2.split(v).iterator();
            String key = split.next();
            checkState(split.hasNext(), "invalid value %s", v);
            String value = split.next();
            return Maps.immutableEntry(key, value);
        }).collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

}
