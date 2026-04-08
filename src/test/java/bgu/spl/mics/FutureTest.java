package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;


class FutureTest {
    @Test
    void testResolve() {
        Future<String> future = new Future<>();
        assertFalse(future.isDone());
        future.resolve("Test");
        assertTrue(future.isDone());
        assertEquals("Test", future.get());
    }

    @Test
    void testGetWithTimeout() {
        Future<String> future = new Future<>();
        assertNull(future.get(1, TimeUnit.SECONDS));
        future.resolve("Timeout Test");
        assertEquals("Timeout Test", future.get(10, TimeUnit.SECONDS));
    }

}