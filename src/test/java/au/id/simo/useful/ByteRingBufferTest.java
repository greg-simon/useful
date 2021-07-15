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
public class ByteRingBufferTest implements AbstractRingBufferTest<Byte>{

    @Override
    public Byte[] testData(int arrayLength) {
        Byte[] testData = new Byte[arrayLength];
        for(int i=0;i<arrayLength;i++) {
            testData[i] = data(i);
        }
        return testData;
    }

    @Override
    public AbstractRingBuffer<Byte> createRingBuffer(int capacity) {
        return new ByteRingBuffer(capacity);
    }

    @Test
    public void testContains() {
        String testStr = "testing 123";
        byte[] testArray = testStr.getBytes();
        ByteRingBuffer rb = new ByteRingBuffer(testArray.length + 1);
        for (byte b : testArray) {
            assertFalse(rb.contains(testArray));
            rb.add(b);
        }
        assertTrue(rb.contains(testArray));
    }
    
    @ParameterizedTest
    @MethodSource("testReadArraySource")
    public void testReadArray(int bufferSize, int dataSize, int initialData) throws IOException {
        ByteRingBuffer rb = new ByteRingBuffer(bufferSize);
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
