package au.id.simo.useful;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface AbstractRingBufferTest<T> {
    
    T[] testData(int arrayLength);
    AbstractRingBuffer<T> createRingBuffer(int capacity);
    
    @Test
    default void testAdd() {
        T[] testData = testData(11);
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        
        // add data the first time
        for (T testData1 : testData) {
            rb.add(testData1);
        }
        
        // add data a second time to overwrite the first loop
        for (T testData1 : testData) {
            rb.add(testData1);
        }
        
        assertEquals(testData.length, rb.size(), "Should not be more items than the testData");
        
        for (int i=0;i<testData.length;i++) {
            assertEquals(testData[i], rb.peek(i), String.format("index: %s", i));
        }
    }
    
    @Test
    default void testPut() {
        T[] testData = testData(2);
        AbstractRingBuffer<T> rb = createRingBuffer(1);
        
        rb.put(testData[0]);
        assertEquals(1, rb.size());
        
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            rb.put(testData[1]);
        }, "Should throw exception when using put at buffer capacity.");
    }
    
    @Test
    default void testPeek() {
        T[] testData = testData(2);
        AbstractRingBuffer<T> rb = createRingBuffer(2);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            rb.peek();
        });
        rb.add(testData[0]);
        assertEquals(testData[0], rb.peek());
        rb.add(testData[1]);
        // should still return the oldest element
        assertEquals(testData[0], rb.peek());
    }
    
    @Test
    default void testPeek_3args() {
        T[] testData = testData(11);
        
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        
        // add all test data to ring buffer
        for(T element: testData) {
            rb.add(element);
        }
        
        T[] buffer = testData(11);
        // set all buffer elements to null
        for (int i=0;i<buffer.length;i++) {
            buffer[i] = null;
        }
        
        int peekCount = rb.peek(buffer, 0, buffer.length);
        assertEquals(buffer.length, peekCount);
        assertEquals(testData.length, rb.size());
        assertArrayEquals(testData, buffer);
    }
    
    @Test
    default void testRead() {
        T[] testData = testData(2);
        AbstractRingBuffer<T> rb = createRingBuffer(2);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            rb.read();
        });
        rb.add(testData[0]);
        assertEquals(1, rb.size());
        assertEquals(testData[0], rb.read());
        assertEquals(0, rb.size());
        
        rb.add(testData[1]);
        assertEquals(1, rb.size());
        assertEquals(testData[1], rb.read());
        assertEquals(0, rb.size());
    }
    
    @Test
    default void testRead_3args() {
        T[] testData = testData(11);
        
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        
        // add all test data to ring buffer
        for(T element: testData) {
            rb.add(element);
        }
        
        T[] buffer = testData(11);
        // set all buffer elements to null
        for (int i=0;i<buffer.length;i++) {
            buffer[i] = null;
        }
        
        int readCount = rb.read(buffer, 0, buffer.length);
        assertEquals(buffer.length, readCount);
        assertEquals(0, rb.size());
        assertArrayEquals(testData, buffer);
    }
    
    @Test
    default void testContainsArray() {
        T[] testData = testData(11);
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length+2);
        
        // move index up by three, but leave ring buffer empty.
        T firstElement = testData[0];
        for (int i=0;i<3;i++) {
            rb.add(firstElement);
            rb.read();
        }
        
        for (T element: testData) {
            rb.add(element);
        }
        assertTrue(rb.containsArray(testData));
        
        // overwrite most elements and test again.
        for (T element: testData) {
            rb.add(element);
        }
        assertTrue(rb.containsArray(testData));
    }
    
    @Test
    default void testContainsArrayFailures() {
        T[] testData = testData(5);
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        
        for (T element: testData) {
            rb.add(element);
        }
        assertTrue(rb.containsArray(testData));
        
        T[] biggerTestData = testData(6);
        assertFalse(rb.containsArray(biggerTestData));
        
        // remove oldest element and expect failure.
        rb.read();
        assertFalse(rb.containsArray(testData));
        
        // write all the same element and expect failure
        for (int i=0;i<rb.maxSize();i++) {
            rb.add(testData[0]);
        }
        assertFalse(rb.containsArray(testData));
        assertFalse(rb.containsArray(biggerTestData));
        
        // write all the same second element and expect failure
        for (int i=0;i<rb.maxSize();i++) {
            rb.add(testData[1]);
        }
        assertFalse(rb.containsArray(testData));
        assertFalse(rb.containsArray(biggerTestData));
        
        // clear and only write some elements and expect failure
        rb.clear();
        for (int i=0;i< testData.length - 2 ;i++) {
            rb.add(testData[i]);
        }
        assertFalse(rb.containsArray(testData));
    }
}
