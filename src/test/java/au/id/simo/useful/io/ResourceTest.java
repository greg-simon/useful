package au.id.simo.useful.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import au.id.simo.useful.Defer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Only the abstract Resource class methods are tested here.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface ResourceTest {
    
    Resource createResource(byte[] testData, Charset charset) throws IOException;
    
    default Resource createResource(byte[] testData) throws IOException {
        return createResource(testData, StandardCharsets.UTF_8);
    }
    
    default Stream<Arguments> charsets() {
        return Stream.of(
                Arguments.of(StandardCharsets.ISO_8859_1),
                Arguments.of(StandardCharsets.US_ASCII),
                Arguments.of(StandardCharsets.UTF_16),
                Arguments.of(StandardCharsets.UTF_16BE),
                Arguments.of(StandardCharsets.UTF_16LE),
                Arguments.of(StandardCharsets.UTF_8)
        );
    }

    @Test
    default void testGetReader() throws Exception {
        String testStr = "This is a test for the reader() method";
        Resource r = createResource(testStr.getBytes());
        Reader reader = r.getReader();
        for (int i = 0; i < testStr.length(); i++) {
            assertEquals(
                    testStr.charAt(i),
                    (char) reader.read(),
                    "Comparing char No. " + i
            );
        }
    }
    
    @ParameterizedTest
    @MethodSource("charsets")
    default void testGetReader_Charset(Charset charset) throws Exception {
        String testStr = "This is a test for the reader() method";
        Resource r = createResource(testStr.getBytes(charset), charset);
        Reader reader = r.getReader(charset);
        for (int i = 0; i < testStr.length(); i++) {
            assertEquals(
                    testStr.charAt(i),
                    (char) reader.read(),
                    "Comparing char No. " + i
            );
        }
    }

    @Test
    default void testCopyTo() throws Exception {
        String testStr = "This is some data to test copyTo()";
        Resource r = createResource(testStr.getBytes());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        long copyByteCount = r.copyTo(bout);
        byte[] testBytes = testStr.getBytes();
        byte[] copyBytes = bout.toByteArray();

        assertEquals(testBytes.length, copyByteCount);
        assertArrayEquals(testBytes, copyBytes);
    }

    @Test
    default void testInputStream() throws Exception {
        String testStr = "This is a test for the inputStream() method";
        Resource r = createResource(testStr.getBytes());

        byte[] testBytes = testStr.getBytes();
        try (InputStream in = r.inputStream()) {
            for (int i = 0; i < testBytes.length; i++) {
                assertEquals(
                        testBytes[i],
                        (byte) in.read(),
                        "Comparing byte index: " + i
                );
            }
        }
    }
    
    @Test
    default void testMultipleInputStreams() throws Exception {
        String testStr = "This is a test for the inputStream() method";
        Resource r = createResource(testStr.getBytes());

        byte[] testBytes = testStr.getBytes();
        try (Defer defer = new Defer()) {
            InputStream in1 = defer.close(r.inputStream());
            InputStream in2 = defer.close(r.inputStream());

            for (int i = 0; i < testBytes.length; i++) {
                assertEquals(
                        testBytes[i],
                        (byte) in1.read(),
                        "Comparing in1, index: " + i
                );
                assertEquals(
                        testBytes[i],
                        (byte) in2.read(),
                        "Comparing in2, index: " + i
                );
            }
        }
    }
}
