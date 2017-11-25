package com.techshroom.lettar.routing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

public class RouteMap<V> {

    /**
     * Routes moves through two states, first modifiable for registration, then
     * immutablized on first routing.
     */
    private transient Set<RuntimeRoute<V>> routes = new HashSet<>();
    private boolean routeImmutable = false;

    public void addRuntimeRoute(RuntimeRoute<V> route) {
        routes.add(route);
    }

    public <O> Optional<O> route(Request request, Function<RouteResult<V>, Optional<O>> handler) {
        if (!routeImmutable) {
            routes = ImmutableSet.copyOf(routes);
            routeImmutable = true;
        }
        return routes.stream()
                .map(r -> r.matches(request))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(handler)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

}
