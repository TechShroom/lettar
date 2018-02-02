package com.techshroom.lettar.inheiritor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.auto.value.AutoValue;
import com.techshroom.lettar.reflect.Extractions;

@AutoValue
public abstract class InheritorContext {

    public static InheritorContext from(Method m) {
        Type bodyType = Extractions.getBodyTypeForMethod(m);
        return of(bodyType);
    }
    
    public static InheritorContext of(Type bodyType) {
        return new AutoValue_InheritorContext(bodyType);
    }

    InheritorContext() {
    }

    /**
     * @return the type of the body that is expected in this context
     */
    public abstract Type getBodyType();

}
