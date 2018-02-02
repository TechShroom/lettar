package com.techshroom.lettar.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import com.techshroom.lettar.Request;

public class Extractions {

    public static Type getBodyTypeForMethod(Method method) {
        // method may not have request argument
        Type[] params = method.getGenericParameterTypes();
        if (params.length < 1) {
            return Object.class;
        }
        Type arg1 = params[0];
        if (!(arg1 instanceof ParameterizedType)) {
            // raw type or something weirder -- still just object!
            return Object.class;
        }
        ParameterizedType paramType = (ParameterizedType) arg1;
        if (paramType.getRawType() != Request.class) {
            return Object.class;
        }
        Type[] typeArgs = paramType.getActualTypeArguments();
        if (typeArgs.length < 1) {
            // raw type or something weirder -- still just object!
            return Object.class;
        }
        return resolveConcreteType(typeArgs[0]);
    }

    private static Type resolveConcreteType(Type type) {
        if (type instanceof WildcardType) {
            // reduce to upper/lower bound
            // this isn't perfect, but devs SHOULDNT be using wildcards...
            WildcardType wild = (WildcardType) type;
            if (wild.getLowerBounds().length > 0) {
                return wild.getLowerBounds()[0];
            }
            if (wild.getUpperBounds().length > 0) {
                return wild.getUpperBounds()[0];
            }
            return Object.class;
        }
        return type;
    }

}
