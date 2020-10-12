package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class RecorderInputStreamTest {

    @Test
    public void testRead_3args() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 10);
        
        byte readBuf[] = new byte[10];
        rin.read(readBuf, 0, readBuf.length);
        
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        assertArrayEquals(expected, readBuf);
        assertArrayEquals(expected, rin.getReadByteArray());
    }
    
    @Test
    public void testRead_3args_limits() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 5);
        
        byte readBuf[] = new byte[3];
        while(rin.read(readBuf, 0, readBuf.length) != -1) {}
        
        byte[] expected = new byte[] {1,2,3,4,5};
        assertArrayEquals(expected, rin.getReadByteArray()); // limits respected
    }
    
    @Test
    public void testRead_3args_limitIn() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 20);
        
        byte readBuf[] = new byte[4];
        int read;
        int readSum = 0;
        while ((read = rin.read(readBuf, 0, readBuf.length)) != -1) {
            readSum += read;
        };
        
        assertEquals(10, readSum);
        byte[] expected = new byte[] {1,2,3,4,5,6,7,8,9,10};
        assertArrayEquals(expected, rin.getReadByteArray()); // limits respected
    }
    
    @Test
    public void testRead_3args_largeLimit() throws Exception {
        int streamLimit = 100_000;
        InputStream infiniteStream = new InputStream() {
            private long counter = 0;
            
            @Override
            public int read() throws IOException {
                if (counter >= streamLimit) {
                    return -1;
                }
                counter++;
                return 5;
            }
        };
        RecorderInputStream rin = new RecorderInputStream(infiniteStream, Integer.MAX_VALUE);
        
        byte readBuf[] = new byte[1024];
        int read;
        long readSum=0;
        while ((read = rin.read(readBuf, 0, readBuf.length)) != -1) {
            readSum+=read;
            //System.out.println(String.format("%d : %d", readSum, rin.getRecordedByteCount()));
        };
        assertEquals(streamLimit, rin.getRecordedByteCount());
    }

    @Test
    public void testRead_0args() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 10);
        while(rin.read()!=-1){};
        assertArrayEquals(testData, rin.getReadByteArray());
    }
    
    @Test
    public void testRead_0args_limitRec() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 3);
        while(rin.read()!=-1){};
        byte[] expected = new byte[]{1,2,3};
        assertArrayEquals(expected,rin.getReadByteArray());
    }
    
    @Test
    public void testRead_0args_limitInput() throws Exception {
        byte testData[] = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        ByteArrayInputStream bin = new ByteArrayInputStream(testData);
        RecorderInputStream rin = new RecorderInputStream(bin, 20);
        while(rin.read()!=-1){};
        byte[] expected = new byte[]{1,2,3,4,5,6,7,8,9,10};
        assertArrayEquals(expected,rin.getReadByteArray());
    }
}
