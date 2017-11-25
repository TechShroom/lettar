package com.techshroom.lettar;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Map;

import com.techshroom.lettar.reflect.StringConverters;

class SRMethodHandles {

    private static void verifyResponseReturn(MethodType type, String name) {
        checkArgument(Response.class.isAssignableFrom(type.returnType()),
                "response return required (method %s%s)", name, type);
    }

    public static MethodHandle notFoundHandlerTransform(MethodHandle base, String name) {
        // need (LRequest;)LResponse;
        MethodType originalType = base.type();

        base = injectRequestParameter(base);

        checkArgument(base.type().parameterCount() == 1,
                "Only a Request is allowed as a parameter (method %s%s)",
                originalType, name);

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle routeTransform(MethodHandle base, int numCaps, String name) {
        // need (LRequest;[LObject;)LResponse;

        base = injectRequestParameter(base);

        MethodType type = base.type();

        // transform parameters as needed -- we want all inputs to be String
        MethodHandle[] filters = new MethodHandle[type.parameterCount()];
        for (int i = 1; i < type.parameterCount(); i++) {
            Class<?> parameter = type.parameterType(i);
            filters[i] = StringConverters.getHandleForArgType(parameter);
        }
        base = MethodHandles.filterArguments(base, 0, filters);

        // append parameters as needed
        int needed = numCaps - (base.type().parameterCount() - 1);
        Class<?>[] injection = new Class<?>[needed];
        Arrays.fill(injection, String.class);
        base = MethodHandles.dropArguments(base, base.type().parameterCount(), injection);

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle errorHandlerTransform(MethodHandle base, Map<Class<?>, Integer> paramIndexes, Class<?> exception, String name) {
        // need (LRequest;LExceptionParam;)LResponse;
        // base is (LRequest;<exceptions that are part of the
        // handler>;)LResponse;
        MethodType originalType = base.type();

        int index = paramIndexes.getOrDefault(exception, -1);
        if (index != -1) {
            // drop parameters that aren't the parameter (except req)
            // this is the length of range [1, index)
            int numBefore = index - 1;
            base = MethodHandles.insertArguments(base, 1, new Object[numBefore]);
            // this is the length of range (index, count - 1]
            int numAfter = base.type().parameterCount() - 1 - index;
            base = MethodHandles.insertArguments(base, index + 1, new Object[numAfter]);
        } else {
            // drop all parameters after the request, insert fake parameter
            base = MethodHandles.insertArguments(base, 1, new Object[originalType.parameterCount() - 1]);
            base = MethodHandles.dropArguments(base, 1, exception);
        }

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle injectRequestParameter(MethodHandle base) {
        MethodType type = base.type();

        // first, check first parameter, inject Request if needed
        int params = type.parameterCount();
        switch (params) {
            case 0:
                // no parameter - inject Request as a fake parameter
                base = MethodHandles.dropArguments(base, 0, Request.class);
                break;
            default:
                // perhaps inject
                if (Request.class.isAssignableFrom(type.parameterType(0))) {
                    // all good
                    break;
                }
                base = MethodHandles.dropArguments(base, 0, Request.class);
        }
        return base;
    }

}
