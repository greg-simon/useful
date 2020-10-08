package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class CountingInputStreamTest {
    /**
     * Test of getByteCount method, of class SizeCounterInputStream.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRead() throws IOException {
        byte[] bytes = "This is a counting test.".getBytes("UTF-8");
        CountingInputStream cin = new CountingInputStream(new ByteArrayInputStream(bytes));
        while (cin.read() >= 0) {}
        assertEquals(bytes.length, cin.getByteCount());
    }

    /**
     * Test of getByteCount method, of class SizeCounterInputStream.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testReadByteArray() throws IOException {
        byte[] bytes = "This is a counting test.".getBytes("UTF-8");
        CountingInputStream cin = new CountingInputStream(new ByteArrayInputStream(bytes));
        byte[] buffer = new byte[2];
        while (cin.read(buffer) >= 0) {}
        assertEquals(bytes.length, cin.getByteCount());
    }
    
    @Test
    public void testReadByteArrayIndexLength() throws IOException {
        byte[] bytes = "This is a counting test.".getBytes("UTF-8");
        CountingInputStream cin = new CountingInputStream(new ByteArrayInputStream(bytes));
        byte[] buffer = new byte[2];
        while (cin.read(buffer, 0, 2) >= 0) {}
        assertEquals(bytes.length, cin.getByteCount());
    }
}
