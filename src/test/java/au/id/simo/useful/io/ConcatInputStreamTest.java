package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ConcatInputStreamTest implements InputStreamTest {

    @Override
    public InputStream create(InputStream in) {
        return new ConcatInputStream(in);
    }

    @Test
    public void testRead() throws IOException {
        ConcatInputStream cin = new ConcatInputStream(
                new ByteArrayInputStream("Hello".getBytes()),
                new ByteArrayInputStream(" there".getBytes())
        );
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int i;
        while ((i = cin.read()) != -1) {
            bout.write(i);
        }
        assertEquals("Hello there", new String(bout.toByteArray()));
    }

    /**
     * Test of getByteCount method, of class SizeCounterInputStream.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testReadByteArray() throws IOException {
        ConcatInputStream cin = new ConcatInputStream(
                new ByteArrayInputStream("Hello".getBytes()),
                new ByteArrayInputStream(" there".getBytes())
        );
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int i;
        byte[] buf = new byte[2];
        while ((i = cin.read(buf, 0, buf.length)) != -1) {
            bout.write(buf, 0, i);
        }
        assertEquals("Hello there", new String(bout.toByteArray()));
    }
}
