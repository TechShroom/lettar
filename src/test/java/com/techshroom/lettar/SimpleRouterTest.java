package com.techshroom.lettar;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.routing.HttpMethod;

public class SimpleRouterTest {

    private SimpleRouter<String, String> router;

    @Before
    public void setup() {
        router = SimpleRouter.create();
    }

    private void registerRoutes() {
        router.registerRoutes(ImmutableList.of(new TestRoutes()));
    }

    @Test
    public void testRegisterRoutes() throws Exception {
        registerRoutes();
    }

    @Test
    public void testPathRoutes() throws Exception {
        registerRoutes();

        assertEquals(SimpleResponse.of(200, "Index Page"), router.route(request("/")));
        assertEquals(SimpleResponse.of(200, "Index Page"), router.route(request("//")));
        assertEquals(SimpleResponse.of(200, "[a,b,c,res1]"), router.route(request("/res1/list")));
        assertEquals(SimpleResponse.of(200, "[a,b,c,res2]"), router.route(request("/res2/list")));
    }

    @Test
    public void testQueryRoutes() throws Exception {
        registerRoutes();

        assertEquals(SimpleResponse.of(200, "Queried 'index' Page"), router.route(request("/query",
                ImmutableMap.of("page", "index"))));
        assertEquals(SimpleResponse.of(200, "Queried 'action' Page"), router.route(request("/query",
                ImmutableMap.of("page", "action", "action", "foobar"))));
    }

    @Test
    public void test404Routes() throws Exception {
        registerRoutes();

        SimpleResponse<String> notFound = SimpleResponse.of(404, "404 Page");

        assertEquals(notFound, router.route(request("/nonexist")));
        assertEquals(notFound, router.route(request("//list")));
        assertEquals(notFound, router.route(request("/apiary/drapiary/list")));
    }

    private static Request<String> request(String path) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path)
                .build();
    }

    private static Request<String> request(String path, Map<String, String> query) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path)
                .queryParts(query)
                .build();
    }

}
