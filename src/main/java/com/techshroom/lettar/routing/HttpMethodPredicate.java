package com.techshroom.lettar.routing;

import java.util.Collection;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@AutoValue
public abstract class HttpMethodPredicate {

    public static HttpMethodPredicate of(HttpMethod... methods) {
        return of(ImmutableSet.copyOf(methods));
    }

    public static HttpMethodPredicate of(Collection<HttpMethod> methods) {
        return new AutoValue_HttpMethodPredicate(Sets.immutableEnumSet(methods));
    }

    HttpMethodPredicate() {
    }

    abstract ImmutableSet<HttpMethod> getMethods();

    public final boolean matches(HttpMethod method) {
        return getMethods().contains(method);
    }

}
