package au.id.simo.useful.io;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LimitedOutputStreamTest {

    private static final byte[] TEST_DATA = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    };

    @Test
    public void testWrite3args() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LimitedOutputStream lout = new LimitedOutputStream(bout, 6);

        // write 10,11,12,13,14,15,16 but 16 should be thrown away.
        lout.write(TEST_DATA, 9, 7);
        lout.flush();

        byte[] resultBytes = bout.toByteArray();
        assertEquals(6, resultBytes.length);
        assertEquals(10, resultBytes[0]);
        assertEquals(11, resultBytes[1]);
        assertEquals(12, resultBytes[2]);
        assertEquals(13, resultBytes[3]);
        assertEquals(14, resultBytes[4]);
        assertEquals(15, resultBytes[5]);
    }

    @Test
    public void testWriteByteArr() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LimitedOutputStream lout = new LimitedOutputStream(bout, 3);
        lout.write(TEST_DATA);

        byte[] resultBytes = bout.toByteArray();
        assertEquals(3, resultBytes.length);
        assertEquals(1, resultBytes[0]);
        assertEquals(2, resultBytes[1]);
        assertEquals(3, resultBytes[2]);

    }

    @Test
    public void testWriteInt() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LimitedOutputStream lout = new LimitedOutputStream(bout, 3);
        lout.write(1);
        lout.write(2);
        lout.write(3);
        lout.write(4);

        byte[] resultBytes = bout.toByteArray();
        assertEquals(3, resultBytes.length);
        assertEquals(1, resultBytes[0]);
        assertEquals(2, resultBytes[1]);
        assertEquals(3, resultBytes[2]);
    }
}
