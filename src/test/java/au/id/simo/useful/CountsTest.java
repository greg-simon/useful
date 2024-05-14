package au.id.simo.useful;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class CountsTest {
    private Object resourceLock = new Object();

    public CountsTest() {
    }

    @BeforeEach
    public void beforeEachTest() {
        Counts.removeAll();
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testIncrement() {
        String countName = "testIncrement";
        assertEquals(0l, Counts.get(countName));
        assertEquals(1l, Counts.increment(countName));
        assertEquals(2l, Counts.increment(countName));
        assertEquals(2l, Counts.get(countName));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testSetExistingCount() {
        String countName = "testSetExisting";
        assertEquals(0l, Counts.get(countName));
        assertEquals(0l, Counts.set(countName, 5));
        assertEquals(5l, Counts.set(countName, 9));
        assertEquals(9l, Counts.get(countName));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testSetNonExistingCount() {
        String countName = "testSetNonExisting";
        assertEquals(0l, Counts.set(countName, 5));
        assertEquals(5l, Counts.set(countName, 9));
        assertEquals(9l, Counts.get(countName));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testGetExisting() {
        String countName = "testGetExisting";
        Counts.set(countName, 5);
        assertEquals(5l, Counts.get(countName));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testGetNonExisting() {
        String countName = "testGetNonExisting";
        assertEquals(0l, Counts.get(countName));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testGetCountNamesEmpty() {
        List<String> names = Counts.getCountNames();
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testGetCountNamesNotEmpty() {
        Counts.increment("count1");
        Counts.increment("count2");
        Counts.increment("count3");

        List<String> names = Counts.getCountNames();
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("count1"));
        assertTrue(names.contains("count2"));
        assertTrue(names.contains("count3"));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testRemoveCount() {
        // set up
        Counts.increment("count");
        assertTrue(Counts.getCountNames().contains("count"));

        // actual test
        Counts.removeCount("count");
        assertFalse(Counts.getCountNames().contains("count"));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testRemoveCountNonExisting() {
        // set up assertion
        assertFalse(Counts.getCountNames().contains("count"));

        // actual test, assert removal had no effect
        Counts.removeCount("count");
        assertFalse(Counts.getCountNames().contains("count"));
    }

    @Test
    @ResourceLock(value = "resourceLock")
    public void testRemoveAll() {
        // set up
        Counts.increment("1");
        Counts.increment("2");
        Counts.increment("3");
        Counts.increment("4");
        assertEquals(4, Counts.getCountNames().size());

        // test
        Counts.removeAll();
        assertTrue(Counts.getCountNames().isEmpty());
    }
}
