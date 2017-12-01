package com.techshroom.lettar;

import com.google.common.base.Throwables;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.annotation.ServerErrorHandler;

public class TestRoutes {

    @Route(path = "/")
    public Response<String> index() {
        return SimpleResponse.of(200, "Index Page");
    }

    @Route(path = "/query", params = "page=index")
    public Response<String> queryIndex() {
        return SimpleResponse.of(200, "Queried 'index' Page");
    }

    @Route(path = "/query", params = "page=action")
    public Response<String> queryAction() {
        return SimpleResponse.of(200, "Queried 'action' Page");
    }

    @Route(path = "/{*}/list")
    public Response<String> resouceList(String resourceType) {
        return SimpleResponse.of(200, "[a,b,c," + resourceType + "]");
    }

    @Route(path = "/ec/{**}")
    public Response<String> endChomp(String chomped) {
        return SimpleResponse.of(200, "CHOMP! " + chomped);
    }

    @Route(path = "/re/{re:\\d+}")
    public Response<String> endChomp(int number) {
        return SimpleResponse.of(200, "RE: " + number);
    }

    @Route(path = "/error")
    public Response<String> getError() {
        throw new AssertionError("Error Here");
    }

    @ServerErrorHandler
    public Response<String> error(Exception error) {
        return SimpleResponse.of(500, "Error encountered: " + Throwables.getStackTraceAsString(error));
    }

    @ServerErrorHandler(exception = AssertionError.class)
    public Response<String> assertionFailed(AssertionError error) {
        return SimpleResponse.of(500, "Error encountered: " + error.getMessage());
    }

    @NotFoundHandler
    public Response<String> notFound() {
        return SimpleResponse.of(404, "404 Page");
    }

}
