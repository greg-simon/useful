package au.id.simo.useful.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class RingBufferTest implements AbstractRingBufferTest<Integer> {

    @Override
    public Integer[] testData(int arrayLength) {
        Integer[] testData = new Integer[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            testData[i] = i;
        }
        return testData;
    }

    @Override
    public AbstractRingBuffer<Integer> createRingBuffer(int capacity) {
        return new RingBuffer<>(capacity);
    }
    
    @Test
    public void testToString_Capacity3() {
        RingBuffer<String> rb = new RingBuffer<>(3);
        assertEquals("RingBuffer[+-null,null,null]", rb.toString());
        rb.add("one");
        assertEquals("RingBuffer[-one,+null,null]", rb.toString());
        rb.add("two");
        assertEquals("RingBuffer[-one,two,+null]", rb.toString());
        rb.add("three");
        assertEquals("RingBuffer[+-one,two,three]", rb.toString());
        rb.add("four");
        assertEquals("RingBuffer[four,+-two,three]", rb.toString());
    }
    @Test
    public void testToString_Capacity6() {
        RingBuffer<Integer> rb = new RingBuffer<>(6);
        rb.add(1);
        rb.add(2);
        rb.add(3);
        rb.add(4);
        rb.add(5);
        rb.add(6);
        assertEquals("RingBuffer[+-1,2,3,4,5...]", rb.toString());
    }
}
