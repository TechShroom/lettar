package com.techshroom.lettar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.reflect.ClassClimbing;

public class ClassClimbingTest {

    private static class Top {
    }

    private static class Middle extends Top {
    }

    private static class Bottom extends Middle {
    }

    @Test
    public void testClimbToObject() throws Exception {
        assertEquals(ImmutableList.of(Bottom.class, Middle.class, Top.class, Object.class),
                ImmutableList.copyOf(ClassClimbing.superClasses(Bottom.class)));
    }

    @Test
    public void testClimbToTop() throws Exception {
        assertEquals(ImmutableList.of(Bottom.class, Middle.class, Top.class),
                ImmutableList.copyOf(ClassClimbing.superClasses(Bottom.class, Top.class)));
    }

}
