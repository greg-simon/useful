package au.id.simo.useful.collections;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.NoSuchElementException;

import au.id.simo.useful.collections.ExpireCollection.ExpireEntry;
import au.id.simo.useful.test.ManualClock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ExpireCollectionTest {

    @Test
    public void testAdd() {
        ManualClock mc = new ManualClock(Instant.EPOCH);
        ExpireCollection<String> ec = new ExpireCollection<>(mc, Duration.ofSeconds(1));
        assertEquals(0, ec.size());
        ec.add("First item");
        assertEquals(1, ec.size());
    }

    @Test
    public void testAdd_WithExpiry() {
        ManualClock mc = new ManualClock(Instant.EPOCH);
        ExpireCollection<String> ec = new ExpireCollection<>(mc, Duration.ofSeconds(1));
        assertEquals(0, ec.size());
        ec.add("First item", Duration.ofSeconds(2));
        assertEquals(1, ec.size());
    }

    @Test
    public void testExpireIterator() {
        ManualClock mc = new ManualClock(Instant.EPOCH);
        ExpireCollection<String> ec = new ExpireCollection<>(mc, Duration.ZERO);

        mc.setInstant(Instant.ofEpochMilli(1));
        ec.add("First String");

        mc.setInstant(Instant.ofEpochMilli(2));
        ec.add("Second String");
        
        ExpireEntry<String> entry;
        Iterator<ExpireEntry<String>> itr = ec.expiryIterator();
        assertTrue(itr.hasNext());
        entry = itr.next();
        assertEquals("First String", entry.getValue());
        assertEquals(Instant.ofEpochMilli(1), entry.getExpiry());
        
        assertTrue(itr.hasNext());
        entry = itr.next();
        assertEquals("Second String", entry.getValue());
        assertEquals(Instant.ofEpochMilli(2), entry.getExpiry());
        
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, () -> itr.next());
    }
    
    @Test
    public void testIterator() {
        ManualClock mc = new ManualClock(Instant.EPOCH);
        ExpireCollection<String> ec = new ExpireCollection<>(mc, Duration.ZERO);

        mc.setInstant(Instant.ofEpochMilli(1));
        ec.add("First String");

        mc.setInstant(Instant.ofEpochMilli(2));
        ec.add("Second String");
        
        Iterator<String> itr = ec.iterator();
        assertTrue(itr.hasNext());
        assertEquals("First String", itr.next());
        
        assertTrue(itr.hasNext());
        assertEquals("Second String", itr.next());
        
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, () -> itr.next());
    }
    
    @Test
    public void testIterator_Remove() {
        ManualClock mc = new ManualClock(Instant.EPOCH);
        ExpireCollection<String> ec = new ExpireCollection<>(mc, Duration.ZERO);

        mc.setInstant(Instant.ofEpochMilli(1));
        ec.add("First String");

        mc.setInstant(Instant.ofEpochMilli(2));
        ec.add("Second String");
        
        Iterator<String> itr = ec.iterator();
        assertThrows(IllegalStateException.class, () -> itr.remove());
        
        assertTrue(itr.hasNext());
        assertEquals("First String", itr.next());
        
        assertTrue(itr.hasNext());
        assertEquals("Second String", itr.next());
        
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, () -> itr.next());
        
        assertEquals(2, ec.size());
        itr.remove();
        assertEquals(1, ec.size());
        assertEquals("First String", ec.iterator().next());
    }
}
