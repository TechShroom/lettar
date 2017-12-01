package com.techshroom.lettar.routing;

import java.util.Collection;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.mime.MimeType;

@AutoValue
public abstract class RouteResult<V> {

    public static <V> RouteResult<V> of(V target, Collection<String> pathVars, MimeType contentType) {
        return new AutoValue_RouteResult<>(target, ImmutableList.copyOf(pathVars), contentType);
    }

    public abstract V getRouteTarget();

    public abstract ImmutableList<String> getPathVariables();

    public abstract MimeType getContentType();

}