package com.techshroom.lettar;

/**
 * Routes a request, and returns a response.
 */
public interface Router<IB, OB> {

    void registerRoutes(Iterable<?> routes);

    Response<OB> route(Request<IB> request);

}
