package com.techshroom.lettar;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Utilities for HTTP.
 */
public class HttpUtil {

    /**
     * Creates a special {@link ImmutableMap} builder that creates
     * case-insensitive maps, as required for headers.
     * 
     * @return a header map builder
     */
    public static ImmutableMap.Builder<String, String> headerMapBuilder() {
        return ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
    }

}
