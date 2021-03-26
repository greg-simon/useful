package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import au.id.simo.useful.test.DataGenFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the InputStream api, useful for InputStream implementations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface FilterInputStreamTest {
    
    /**
     * Used in unit tests to create the FilterInputStream implementation with
     * known test data via the provided InputStream.
     * @param in Source of known test data.
     * @return the FilterInputStream implementation to be tested.
     */
    FilterInputStream create(InputStream in);
    
    @Test
    default void test_read() throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(6));
        assertEquals(0, fin.read());
        assertEquals(1, fin.read());
        assertEquals(2, fin.read());
        assertEquals(3, fin.read());
        assertEquals(4, fin.read());
        assertEquals(5, fin.read());
        assertEquals(-1, fin.read());
        assertEquals(-1, fin.read());
        assertEquals(-1, fin.read());
    }
    
    @Test
    default void test_readByteArray() throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(6));
        byte[] result = new byte[10];
        assertEquals(6, fin.read(result), "expect to read all 6 bytes.");
        
        assertEquals(0, result[0]);
        assertEquals(1, result[1]);
        assertEquals(2, result[2]);
        assertEquals(3, result[3]);
        assertEquals(4, result[4]);
        assertEquals(5, result[5]);
        assertEquals(0, result[6]); // no data written from here on
        assertEquals(0, result[7]);
        assertEquals(0, result[8]);
        assertEquals(0, result[9]);
        
        assertEquals(-1, fin.read(result), "expect end of stream marker");
    }
    
    @Test
    default void test_readByteArrayOffsetLength() throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(6));
        byte[] result = new byte[10];
        assertEquals(6, fin.read(result,4,6), "expect to read all 6 bytes from 4th array slot.");
        
        assertEquals(0, result[0], "No data at start of array");
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
        assertEquals(0, result[3]);
        assertEquals(0, result[4], "data read into result array from here on");
        assertEquals(1, result[5]);
        assertEquals(2, result[6]);
        assertEquals(3, result[7]);
        assertEquals(4, result[8]);
        assertEquals(5, result[9]);
        
        assertEquals(-1, fin.read(result), "expect end of stream marker");
    }
    
    @ParameterizedTest
    @MethodSource("readByteArrayOffsetLengthArgs")
    default <T extends Throwable> void test_readByteArrayOffsetLength(int bufLength, int offset, int readLength, long testDataLimit, Class<T> expectThrownException) throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(testDataLimit));
        if (expectThrownException != null) {
            assertThrows(expectThrownException, () -> {
                byte[] bytBuf = new byte[bufLength];
                fin.read(bytBuf, offset, readLength);
            });
            return;
        }
        
        byte[] bytBuf = new byte[bufLength];
        long totalRead=0;
        int readCount;
        while((readCount = fin.read(bytBuf, offset, readLength))!=-1) {
            // verify expected results
            for(int i=0; i<readCount;i++) {
                assertEquals(bytBuf[i+offset], (byte)DataGenFactory.expectedByte(totalRead + i));
            }
            totalRead += readCount;
        }
    }
    
    static Stream<Arguments> readByteArrayOffsetLengthArgs() {
        return Stream.of(
                // bufLength, offset, readLen, testDataLimit, expectedException
                Arguments.of(32,  0, 32, 100, null),
                Arguments.of(32, -1, 32, 100, IndexOutOfBoundsException.class),
                Arguments.of(10,  0, 11, 100, IndexOutOfBoundsException.class),
                Arguments.of(10, 11,  1, 100, IndexOutOfBoundsException.class),
                Arguments.of(10,  9,  2, 100, IndexOutOfBoundsException.class),
                Arguments.of(-1,  0,  5, 100, NegativeArraySizeException.class)
        );
    }
    
    @Test
    default void test_skip() throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(20));
        assertEquals(10, fin.skip(10)); // 10 bytes remaining
        assertEquals(10, fin.read());   //  9 bytes remaining
        assertEquals(7, fin.skip(7));   //  2 byets remaining
        assertEquals(2, fin.skip(10));  //  end of stream after first 2 bytes
    }
    
    @Test
    default void test_close() throws IOException {
        FilterInputStream fin = create(DataGenFactory.incrementingBytes(6));
        fin.close();
        
        IOException ioe;
        ioe = assertThrows(IOException.class, () -> {
            fin.read();
        });
        assertEquals("Stream Closed", ioe.getMessage());
        ioe = assertThrows(IOException.class, () -> {
            fin.read(new byte[2]);
        });
        assertEquals("Stream Closed", ioe.getMessage());
        ioe = assertThrows(IOException.class, () -> {
            fin.read(new byte[2],0,2);
        });
        assertEquals("Stream Closed", ioe.getMessage());
        ioe = assertThrows(IOException.class, () -> {
            fin.skip(10);
        });
        assertEquals("Stream Closed", ioe.getMessage());
    }
}
