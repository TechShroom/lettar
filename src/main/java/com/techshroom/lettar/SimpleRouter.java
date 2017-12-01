/*
 * This file is part of Lettar, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.lettar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.AnnotationUtil;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.Produces;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.reflect.ClassClimbing;
import com.techshroom.lettar.reflect.StringConversionException;
import com.techshroom.lettar.routing.AcceptPredicate;
import com.techshroom.lettar.routing.HttpMethodPredicate;
import com.techshroom.lettar.routing.KeyValuePredicate;
import com.techshroom.lettar.routing.PathRoutePredicate;
import com.techshroom.lettar.routing.RouteMap;
import com.techshroom.lettar.routing.RouteResult;
import com.techshroom.lettar.routing.RuntimeRoute;

public class SimpleRouter<IB, OB> implements Router<IB, OB> {

    private static final Logger LOGGER = Logging.getLogger();

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    private static MethodHandle safeUnreflect(Method method) {
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            // this method assumes method is public
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle EMPTY_404_HANDLER;
    static {
        try {
            EMPTY_404_HANDLER = makeNotFoundHandler(SimpleRouter.class.getMethod("empty404Response"), null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static Response<?> empty404Response() {
        return SimpleResponse.builder().statusCode(404).build();
    }

    public static <IB, OB> SimpleRouter<IB, OB> create() {
        return new SimpleRouter<>();
    }

    // method handles must be (LRequest;LExceptionParam;)LResponse;
    private final Map<Class<?>, MethodHandle> exceptionHandlers = new HashMap<>();
    // method handles must be (LRequest;)LResponse;
    private MethodHandle notFoundHandler = EMPTY_404_HANDLER;
    // method handles must be (LRequest;[LObject;)LResponse;
    // the array is the length of the parameters :D
    private final RouteMap<MethodHandle> routes = new RouteMap<>();

    private SimpleRouter() {
    }

    @Override
    public void registerRoutes(Iterable<?> routes) {
        routes.forEach(this::register);
    }

    private void register(Object routeContainer) {
        Set<Method> methods = ImmutableSet.copyOf(routeContainer.getClass().getMethods());
        for (Method method : methods) {
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }
            Route route = method.getAnnotation(Route.class);
            if (route != null) {
                Produces producesAnnot = method.getAnnotation(Produces.class);
                String[] produces = producesAnnot == null
                        ? new String[] { Produces.DEFAULT }
                        : producesAnnot.value();
                addRoute(route, produces, method, routeContainer);
                continue;
            }

            NotFoundHandler notFoundAnnot = method.getAnnotation(NotFoundHandler.class);
            if (notFoundAnnot != null) {
                notFoundHandler = makeNotFoundHandler(method, routeContainer);
                continue;
            }

            ServerErrorHandler errorAnnot = method.getAnnotation(ServerErrorHandler.class);
            if (errorAnnot != null) {
                addServerErrorHandlers(errorAnnot.exception(), method, routeContainer);
                continue;
            }
        }
    }

    private void addRoute(Route route, String[] produces, Method method, Object container) {
        HttpMethodPredicate methodPredicate = HttpMethodPredicate.of(route.method());
        ImmutableSet<PathRoutePredicate> pathPredicate = Stream.of(route.path())
                .map(PathRoutePredicate::parse)
                .collect(toImmutableSet());
        KeyValuePredicate params = KeyValuePredicate.of(AnnotationUtil.parseMap(route.params()));
        KeyValuePredicate headers = KeyValuePredicate.of(AnnotationUtil.parseMap(route.headers()));
        AcceptPredicate acceptPredicate = AcceptPredicate.of(parseMime(produces));

        // validate that all paths use the same number of captures
        int numCaps = -1;
        for (PathRoutePredicate pred : pathPredicate) {
            if (numCaps == -1) {
                numCaps = pred.getNumberOfCapturedParts();
            }
            checkState(numCaps == pred.getNumberOfCapturedParts(),
                    "all paths must have the same number of captures: found %s and %s",
                    numCaps, pred.getNumberOfCapturedParts());
        }

        MethodHandle base = safeUnreflect(method);

        base = SRMethodHandles.fillThisParam(base, container);
        base = SRMethodHandles.routeTransform(base, numCaps, method.getName());

        routes.addRuntimeRoute(RuntimeRoute.of(methodPredicate, pathPredicate, params, headers, acceptPredicate, base));
    }

    private Collection<MimeType> parseMime(String[] value) {
        return Stream.of(value).map(MimeType::parse).collect(toImmutableSet());
    }

    private static MethodHandle makeNotFoundHandler(Method method, Object container) {
        MethodHandle base = safeUnreflect(method);
        base = SRMethodHandles.fillThisParam(base, container);
        return SRMethodHandles.notFoundHandlerTransform(base, method.getName());
    }

    private void addServerErrorHandlers(Class<? extends Throwable>[] exception, Method method, Object container) {
        Set<Class<?>> exceptions = ImmutableSet.copyOf(exception);
        Map<Class<?>, Integer> parameterIndexes = new HashMap<>();
        int p = -1;
        for (Class<?> param : method.getParameterTypes()) {
            p++;
            // we require that the parameter be a Request or exception
            if (Request.class.isAssignableFrom(param)) {
                // Request must be first for ease-of-coding on this side :)
                checkState(p == 0, "request must be the first parameter");
                // stick it in the map too
                parameterIndexes.put(Request.class, 0);
                continue;
            } else {
                // adjust for injected request parameter
                p++;
            }
            Iterator<Class<?>> superClasses = ClassClimbing.superClasses(param, Throwable.class);
            boolean found = false;
            while (superClasses.hasNext()) {
                Class<?> exc = superClasses.next();
                if (exceptions.contains(exc)) {
                    parameterIndexes.put(exc, p);
                    found = true;
                    break;
                }
            }
            checkArgument(found, "Invalid parameter %s, must be Request or one of the exceptions (%s).",
                    param.getName(), exceptions);
        }

        // all parameters validated, pass on!
        MethodHandle base = safeUnreflect(method);
        base = SRMethodHandles.fillThisParam(base, container);
        // inject a Request in the first param if needed
        base = SRMethodHandles.injectParameter(base, 0, Request.class);
        for (Class<?> exc : exceptions) {
            MethodHandle result = SRMethodHandles.errorHandlerTransform(base, parameterIndexes, exc, method.getName());
            exceptionHandlers.put(exc, result);
        }
    }

    @Override
    public Response<OB> route(Request<IB> request) {
        Optional<Response<OB>> res = routes.route(request, route -> {
            Optional<Response<OB>> responseOpt = Optional.ofNullable(callRoute(request, route));
            // mix in content-type
            responseOpt = responseOpt.map(r -> {
                if (!r.getHeaders().containsKey("content-type")) {
                    return r.addHeaders(ImmutableMap.of("content-type", route.getContentType().toString()));
                }
                return r;
            });
            return responseOpt;
        });
        return res.orElseGet(() -> invokeHandleUnsafe(() -> {
            try {
                return notFoundHandler.invoke(request);
            } catch (Throwable t) {
                return maybeHandleError(request, t);
            }
        }));
    }

    private Response<OB> callRoute(Request<IB> request, RouteResult<MethodHandle> route) {
        return invokeHandleUnsafe(() -> {
            // we use Request + Object[] to pass arguments
            Object[] array = route.getPathVariables().toArray();
            // catch errors and return a 500 response
            try {
                return route.getRouteTarget().invoke(request, route.getContentType(), array);
            } catch (StringConversionException badStringConversion) {
                // this becomes a 404
                return null;
            } catch (Throwable t) {
                return maybeHandleError(request, t);
            }
        });
    }

    private Response<OB> maybeHandleError(Request<IB> request, Throwable t) throws Throwable {
        Iterator<Class<?>> classes = ClassClimbing.superClasses(t.getClass(), Throwable.class);
        while (classes.hasNext()) {
            Class<?> cls = classes.next();
            MethodHandle serverErrorHandler = exceptionHandlers.get(cls);
            if (serverErrorHandler != null) {
                try {
                    return invokeHandleUnsafe(() -> serverErrorHandler.invoke(request, t));
                } catch (Throwable bad) {
                    LOGGER.error("500 handler for " + cls + " threw an exception", bad);
                    return SimpleResponse.<OB> builder().statusCode(500).build();
                }
            }
        }
        LOGGER.error("Unhandled 500 exception, please add a more general handler!", t);
        // return a response with a null body, the best we can do
        return SimpleResponse.<OB> builder().statusCode(500).build();
    }

    private interface MHCall {

        Object call() throws Throwable;
    }

    private static <V> V invokeHandleUnsafe(MHCall call) {
        try {
            @SuppressWarnings("unchecked")
            V result = (V) call.call();
            return result;
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

}
