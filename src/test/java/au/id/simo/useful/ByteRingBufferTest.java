package au.id.simo.useful;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import au.id.simo.useful.io.ConcatInputStream;
import au.id.simo.useful.io.LimitedInputStream;
import au.id.simo.useful.test.DataGenFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        assertTrue(rb.isNotFull());

        rb.add(1);
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1}, rb.toArray());

        rb.add(2);
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1, 2}, rb.toArray());

        rb.add(3);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isFull());
        assertFalse(rb.isNotFull());
        assertTrue(rb.isNotEmpty());
        assertEquals(1, rb.peek());
        assertArrayEquals(new byte[]{1, 2, 3}, rb.toArray());

        rb.add(4);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertFalse(rb.isNotFull());
        assertEquals(2, rb.peek());
        assertArrayEquals(new byte[]{2, 3, 4}, rb.toArray());

        rb.add(5);
        assertEquals(3, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertTrue(rb.isFull());
        assertFalse(rb.isNotFull());
        assertEquals(3, rb.peek());
        assertArrayEquals(new byte[]{3, 4, 5}, rb.toArray());

        // removing values
        assertEquals(3, rb.read());
        assertEquals(2, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotFull());
        assertArrayEquals(new byte[]{4, 5}, rb.toArray());

        assertEquals(4, rb.read());
        assertEquals(1, rb.size());
        assertFalse(rb.isEmpty());
        assertTrue(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotFull());
        assertArrayEquals(new byte[]{5}, rb.toArray());

        assertEquals(5, rb.read());
        assertEquals(0, rb.size());
        assertTrue(rb.isEmpty());
        assertFalse(rb.isNotEmpty());
        assertFalse(rb.isFull());
        assertTrue(rb.isNotFull());
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
    
    @ParameterizedTest
    @MethodSource("testReadArraySource")
    public void testReadArray(int bufferSize, int dataSize, int initialData) throws IOException {
        ByteRingBuffer rb = new ByteRingBuffer(bufferSize);
        System.out.println(bufferSize + ":" +dataSize+":"+initialData);
        InputStream initData = DataGenFactory.incrementingBytes();
        for(int i=0;i<initialData; i++) {
            rb.add(initData.read());
        }
        // fill with data
        InputStream data = DataGenFactory.incrementingBytes();
        for (int b = 0; b < dataSize; b++) {
            rb.add(data.read());
        }
        
        // read whole array
        byte[] newbuf = new byte[bufferSize];
        System.out.println(rb);
        int readCount = rb.read(newbuf, 0, bufferSize);
        assertEquals(bufferSize, readCount);
        
        // what is the expected data read?
        // [<-intialData-><-data->]
        // now only take the last <buffer size> of all that as the rest has been overwritten
        InputStream expectedData = new ConcatInputStream(
                new LimitedInputStream(DataGenFactory.incrementingBytes(), initialData),
                new LimitedInputStream(DataGenFactory.incrementingBytes(), dataSize)
        );
        int skipBytes = Math.max(0, initialData + dataSize - bufferSize);
        expectedData.skip(skipBytes);
        for (int i = bufferSize; i < bufferSize; i++) {
            byte expectedByte = (byte) expectedData.read();
            byte value = newbuf[i];
            if (expectedByte != value) {
                System.out.println("eh?");
            }
            assertEquals(expectedByte, value);
        }
    }
    
    @ParameterizedTest
    @MethodSource("testReadArraySource")
    public void testReadArray_segments(int bufferSize, int dataSize, int initialData) {
        ByteRingBuffer rb = new ByteRingBuffer(bufferSize);
        
        // fill with data
        for (int b = 0; b < dataSize; b++) {
            rb.put(data(b));
        }
        
        //find near enough to midpoint
        int half = dataSize / 2;
        
        // read half whole array
        byte[] newbuf = new byte[dataSize];
        int readCount = rb.read(newbuf, 0, half);
        assertEquals(half, readCount);
        for (int expected = 0; expected < half; expected++) {
            assertEquals(data(expected), newbuf[expected]);
        }
        assertEquals(dataSize - half, rb.size());
        
        // add a quarter more data to create data at each end of array
        int quarter = dataSize / 4;
        for (int i = 0; i < quarter; i++) {
            rb.add(data(dataSize + i));
        }
        
        readCount = rb.read(newbuf, 0, rb.size());
        assertEquals(dataSize - half + quarter, readCount);

        int expectedIndex = 0;
        for (; expectedIndex < half; expectedIndex++) {
            assertEquals(data(expectedIndex + half), newbuf[expectedIndex]);
        }
        for (int i = 0; i < quarter; i++) {
            assertEquals(data(expectedIndex + half + i), newbuf[expectedIndex + i]);
        }
    }
    
    public static byte data(int i) {
        return (byte) (i % Byte.MAX_VALUE);
    }
    
    public static Stream<Arguments> testReadArraySource() {
        //int bufferSize, int dataSize, int initialData
        return Stream.of(
                Arguments.of(10, 8, 2),
                Arguments.of(3, 3, 2),
                Arguments.of(6, 6, 2),
                Arguments.of(2, 2, 1),
                Arguments.of(4096, 4096, 150)
        );
    }

    @Test
    public void testToString() {
        ByteRingBuffer rb = new ByteRingBuffer(3);
        assertEquals("ByteRingBuffer[+-0,0,0]",rb.toString());
        rb.add(1);
        assertEquals("ByteRingBuffer[-1,+0,0]",rb.toString());
        rb.add(2);
        assertEquals("ByteRingBuffer[-1,2,+0]",rb.toString());
        rb.read();
        assertEquals("ByteRingBuffer[0,-2,+0]",rb.toString());
        rb.add(3);
        assertEquals("ByteRingBuffer[+0,-2,3]",rb.toString());
        rb.add(4);
        assertEquals("ByteRingBuffer[4,+-2,3]",rb.toString());
        rb.read();
        assertEquals("ByteRingBuffer[4,+0,-3]",rb.toString());
    }
    
    @Test
    public void testGetFreeSpace() {
        ByteRingBuffer rb = new ByteRingBuffer(5);
        assertEquals(5, rb.getFreeSpace());
        rb.add(0);
        assertEquals(4, rb.getFreeSpace());
        rb.add(0);
        assertEquals(3, rb.getFreeSpace());
        rb.add(0);
        assertEquals(2, rb.getFreeSpace());
        rb.add(0);
        assertEquals(1, rb.getFreeSpace());
        rb.add(0);
        assertEquals(0, rb.getFreeSpace());
        assertTrue(rb.isFull());
        
        rb.add(0);
        assertEquals(0, rb.getFreeSpace());
        assertTrue(rb.isFull());
        rb.add(0);
        assertEquals(0, rb.getFreeSpace());
        assertTrue(rb.isFull());
        
        rb.read();
        assertEquals(1, rb.getFreeSpace());
        assertFalse(rb.isFull());
    }
    
    public class DataGen {
        private int counter;

        public DataGen() {
            counter = 0;
        }
        
        public void setNext(int value) {
            this.counter = value;
        }
        
        public byte next() {
            return (byte) (counter++ % Byte.MAX_VALUE);
        }
    }
}
