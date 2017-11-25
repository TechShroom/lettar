package com.techshroom.lettar.routing;

import com.google.common.collect.ImmutableMap;

/**
 * A request is something that needs routing. It is made up of a path, query
 * parts, a method and headers. For HTTP handling, there's another interface
 * with the body as well.
 */
public interface Request {

    String getPath();

    ImmutableMap<String, String> getQueryParts();

    ImmutableMap<String, String> getHeaders();

    HttpMethod getMethod();

}
