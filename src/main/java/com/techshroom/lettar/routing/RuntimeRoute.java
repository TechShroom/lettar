package com.techshroom.lettar.routing;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.mime.MimeType;
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
            AcceptPredicate acceptPredicate,
            V target) {
        return new AutoValue_RuntimeRoute<>(methodPredicate, ImmutableSet.copyOf(pathPredicates),
                paramPredicate, headerPredicate, acceptPredicate, target);
    }

    private static final Splitter SLASH = Splitter.on('/').omitEmptyStrings();

    RuntimeRoute() {
    }

    abstract HttpMethodPredicate methodPredicate();

    abstract ImmutableSet<PathRoutePredicate> pathPredicates();

    abstract KeyValuePredicate paramPredicate();

    abstract KeyValuePredicate headerPredicate();

    abstract AcceptPredicate acceptPredicate();

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
        Optional<MimeType> contentType = acceptPredicate().matches(request.getHeaders().getOrDefault("Accept", "*/*"));
        if (!contentType.isPresent()) {
            return Optional.empty();
        }
        if (!pathPredicates().isEmpty()) {
            List<String> splitPath = SLASH.splitToList(request.getPath());
            for (PathRoutePredicate pathPredicate : pathPredicates()) {
                MatchResult pathResult = pathPredicate.matches(splitPath);
                if (!pathResult.isSuccessfulMatch()) {
                    continue;
                }
                return Optional.of(RouteResult.of(target(), pathResult.getParts(), contentType.get()));
            }
        }
        return Optional.empty();
    }

}
