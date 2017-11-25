package com.techshroom.lettar;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.reflect.ClassClimbing;
import com.techshroom.lettar.reflect.StringConversionException;
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
            EMPTY_404_HANDLER = makeNotFoundHandler(SimpleRouter.class.getMethod("empty404Reponse"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static Response<?> empty404Response() {
        return SimpleResponse.builder().statusCode(404).build();
    }

    // method handles must be (LRequest;LExceptionParam;)LResponse;
    private final Map<Class<?>, MethodHandle> exceptionHandlers = new HashMap<>();
    // method handles must be (LRequest;)LResponse;
    private MethodHandle notFoundHandler = EMPTY_404_HANDLER;
    // method handles must be (LRequest;[LObject;)LResponse;
    // the array is the length of the parameters :D
    private final RouteMap<MethodHandle> routes = new RouteMap<>();

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
                addRoute(route, method);
                continue;
            }

            NotFoundHandler notFoundAnnot = method.getAnnotation(NotFoundHandler.class);
            if (notFoundAnnot != null) {
                notFoundHandler = makeNotFoundHandler(method);
                continue;
            }

            ServerErrorHandler errorAnnot = method.getAnnotation(ServerErrorHandler.class);
            if (errorAnnot != null) {
                addServerErrorHandlers(errorAnnot.exception(), method);
                continue;
            }
        }
    }

    private void addRoute(Route route, Method method) {
        HttpMethodPredicate methodPredicate = HttpMethodPredicate.of(route.method());
        ImmutableSet<PathRoutePredicate> pathPredicate = Stream.of(route.path())
                .map(PathRoutePredicate::parse)
                .collect(toImmutableSet());
        KeyValuePredicate params = KeyValuePredicate.of(route.params());
        KeyValuePredicate headers = KeyValuePredicate.of(route.headers());

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

        MethodHandle resolvedHandle = safeUnreflect(method);

        resolvedHandle = SRMethodHandles.routeTransform(resolvedHandle, numCaps, method.getName());

        routes.addRuntimeRoute(RuntimeRoute.of(methodPredicate, pathPredicate, params, headers, resolvedHandle));
    }

    private static MethodHandle makeNotFoundHandler(Method method) {
        MethodHandle base = safeUnreflect(method);
        return SRMethodHandles.notFoundHandlerTransform(base, method.getName());
    }

    private void addServerErrorHandlers(Class<? extends Throwable>[] exception, Method method) {
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
            }
            Iterator<Class<?>> superClasses = ClassClimbing.superClasses(param, Throwable.class);
            while (superClasses.hasNext()) {
                Class<?> exc = superClasses.next();
                if (exceptions.contains(exc)) {
                    parameterIndexes.put(exc, p);
                }
            }
            throw new IllegalArgumentException(String.format("Invalid parameter %s, must be Request or one of the exceptions.",
                    param.getName()));
        }

        // all parameters validated, pass on!
        MethodHandle base = safeUnreflect(method);
        // inject a Request in the first param if needed
        base = SRMethodHandles.injectRequestParameter(base);
        for (Class<?> exc : exceptions) {
            MethodHandle result = SRMethodHandles.errorHandlerTransform(base, parameterIndexes, exc, method.getName());
            exceptionHandlers.put(exc, result);
        }
    }

    @Override
    public Response<OB> route(Request<IB> request) {
        Optional<Response<OB>> res = routes.route(request, route -> {
            return Optional.ofNullable(callRoute(request, route));
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
                return route.getRouteTarget().invoke(request, array);
            } catch (StringConversionException badStringConversion) {
                // this becomes a 404
                return null;
            } catch (Throwable t) {
                return maybeHandleError(request, t);
            }
        });
    }

    private Response<OB> maybeHandleError(Request<IB> request, Throwable t) throws Throwable {
        try {
            Iterator<Class<?>> classes = ClassClimbing.superClasses(t.getClass(), Throwable.class);
            while (classes.hasNext()) {
                MethodHandle serverErrorHandler = exceptionHandlers.get(classes.next());
                if (serverErrorHandler != null) {
                    return invokeHandleUnsafe(() -> serverErrorHandler.invoke(request, t));
                }
            }
            // re-throw, it'll be handled outside
            throw t;
        } catch (Throwable unhandledOrBad) {
            // 500 handler messed up!
            // just log it...
            LOGGER.error("Unhandled 500 exception, please add a more general handler or ensure existing ones do not throw exceptions!", t);
            // return a response with a null body, the best we can do
            return SimpleResponse.<OB> builder().statusCode(500).build();
        }
    }

    private interface MHCall {

        Object call() throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <V> V invokeHandleUnsafe(MHCall call) {
        V result;
        try {
            result = (V) call.call();
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        return result;
    }

}
