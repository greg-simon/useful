package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LimitedInputStreamTest {

    private final byte[] testData = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    };
    
    @Test
    public void testConstructorWithCountingStream() throws IOException {
        ByteArrayInputStream testDataIn = new ByteArrayInputStream(testData);
        CountingInputStream cin = new CountingInputStream(testDataIn);
        LimitedInputStream lin = new LimitedInputStream(cin, 1);
        assertEquals(1, lin.read());
        assertEquals(-1, lin.read());
    }

    @Test
    public void testRead() throws Exception {
        ByteArrayInputStream testDataIn = new ByteArrayInputStream(testData);
        InputStream in = new LimitedInputStream(testDataIn, 5);

        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
        assertEquals(4, in.read());
        assertEquals(5, in.read());
        assertEquals(-1, in.read());
    }
    
    @Test
    public void testClose() throws Exception {
        ByteArrayInputStream testDataIn = new ByteArrayInputStream(testData) {
            boolean closed = false;
            @Override
            public void close() throws IOException {
                closed = true;
            }

            @Override
            public synchronized int read() {
                if (closed) {
                    throw new IllegalStateException("Stream already closed.");
                }
                return super.read();
            }
        };
        InputStream in = new LimitedInputStream(testDataIn, 5);
        in.close();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            in.read();
        });
        assertEquals("Stream already closed.", ex.getMessage());
    }

    /**
     * Byte limit and array size doesn't fit exactly.
     * @throws Exception 
     */
    @Test
    public void testRead3args_odd() throws Exception {
        ByteArrayInputStream testDataIn = new ByteArrayInputStream(testData);
        InputStream in = new LimitedInputStream(testDataIn, 7);
        byte[] buf = new byte[2];

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(1, buf[0]);
        assertEquals(2, buf[1]);

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(3, buf[0]);
        assertEquals(4, buf[1]);

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(5, buf[0]);
        assertEquals(6, buf[1]);

        assertEquals(1, in.read(buf, 0, buf.length));
        assertEquals(7, buf[0]);
    }
    
    /**
     * Array size and byte limit fits exactly with no remainders.
     * @throws Exception 
     */
    @Test
    public void testRead3args_even() throws Exception {
        ByteArrayInputStream testDataIn = new ByteArrayInputStream(testData);
        InputStream in = new LimitedInputStream(testDataIn, 8);
        byte[] buf = new byte[2];

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(1, buf[0]);
        assertEquals(2, buf[1]);

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(3, buf[0]);
        assertEquals(4, buf[1]);

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(5, buf[0]);
        assertEquals(6, buf[1]);

        assertEquals(2, in.read(buf, 0, buf.length));
        assertEquals(7, buf[0]);
        assertEquals(8, buf[1]);
        
        //end of stream
        assertEquals(-1, in.read(buf, 0, buf.length));
    }
}
