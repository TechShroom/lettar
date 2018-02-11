package com.techshroom.lettar;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.techshroom.lettar.pipe.PipelineRouterInitializer;

public class SseTest extends AbstractRouterTest {

    private Router<String, InputStream> router;

    @Before
    public void setup() {
        router = new PipelineRouterInitializer()
                .newRouter(ImmutableList.of(new SseTestRoutes()));
    }

    @Test
    public void testSse() throws Exception {
        Response<InputStream> response = getStageValue(router.route(request("/sse")));
        byte[] data = ByteStreams.toByteArray(response.getBody());
        byte[] expected = ("event: test\n" +
                "id: 1\n" +
                "data: event 1\n" +
                "\n" +
                "event: test\n" +
                "id: 2\n" +
                "data: event 2\n" +
                "\n").getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, data);
    }

}
