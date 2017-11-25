package com.techshroom.lettar.routing;

import java.util.Collection;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class RouteResult<V> {

    public static <V> RouteResult<V> of(V target, Collection<String> pathVars) {
        return new AutoValue_RouteResult<>(target, ImmutableList.copyOf(pathVars));
    }

    public abstract V getRouteTarget();

    public abstract ImmutableList<String> getPathVariables();

}