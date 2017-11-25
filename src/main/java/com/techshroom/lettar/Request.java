package com.techshroom.lettar;

import javax.annotation.Nullable;

/**
 * A request is something that needs routing. It is made up of a path, query
 * parts, a method, headers, and body.
 */
public interface Request<B> extends com.techshroom.lettar.routing.Request {

    @Nullable
    B getBody();

}
