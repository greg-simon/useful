package au.id.simo.useful;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ByteRingBufferTest {

    @Test
    public void testIncrementIndex() {
        ByteRingBuffer rb = new ByteRingBuffer(3);
        assertEquals(1, rb.incrementIndex(0, 1));
        assertEquals(2, rb.incrementIndex(1, 1));
        assertEquals(0, rb.incrementIndex(2, 1));
        assertEquals(1, rb.incrementIndex(3, 1));
    }

    @Test
    public void testHappy() {
        ByteRingBuffer rb = new ByteRingBuffer(3);
        assertEquals(0, rb.size());
        assertEquals(3, rb.maxSize());
        assertTrue(rb.isEmpty());
        assertFalse(rb.isNotEmpty());
        assertFalse(rb.isFull());

        rb.add(1);
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1}, rb.toArray());

        rb.add(2);
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1, 2}, rb.toArray());

        rb.add(3);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1, 2, 3}, rb.toArray());

        rb.add(4);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertEquals(2, rb.peek());
        assertArrayEquals(new byte[]{2, 3, 4}, rb.toArray());

        rb.add(5);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertEquals(3, rb.peek());
        assertArrayEquals(new byte[]{3, 4, 5}, rb.toArray());

        // removing values
        assertEquals(3, rb.read());
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{4, 5}, rb.toArray());

        assertEquals(4, rb.read());
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{5}, rb.toArray());

        assertEquals(5, rb.read());
        assertEquals(0, rb.size());
        assertTrue(rb.isEmpty());
        assertFalse(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertArrayEquals(new byte[]{}, rb.toArray());

        rb.clear();
    }

    @Test
    public void testClearAndRemove() {
        ByteRingBuffer buf = new ByteRingBuffer(5);
        buf.add(1);
        buf.add(2);
        buf.add(3);
        buf.add(4);
        buf.add(5);
        assertEquals(5, buf.size());
        assertTrue(buf.containsArray(new byte[]{1, 2, 3, 4, 5}));

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
        assertEquals(6, buf.read());
        assertEquals(7, buf.read());
        assertEquals(8, buf.read());
        assertEquals(9, buf.read());
        assertEquals(0, buf.read());
        assertTrue(buf.isEmpty());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            buf.read();
        });
    }

    @Test
    public void testPeek() {
        ByteRingBuffer rb = new ByteRingBuffer(3);
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

    @Test
    public void testContainsArray() {
        String testStr = "testing 123";
        byte[] testArray = testStr.getBytes();
        ByteRingBuffer rb = new ByteRingBuffer(testArray.length + 1);
        for (byte b : testArray) {
            assertFalse(rb.containsArray(testArray));
            rb.add(b);
        }
        assertTrue(rb.containsArray(testArray));
    }
    
    @Test
    public void testRead() {
        ByteRingBuffer rb = new ByteRingBuffer(3);
        rb.add(1);
        rb.add(2);
        rb.add(3);
        assertTrue(rb.isFull());
        assertEquals(1,rb.read());
        assertFalse(rb.isFull());
        assertEquals(2, rb.size());
    }
    
    @Test
    public void testRead_array() {
        ByteRingBuffer rb = new ByteRingBuffer(10);
        for (byte b = 0; b < 10; b++) {
            rb.put(b);
        }
        assertTrue(rb.isFull());
        
        byte[] newbuf = new byte[10];
        int readCount = rb.read(newbuf, 0, 10);
        assertEquals(10, readCount);
        assertArrayEquals(new byte[]{0,1,2,3,4,5,6,7,8,9}, newbuf);
        assertTrue(rb.isEmpty());
    }
    
    @Test
    public void testRead_array_split_segments() {
        ByteRingBuffer rb = new ByteRingBuffer(10);
        for (byte b = 0; b < 10; b++) {
            rb.put(b);
        }
        assertTrue(rb.isFull());
        
        // remove first 5 values
        for(int i=0;i<5;i++) {
            rb.read();
        }
        
        assertEquals(5, rb.size());
        
        // add another 3
        rb.put(10);
        rb.put(11);
        rb.put(12);
        assertEquals(8, rb.size());
        // expected array values within ring buffer: [10,11,12,-,-,5,6,7,8,9]
        // with tail after the head
        
        byte[] newbuf = new byte[10];
        int readCount = rb.read(newbuf, 0, 10);
        assertEquals(8, readCount);
        assertArrayEquals(new byte[]{5,6,7,8,9,10,11,12,0,0}, newbuf);
        assertTrue(rb.isEmpty());
    }
}
