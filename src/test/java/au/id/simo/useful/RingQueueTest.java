package au.id.simo.useful;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class RingQueueTest {

    @Test
    public void testIncrementIndex() {
        RingQueue<Integer> rq = new RingQueue<>(3);
        assertEquals(1,rq.incrementIndex(0));
        assertEquals(2,rq.incrementIndex(1));
        assertEquals(0,rq.incrementIndex(2));
        assertEquals(1,rq.incrementIndex(3));
    }

    @Test
    public void testHappy() {
        RingQueue<Integer> rq = new RingQueue<>(3);
        assertEquals(0, rq.size());
        assertEquals(3, rq.maxSize());
        assertTrue(rq.isEmpty());
        assertFalse(rq.isNotEmpty());
        assertFalse(rq.isFull());
        
        rq.add(1);
        assertEquals(1, rq.size());
        assertFalse(rq.isEmpty());
        assertFalse(rq.isFull());
        assertTrue(rq.isNotEmpty());
        assertEquals(1, rq.peek());
        assertArrayEquals(new Integer[]{1}, rq.toArray());
        
        rq.add(2);
        assertEquals(2, rq.size());
        assertFalse(rq.isEmpty());
        assertFalse(rq.isFull());
        assertTrue(rq.isNotEmpty());
        assertEquals(1, rq.peek());
        assertArrayEquals(new Integer[]{1,2}, rq.toArray());
        
        rq.add(3);
        assertEquals(3, rq.size());
        assertFalse(rq.isEmpty());
        assertTrue(rq.isFull());
        assertTrue(rq.isNotEmpty());
        assertEquals(1, rq.peek());
        assertArrayEquals(new Integer[]{1,2,3}, rq.toArray());
        
        rq.add(4);
        assertEquals(3, rq.size());
        assertFalse(rq.isEmpty());
        assertTrue(rq.isNotEmpty());
        assertTrue(rq.isFull());
        assertEquals(2, rq.peek());
        assertArrayEquals(new Integer[]{2,3,4}, rq.toArray());
        
        rq.add(5);
        assertEquals(3, rq.size());
        assertFalse(rq.isEmpty());
        assertTrue(rq.isNotEmpty());
        assertTrue(rq.isFull());
        assertEquals(3, rq.peek());
        assertArrayEquals(new Integer[]{3,4,5}, rq.toArray());
        
        // removing values
        assertEquals(3, rq.remove());
        assertEquals(2, rq.size());
        assertFalse(rq.isEmpty());
        assertTrue(rq.isNotEmpty());
        assertFalse(rq.isFull());
        assertArrayEquals(new Integer[]{4,5}, rq.toArray());
        
        assertEquals(4, rq.remove());
        assertEquals(1, rq.size());
        assertFalse(rq.isEmpty());
        assertTrue(rq.isNotEmpty());
        assertFalse(rq.isFull());
        assertArrayEquals(new Integer[]{5}, rq.toArray());
        
        assertEquals(5, rq.remove());
        assertEquals(0, rq.size());
        assertTrue(rq.isEmpty());
        assertFalse(rq.isNotEmpty());
        assertFalse(rq.isFull());
        assertArrayEquals(new Integer[]{}, rq.toArray());
        
        rq.clear();
    }
    
    @Test
    public void testClearAndRemove() {
        RingQueue<Integer> rq = new RingQueue<>(5);
        rq.add(1);
        rq.add(2);
        rq.add(3);
        rq.add(4);
        rq.add(5);
        assertEquals(5, rq.size());
        assertArrayEquals(new Integer[]{1,2,3,4,5}, rq.toArray());
        
        rq.clear();
        assertEquals(0, rq.size());
        assertTrue(rq.isEmpty());
        assertArrayEquals(new Integer[]{}, rq.toArray());
        
        rq.add(6);
        rq.add(7);
        rq.add(8);
        rq.add(9);
        rq.add(0);
        assertTrue(rq.isFull());
        assertFalse(rq.isEmpty());
        assertEquals(6,rq.remove());
        assertEquals(7,rq.remove());
        assertEquals(8,rq.remove());
        assertEquals(9,rq.remove());
        assertEquals(0,rq.remove());
        assertTrue(rq.isEmpty());
        assertNull(rq.peek());
        
        assertThrows(NoSuchElementException.class, () -> {
            rq.remove();
        });
    }

    @Test
    public void testGet() {
        RingQueue<Integer> rb = new RingQueue<>(3);
        rb.add(1);
        rb.add(2);
        rb.add(3);
        assertEquals(1, rb.peek(0));
        assertEquals(2, rb.peek(1));
        assertEquals(3, rb.peek(2));
        
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            rb.peek(3); 
        });
        
        rb.add(4);
        rb.add(5);
        assertEquals(3, rb.peek(0));
        assertEquals(4, rb.peek(1));
        assertEquals(5, rb.peek(2));
        
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            rb.peek(3);
        });
    }
}
