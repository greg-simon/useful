package au.id.simo.useful;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class RingBufferTest {

    @Test
    public void testIncrementIndex() {
        RingBuffer rb = new RingBuffer(3);
        assertEquals(1,rb.incrementIndex(0));
        assertEquals(2,rb.incrementIndex(1));
        assertEquals(0,rb.incrementIndex(2));
        assertEquals(1,rb.incrementIndex(3));
    }

    @Test
    public void testHappy() {
        RingBuffer rb = new RingBuffer(3);
        System.out.println(rb);
        assertEquals(0, rb.size());
        assertEquals(3, rb.maxSize());
        assertTrue(rb.isEmpty());
        assertFalse(rb.isNotEmpty());
        assertFalse(rb.isFull());
        
        rb.add(1);
        System.out.println(rb);
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1}, rb.toArray());
        
        rb.add(2);
        System.out.println(rb);
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1,2}, rb.toArray());
        
        rb.add(3);
        System.out.println(rb);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1,2,3}, rb.toArray());
        
        rb.add(4);
        System.out.println(rb);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertEquals(2, rb.peek());
        assertArrayEquals(new byte[]{2,3,4}, rb.toArray());
        
        rb.add(5);
        System.out.println(rb);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertEquals(3, rb.peek());
        assertArrayEquals(new byte[]{3,4,5}, rb.toArray());
        
        // removing values
        assertEquals(3, rb.remove());
        System.out.println(rb);
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{4,5}, rb.toArray());
        
        assertEquals(4, rb.remove());
        System.out.println(rb);
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{5}, rb.toArray());
        
        assertEquals(5, rb.remove());
        System.out.println(rb);
        assertEquals(0, rb.size());
        assertTrue(rb.isEmpty());
        assertFalse(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{}, rb.toArray());
        
        rb.clear();
        System.out.println(rb);
    }
    
    @Test
    public void testClearAndRemove() {
        RingBuffer buf = new RingBuffer(5);
        buf.add(1);
        buf.add(2);
        buf.add(3);
        buf.add(4);
        buf.add(5);
        assertEquals(5, buf.size());
        assertTrue(buf.containsArray(new byte[]{1,2,3,4,5}));
        
        buf.clear();
        assertEquals(0, buf.size());
        assertTrue(buf.isEmpty());
        assertTrue(buf.containsArray(new byte[]{}));
        
        buf.add(6);
        buf.add(7);
        buf.add(8);
        buf.add(9);
        buf.add(0);
        assertTrue(buf.isFull());
        assertFalse(buf.isEmpty());
        assertEquals(6,buf.remove());
        assertEquals(7,buf.remove());
        assertEquals(8,buf.remove());
        assertEquals(9,buf.remove());
        assertEquals(0,buf.remove());
        assertTrue(buf.isEmpty());
        
        boolean execeptionThrown = false;
        try {
            buf.remove();
            fail("Expected exception before this line");
        } catch (ArrayIndexOutOfBoundsException e) {
            execeptionThrown = true;
        }
        assertTrue(execeptionThrown);
    }

    @Test
    public void testGet() {
        RingBuffer rb = new RingBuffer(3);
        rb.add(1);
        rb.add(2);
        rb.add(3);
        System.out.println(rb);
        assertEquals(1, rb.get(0));
        assertEquals(2, rb.get(1));
        assertEquals(3, rb.get(2));
        
        try {
            rb.get(3);
            fail("ArrayIndexOutOfBoundsException should have been thrown above");
        } catch (ArrayIndexOutOfBoundsException e) {}
        
        rb.add(4);
        rb.add(5);
        System.out.println(rb);
        assertEquals(3, rb.get(0));
        assertEquals(4, rb.get(1));
        assertEquals(5, rb.get(2));
        
        try {
            rb.get(3);
            fail("ArrayIndexOutOfBoundsException should have been thrown above");
        } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    @Test
    public void testContainsArray() {
        String testStr = "testing 123";
        byte[] testArray = testStr.getBytes();
        RingBuffer rb = new RingBuffer(testArray.length+1);
        for(byte b: testArray) {
            assertFalse(rb.containsArray(testArray));
            rb.add(b);
        }
        assertTrue(rb.containsArray(testArray));
    }
}
