/*
 * This file is part of lettar, licensed under the MIT License (MIT).
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
import static com.techshroom.lettar.reflect.MethodHandles2.invokeHandleUnchecked;

import java.lang.invoke.MethodHandle;
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

import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.AnnotationUtil;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.reflect.ClassClimbing;
import com.techshroom.lettar.reflect.MethodHandles2;
import com.techshroom.lettar.reflect.StringConversionException;
import com.techshroom.lettar.routing.AcceptPredicate;
import com.techshroom.lettar.routing.HttpMethodPredicate;
import com.techshroom.lettar.routing.KeyValuePredicate;
import com.techshroom.lettar.routing.PathRoutePredicate;
import com.techshroom.lettar.routing.RouteMap;
import com.techshroom.lettar.routing.RouteResult;
import com.techshroom.lettar.routing.RuntimeRoute;
import com.techshroom.lettar.transform.TransformChain;

public class SimpleRouter<IB, OB> implements Router<IB, OB> {

    private static final Logger LOGGER = Logging.getLogger();

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
    private final RouteMap<TransformChain<IB, OB>> routes = new RouteMap<>();
    private final ThreadLocal<MimeType> contentTypeThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<Object[]> parametersThreadLocal = new ThreadLocal<>();

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
                RouteEnhancements enhancements = RouteEnhancements.load(method);
                addRoute(route, enhancements, method, routeContainer);
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

    private void addRoute(Route route, RouteEnhancements enhancements, Method method, Object container) {
        HttpMethodPredicate methodPredicate = HttpMethodPredicate.of(route.method());
        ImmutableSet<PathRoutePredicate> pathPredicate = Stream.of(route.path())
                .map(PathRoutePredicate::parse)
                .collect(toImmutableSet());
        KeyValuePredicate params = KeyValuePredicate.of(AnnotationUtil.parseMultimap(route.params()));
        KeyValuePredicate headers = KeyValuePredicate.of(AnnotationUtil.parseMap(route.headers()).asMultimap());
        AcceptPredicate acceptPredicate = AcceptPredicate.of(parseMime(enhancements.produces()));

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

        MethodHandle base = MethodHandles2.safeUnreflect(method);

        base = SRMethodHandles.fillThisParam(base, container);
        base = SRMethodHandles.routeTransform(base, numCaps, enhancements, contentTypeThreadLocal, parametersThreadLocal, method.getName());

        TransformChain<IB, OB> chain = SRChains.newChain(enhancements, base);

        routes.addRuntimeRoute(RuntimeRoute.of(methodPredicate, pathPredicate, params, headers, acceptPredicate, chain));
    }

    private Collection<MimeType> parseMime(Collection<String> value) {
        return value.stream().map(MimeType::parse).collect(toImmutableSet());
    }

    private static MethodHandle makeNotFoundHandler(Method method, Object container) {
        MethodHandle base = MethodHandles2.safeUnreflect(method);
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
        MethodHandle base = MethodHandles2.safeUnreflect(method);
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
                if (!r.getHeaders().getMultimap().containsKey("content-type")) {
                    return r.addHeader("content-type", route.getContentType().toString());
                }
                return r;
            });
            return responseOpt;
        });
        return res.orElseGet(() -> invokeHandleUnchecked(() -> {
            try {
                return notFoundHandler.invoke(request);
            } catch (Throwable t) {
                return maybeHandleError(request, t);
            }
        }));
    }

    private Response<OB> callRoute(Request<IB> request, RouteResult<TransformChain<IB, OB>> route) {
        contentTypeThreadLocal.set(route.getContentType());
        parametersThreadLocal.set(route.getPathVariables().toArray());
        try {
            return route.getRouteTarget().withRequest(request).next();
        } catch (StringConversionException badStringConversion) {
            // this becomes a 404
            return null;
        } catch (Throwable t) {
            return maybeHandleError(request, t);
        } finally {
            contentTypeThreadLocal.set(null);
            parametersThreadLocal.set(null);
        }
    }

    private Response<OB> maybeHandleError(Request<IB> request, Throwable t) {
        Iterator<Class<?>> classes = ClassClimbing.superClasses(t.getClass(), Throwable.class);
        while (classes.hasNext()) {
            Class<?> cls = classes.next();
            MethodHandle serverErrorHandler = exceptionHandlers.get(cls);
            if (serverErrorHandler != null) {
                try {
                    return invokeHandleUnchecked(() -> serverErrorHandler.invoke(request, t));
                } catch (Throwable bad) {
                    bad.addSuppressed(new RuntimeException("Original Exception in Cause", t));
                    // if unhandled Error, re-throw, as it is likely fatal
                    if (bad instanceof Error) {
                        throw (Error) bad;
                    }
                    LOGGER.error("500 handler for " + cls + " threw an exception", bad);
                    return SimpleResponse.<OB> builder().statusCode(500).build();
                }
            }
        }
        // if unhandled Error, re-throw, as it is likely fatal
        if (t instanceof Error) {
            throw (Error) t;
        }
        LOGGER.error("Unhandled 500 exception, please add a more general handler!", t);
        // return a response with a null body, the best we can do
        return SimpleResponse.<OB> builder().statusCode(500).build();
    }

}
