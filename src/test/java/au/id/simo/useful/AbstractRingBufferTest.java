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
 * @param <T> the type this ring buffer stores
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
    default void testIncrementIndex() {
        AbstractRingBuffer rb = createRingBuffer(3);
        assertEquals(1, rb.incrementIndex(0, 1));
        assertEquals(2, rb.incrementIndex(1, 1));
        assertEquals(0, rb.incrementIndex(2, 1));
        assertEquals(1, rb.incrementIndex(3, 1));
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
    
    public static Stream<Arguments> containsArrayTests() {
        return Stream.of(
            // testDataSize, arrayOffset, arrayLength, nullLastXInArray, rbCapacity, expectedResult
            // locate exect contents of the buffer.
            Arguments.of(10, 0, 10, 0, 10, true),
            // locate 2 elements near the middle of buffer.
            Arguments.of(10, 5, 2, 0, 10, true),
            // locate 5 elements that start near end of buffer array and wrap to start of it.
            Arguments.of(15, 7, 5, 0, 10, true),
            // locate some elements but not all of them.
            Arguments.of(10, 5, 10, 0, 10, false),
            // locate none of the desired elements.
            Arguments.of(10, 20, 10, 0, 10, false),
            // fail due to more desired elements than buffer contains.
            Arguments.of(5, 0, 10, 0, 10, false),
            // locate the start of the desired array, but not the 3 nulls at the end.
            Arguments.of(10, 0, 10, 3, 10, false)
        );
    }
    
    /**
     * 
     * @param testDataSize generates test data with this many elements, and adds
     * it to the RingBuffer.
     * @param arrayOffset the generated the array to locate, has test data
     * starting from this offset.
     * @param arrayLength the generated array to locate, has this many elements
     * and is of the same length.
     * @param nullLastXInArray the generated array to locate has this many
     * elements at the end of it zeroed out using
     * {@link AbstractRingBuffer#nullValue}.
     * @param rbCapacity the capacity of the RingBuffer.
     * @param expectedResult the expected result of
     * {@link AbstractRingBuffer#containsArray(T[])}.
     */
    @ParameterizedTest
    @MethodSource("containsArrayTests")
    default void testContainsArray(int testDataSize, int arrayOffset, int arrayLength, int nullLastXInArray, int rbCapacity, boolean expectedResult) {
        T[] testData = testData(testDataSize);
        AbstractRingBuffer<T> rb = createRingBuffer(rbCapacity);
        for(T e: testData) {
            rb.add(e);
        }
        // create array to locate
        T[] locateTestData = testData(arrayOffset + arrayLength);
        T[] locateArray = zeroedArray(arrayLength, rb.nullValue);
        System.arraycopy(locateTestData, arrayOffset, locateArray, 0, arrayLength - nullLastXInArray);
        
        assertEquals(expectedResult, rb.containsArray(locateArray));
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
    
    public static Stream<Arguments> toStringArgs() {
        return Stream.of(
            // testDataSize, bufferCapacity, className, showNullValue, maxPrint
            Arguments.of(0, 10, "RingBuffer", " ", 2),
            Arguments.of(10, 10, "RingBuffer", " ", 2),
            Arguments.of(0, 10, "blahblah", "-", 2),
            Arguments.of(1, 5, "blahblah", "-", 10),
            Arguments.of(1, 5, "blahblah", "", 10)
        );
    }
    
    @ParameterizedTest
    @MethodSource("toStringArgs")
    default void testToString_3Args(int testDataSize, int bufferCapacity, String className, String showNullValue, int maxPrint) {
        T[] testData = testData(testDataSize);
        AbstractRingBuffer<T> rb = createRingBuffer(bufferCapacity);
        for (T e : testData) {
            rb.add(e);
        }
        
        StringBuilder expectedSB = new StringBuilder();
        expectedSB.append(className);
        expectedSB.append("[");
        int loopCount = Math.min(rb.capacity, maxPrint);
        for (int i = 0; i < loopCount; i++) {
            // head plus first
            if (rb.head == i) {
                expectedSB.append('+');
            }
            // then tail minus
            if (rb.tail == i) {
                expectedSB.append('-');
            }
            
            if (i < testData.length) {
                expectedSB.append(String.valueOf(testData[i]));
            } else {
                expectedSB.append(showNullValue);
            }
            expectedSB.append(",");
        }
        expectedSB.deleteCharAt(expectedSB.length()-1);
        if (maxPrint < rb.capacity) {
            expectedSB.append("...");
        }
        expectedSB.append(']');
        assertEquals(expectedSB.toString(), rb.toString(className, showNullValue, maxPrint));
    }
    
    @Test
    default void testClear_Size_Capacity_getFreeSpace() {
        T[] testData = testData(3);
        AbstractRingBuffer<T> rb = createRingBuffer(3);
        assertEquals(0, rb.size());
        assertEquals(3, rb.getFreeSpace());
        assertEquals(3, rb.capacity());
        
        rb.add(testData[0]);
        assertEquals(1, rb.size());
        assertEquals(2, rb.getFreeSpace());
        assertEquals(3, rb.capacity());
        rb.add(testData[1]);
        assertEquals(2, rb.size());
        assertEquals(1, rb.getFreeSpace());
        assertEquals(3, rb.capacity());
        rb.add(testData[2]);
        assertEquals(3, rb.size());
        assertEquals(0, rb.getFreeSpace());
        assertEquals(3, rb.capacity());
        
        rb.clear();
        assertEquals(0, rb.size());
        assertEquals(3, rb.getFreeSpace());
        assertEquals(3, rb.capacity());
    }
}
