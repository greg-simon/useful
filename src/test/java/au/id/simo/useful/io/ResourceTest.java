package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Only the abstract Resource class methods are tested here.
 */
public class ResourceTest {

    @Test
    public void testGetString() throws Exception {
        String testStr = "This is a test, I repeat, this is a test";
        Resource r = new ResourceImpl(testStr.getBytes());
        assertEquals(testStr, r.getString());
    }

    @Test
    public void testGetReader() throws Exception {
        String testStr = "This is a test for the reader() method";
        Resource r = new ResourceImpl(testStr.getBytes());
        Reader reader = r.getReader();
        for (int i = 0; i < testStr.length(); i++) {
            assertEquals(
                    testStr.charAt(i),
                    (char) reader.read(),
                    "Comparing char No. " + i
            );
        }
    }

    @Test
    public void testGetBytes() throws Exception {
        String testStr = "This is a test for the bytes() method";
        Resource r = new ResourceImpl(testStr.getBytes());

        byte[] testBytes = testStr.getBytes();
        byte[] resourceBytes = r.getBytes();

        assertEquals(testBytes.length, resourceBytes.length);
        for (int i = 0; i < testBytes.length; i++) {
            assertEquals(
                    testBytes[i],
                    resourceBytes[i],
                    "Comparing byte No. " + i
            );
        }
    }

    @Test
    public void testCopyTo() throws Exception {
        String testStr = "This is some data to test copyTo()";
        Resource r = new ResourceImpl(testStr.getBytes());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        long copyByteCount = r.copyTo(bout);
        byte[] testBytes = testStr.getBytes();
        byte[] copyBytes = bout.toByteArray();

        assertEquals(testBytes.length, copyByteCount);
        assertEquals(testBytes.length, copyBytes.length);
        for (int i = 0; i < testBytes.length; i++) {
            assertEquals(
                    testBytes[i],
                    copyBytes[i],
                    "Comparing byte index: " + i
            );
        }
    }

    @Test
    public void testInputStream() throws Exception {
        String testStr = "This is a test for the inputStream() method";
        Resource r = new ResourceImpl(testStr.getBytes());

        byte[] testBytes = testStr.getBytes();
        InputStream in = r.inputStream();

        for (int i = 0; i < testBytes.length; i++) {
            assertEquals(
                    testBytes[i],
                    (byte) in.read(),
                    "Comparing byte index: " + i
            );
        }
    }

    public class ResourceImpl extends Resource {

        private final byte[] underlyingData;

        public ResourceImpl(byte[] underlyingData) {
            this.underlyingData = underlyingData;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return new ByteArrayInputStream(underlyingData);
        }
    }
}
