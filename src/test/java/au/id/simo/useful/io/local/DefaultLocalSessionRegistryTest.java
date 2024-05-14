package au.id.simo.useful.io.local;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultLocalSessionRegistryTest implements LocalSessionRegistryTest {
    @Override
    public LocalSessionRegistry getInstance() {
        return new DefaultLocalSessionRegistry("default");
    }

    @ParameterizedTest
    @MethodSource("constructorParams")
    public void testCapacityCalculation(String namespace, int minId, int maxId, int capacity) {
        DefaultLocalSessionRegistry registry = new DefaultLocalSessionRegistry(namespace, minId, maxId);
        assertEquals(registry.getMinSessionId(), minId);
        assertEquals(registry.getMaxSessionId(), maxId);
        assertEquals(registry.capacity(), capacity);
    }

    static Stream<Arguments> constructorParams() {
        return Stream.of(
                // namespace, min ID, maxID, expected capacity
                Arguments.of("test-namespace", 1, 10, 10), // [1,2,3,4,5,6,7,8,9,10]
                Arguments.of("test-namespace", 1, 100, 100),
                Arguments.of("test-namespace", 1, 1, 1),// [1]
                Arguments.of("test-namespace", -1, 1, 3), // [-1,0,1]
                Arguments.of("test-namespace", 5, 10, 6) // [5,6,7,8,9,10]
        );
    }

    @Test
    void testAllocatingAllSessions() {
        DefaultLocalSessionRegistry registry = new DefaultLocalSessionRegistry("default", 1, 3);
        assertEquals(3, registry.capacity());

        LocalSession session01 = registry.newSession();
        assertEquals("1", session01.getId());
        assertEquals(1, registry.size());

        LocalSession session02 = registry.newSession();
        assertEquals("2", session02.getId());
        assertEquals(2, registry.size());

        LocalSession session03 = registry.newSession();
        assertEquals("3", session03.getId());
        assertEquals(3, registry.size());

        // All sessions have been allocated
        // ensure new session request fails
        assertThrows(SessionLimitReachedException.class,() -> registry.newSession());
        assertEquals(3, registry.size());

        // close a session to free space and allocate a new one.
        session02.close();
        assertEquals(2, registry.size());
        LocalSession session04 = registry.newSession();
        assertEquals(3, registry.size());
        assertEquals("2", session04.getId());
        assertNotSame(session02, session04);

        // test close all
        assertEquals(3, registry.closeAllSessions());
        assertEquals(0, registry.size());
        assertTrue(session01.isClosed());
        assertTrue(session02.isClosed());
        assertTrue(session03.isClosed());
        assertTrue(session04.isClosed());
    }

    @Test
    void testGetMaxSessionId() {
        assertEquals(2, new DefaultLocalSessionRegistry("default", 1, 10).getMaxSessionIdLength());
        assertEquals(2, new DefaultLocalSessionRegistry("default", -9, 5).getMaxSessionIdLength());
        assertEquals(1, new DefaultLocalSessionRegistry("default", 1, 5).getMaxSessionIdLength());
        assertEquals(5, new DefaultLocalSessionRegistry("default", -1000, 1000).getMaxSessionIdLength());
        assertEquals(4, new DefaultLocalSessionRegistry("default", 1, 1000).getMaxSessionIdLength());
        assertEquals(10, new DefaultLocalSessionRegistry("default", 1, Integer.MAX_VALUE).getMaxSessionIdLength());
    }
}
