package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import au.id.simo.useful.datagen.DataGenFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface ByteBundleTest extends InputStreamTest, ResourceTest {

    ByteBundle newByteBundle(int initialCapacity, int maxCapacity);

    @Override
    default InputStream create(InputStream in) {
        ByteBundle bb = newByteBundle(10, ByteBundle.MAX_ARRAY_SIZE);
        try {
            bb.copyIn(0, in);
        } catch (IOException ex) {
            fail(ex);
        }
        return bb.inputStream();
    }

    @Override
    public default Resource createResource(byte[] testData, Charset charset) throws IOException {
        ByteBundle bb = newByteBundle(testData.length, ByteBundle.MAX_ARRAY_SIZE);
        bb.copyIn(0, testData);
        return bb;
    }
    
    @Test
    default void testSize() {
        ByteBundle bb = newByteBundle(50, 60);
        assertEquals(0, bb.size());
        bb.append(new byte[]{0, 1, 2, 3, 4});
        assertEquals(5, bb.size());
    }
    
    @Test
    default void testCapacity() {
        ByteBundle bb = newByteBundle(50, 60);
        assertEquals(50, bb.capacity());
    }
    
    @Test
    default void testMaxCapacity() {
        ByteBundle bb = newByteBundle(50, 60);
        assertEquals(60, bb.maxCapacity());
    }
    
    @Test
    default void testTrim() {
        ByteBundle bb = newByteBundle(5, 100);
        bb.trim();
        assertEquals(0, bb.size());
        assertEquals(0, bb.capacity());
        
        bb.append(new byte[]{0,1,2});
        bb.trim();
        assertEquals(3, bb.size());
        assertEquals(3, bb.capacity());
    }
    
    @Test
    default void testClear() {
        ByteBundle bb = newByteBundle(5, 10);
        assertEquals(0, bb.size());
        bb.append(new byte[]{1,2,3,4,5});
        assertEquals(5, bb.size());
        bb.clear();
        assertEquals(0, bb.size());
    }
    
    @Test
    default void testCopyOut_dest() {
        ByteBundle bb = newByteBundle(5, 10);
        bb.append(new byte[]{1, 2, 3, 4, 5});

        byte[] buf;

        buf = new byte[4];
        assertEquals(4, bb.copyOut(buf));
        assertArrayEquals(new byte[]{1, 2, 3, 4}, buf);

        buf = new byte[5];
        assertEquals(5, bb.copyOut(buf));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, buf);

        buf = new byte[6];
        assertEquals(5, bb.copyOut(buf));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 0}, buf);
    }

    @Test
    default void testCopyOut_pos_dest() {
        // generate data
        byte[] testData = new byte[]{1,2,3,4,5,6,7,8,9,10};
        ByteBundle bb = newByteBundle(5, 10);
        bb.append(testData);

        byte[] outBuf = new byte[20];
        assertEquals(10, bb.copyOut(0, outBuf));
        for (int i=0;i<10;i++) {
            assertEquals(testData[i], outBuf[i], String.format("byte No: %d was different", i));
        }
    }
    
    @Test
    default void testAppend() {
        ByteBundle bb = newByteBundle(10, 20);
        bb.append(new byte[]{1,2,3,4,5});
        assertEquals(5, bb.size());
        bb.append(new byte[]{1,2,3,4,5});
        assertEquals(10, bb.size());
        
        byte[] output = new byte[10];
        assertEquals(10, bb.copyOut(output));
        assertArrayEquals(
                new byte[]{1,2,3,4,5,1,2,3,4,5},
                output
        );
    }
    
    @Test
    default void testAppend_srcPos_length() {
        byte[] testData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteBundle bb = newByteBundle(4, 10);
        bb.append(testData, 0, 5);
        assertEquals(5, bb.size());
        bb.append(testData, 5, 5);
        assertEquals(10, bb.size());
        
        byte[] output = new byte[10];
        assertEquals(10, bb.copyOut(output));
        assertArrayEquals(
                testData,
                output
        );
    }
    
    @Test
    default void testCopyIn_pos_InputStream() throws IOException {
        ByteBundle bb = newByteBundle(10, 100);
        int copiedIn = 100;
        bb.copyIn(0, DataGenFactory.incrementingBytes(copiedIn));
        assertEquals(100, copiedIn);
        assertEquals(100, bb.size());
        
        try (InputStream in = bb.inputStream()) {
            for (int i = 0; i < 100; i++) {
                assertEquals(DataGenFactory.expectedByte(i), in.read());
            }
            assertEquals(-1, in.read()); // end of stream
        }
    }
    
    @Test
    default void testCopyIn_pos_InputStream_overMaxLimit() throws IOException {
        ByteBundle bb = newByteBundle(10, 100);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bb.copyIn(0, DataGenFactory.incrementingBytes(bb.maxCapacity() + 10));
        });
    }
    
    @Test
    default void testCopyIn() {
        ByteBundle bb = newByteBundle(5, 5);
        byte[] data  = new byte[]{1,2,3,4,5,6};
        
        bb.copyIn(0, data, 0, 4);
        bb.copyIn(0, data, 0, 5);
        assertThrows(NullPointerException.class     , () -> bb.copyIn( 0, null,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 0, data,  0, 6));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn(-1, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 0, data, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 0, data,  0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 6, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 0, data,  10, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 4, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyIn( 0, data,  4, 5));
    }
    
    @Test
    default void testCopyOut() {
        ByteBundle bb = newByteBundle(5, 5);
        bb.append(new byte[]{1,2,3,4,5});
        
        byte[] data = new byte[5];
        assertEquals(5, bb.copyOut(0, data, 0, bb.size()));
        assertArrayEquals(new byte[]{1,2,3,4,5}, data);
        zeroArray(data);
        assertEquals(4, bb.copyOut(1, data, 0, 4));
        assertArrayEquals(new byte[]{2,3,4,5,0}, data);
        zeroArray(data);
        assertEquals(1, bb.copyOut(4, data, 4, 1));
        assertArrayEquals(new byte[]{0,0,0,0,5}, data);
        
        zeroArray(data);
        assertThrows(NullPointerException.class     , () -> bb.copyOut( 0, null,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 0, data,  0, 6));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut(-1, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 0, data, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 0, data,  0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 6, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 0, data,  10, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 4, data,  0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bb.copyOut( 0, data,  4, 5));
    }
    
    @Test
    default void testNegMaxCapacity() {
        ByteBundle bb = newByteBundle(0, -1);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            bb.append(new byte[1]);
        });
    }
    
    default void zeroArray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }
}
