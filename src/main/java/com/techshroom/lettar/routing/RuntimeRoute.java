package com.techshroom.lettar.routing;

import java.util.Collection;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.routing.PathRoutePredicate.MatchResult;

/**
 * Runtime interpretation of {@link Route}.
 * 
 * @param <V>
 *            - the type contained in this route
 */
@AutoValue
public abstract class RuntimeRoute<V> {

    public static <V> RuntimeRoute<V> of(HttpMethodPredicate methodPredicate,
            Collection<PathRoutePredicate> pathPredicates,
            KeyValuePredicate paramPredicate,
            KeyValuePredicate headerPredicate,
            V target) {
        return new AutoValue_RuntimeRoute<>(methodPredicate, ImmutableSet.copyOf(pathPredicates),
                paramPredicate, headerPredicate, target);
    }

    private static final Splitter SLASH = Splitter.on('/');

    RuntimeRoute() {
    }

    abstract HttpMethodPredicate methodPredicate();

    abstract ImmutableSet<PathRoutePredicate> pathPredicates();

    abstract KeyValuePredicate paramPredicate();

    abstract KeyValuePredicate headerPredicate();

    abstract V target();

    public final Optional<RouteResult<V>> matches(Request request) {
        if (!methodPredicate().matches(request.getMethod())) {
            return Optional.empty();
        }
        if (!paramPredicate().matches(request.getQueryParts())) {
            return Optional.empty();
        }
        if (!headerPredicate().matches(request.getHeaders())) {
            return Optional.empty();
        }
        for (PathRoutePredicate pathPredicate : pathPredicates()) {
            MatchResult pathResult = pathPredicate.matches(SLASH.splitToList(request.getPath()));
            if (!pathResult.isSuccessfulMatch()) {
                continue;
            }
            return Optional.of(RouteResult.of(target(), pathResult.getParts()));
        }
        return Optional.empty();
    }

}
