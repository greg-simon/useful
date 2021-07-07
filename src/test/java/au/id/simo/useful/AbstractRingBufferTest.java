package au.id.simo.useful;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface AbstractRingBufferTest<T> {
    
    T[] testData(int arrayLength);
    AbstractRingBuffer<T> createRingBuffer(int capacity);
    default T[] zeroedArray(int arrayLength, T nullValue) {
        T[] array = testData(arrayLength);
        for (int i = 0; i < array.length; i++) {
            array[i] = nullValue;
        }
        return array;
    }
    
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
        
        assertEquals(testData.length, rb.size(),
                "Should not be more items than the testData");
        
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
    
    public static Stream<Arguments> readAndPeek3ArgsSource() {
        return Stream.of(
            // testDataSize, destArraySize, destOffset, length, expectedReadLength, expectedException
            // happy path
            Arguments.of(10, 10, 0, 10, 10, null),
            Arguments.of(15, 10, 0, 10, 10, null),
            Arguments.of( 5, 10, 0, 10,  5, null),
            // error causing args
            Arguments.of(10,  5,  0, 10, 0, IndexOutOfBoundsException.class),
            Arguments.of(10,  5,  5,  1, 0, IndexOutOfBoundsException.class),
            Arguments.of(10,  5, -1,  1, 0, IndexOutOfBoundsException.class),
            Arguments.of(10,  5,  1, -1, 0, IndexOutOfBoundsException.class)
        );
    }
    
    @ParameterizedTest
    @MethodSource("readAndPeek3ArgsSource")
    default void read3Arg(int testDataSize, int destArraySize, int destOffset, int length, int expectedReadLength, Class<? extends Throwable> expectedException) {
        T[] testData = testData(testDataSize);
        AbstractRingBuffer<T> rb = createRingBuffer(testDataSize);
        for (int i = 0; i < testDataSize; i++) {
            rb.add(testData[i]);
        }
        T[] destArray = zeroedArray(destArraySize, rb.nullValue);

        if (expectedException != null) {
            assertThrows(expectedException, () -> {
                rb.read(destArray, destOffset, length);
            });
            return;
        }
        // no further exeptions expected
        int readLength = rb.read(destArray, destOffset, length);
        assertEquals(expectedReadLength, readLength, "readLength");
        for (int i = 0; i < readLength; i++) {
            assertEquals(testData[i], destArray[destOffset + i], String.format("destArray index: %s", i));
        }
        int remainingValues = testDataSize - expectedReadLength;
        assertEquals(remainingValues, rb.size());
    }

    @ParameterizedTest
    @MethodSource("readAndPeek3ArgsSource")
    default void peek3Arg(int testDataSize, int destArraySize, int destOffset, int length, int expectedReadLength, Class<? extends Throwable> expectedException) {
        T[] testData = testData(testDataSize);
        AbstractRingBuffer<T> rb = createRingBuffer(testDataSize);
        for (int i = 0; i < testDataSize; i++) {
            rb.add(testData[i]);
        }
        T[] destArray = zeroedArray(destArraySize, rb.nullValue);

        if (expectedException != null) {
            assertThrows(expectedException, () -> {
                rb.peek(destArray, destOffset, length);
            });
            return;
        }
        // no further exeptions expected
        int peekLength = rb.peek(destArray, destOffset, length);
        assertEquals(expectedReadLength, peekLength, "peekLength");
        for (int i = 0; i < peekLength; i++) {
            assertEquals(testData[i], destArray[destOffset + i], String.format("destArray index: %s", i));
        }
        assertEquals(testDataSize, rb.size());
    }
    
    @Test
    default void testRead() {
        T[] testData = testData(2);
        AbstractRingBuffer<T> rb = createRingBuffer(2);
        assertThrows(ArrayIndexOutOfBoundsException.class, rb::read);
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
        // TODO: Break up into individual tests, and expand error scenarios
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
    
    @Test
    default void testIteratorInForLoop() {
        T[] testData = testData(5);
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        // add test data
        for(T element: testData) {
            rb.add(element);
        }
        
        // loop buffer as an Iterable
        assertEquals(5, rb.size());
        int i = 0;
        for(T element: rb) {
            assertEquals(testData[i], element);
            i++;
        }
        assertEquals(5, i, "Verify for-each looped the expected number of times");
    }
    
    @Test
    default void testIterator() {
        T[] testData = testData(3);
        AbstractRingBuffer<T> rb = createRingBuffer(testData.length);
        // add test data
        for(T element: testData) {
            rb.add(element);
        }
        
        Iterator<T> itr = rb.iterator();
        assertTrue(itr.hasNext());
        assertEquals(testData[0], itr.next());
        assertTrue(itr.hasNext());
        assertEquals(testData[1], itr.next());
        assertTrue(itr.hasNext());
        assertEquals(testData[2], itr.next());
        assertThrows(NoSuchElementException.class, itr::next);
    }
}
