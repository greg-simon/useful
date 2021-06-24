package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import au.id.simo.useful.Cleaner;
import au.id.simo.useful.test.DataGenFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class MultiOutputStreamTest {
    
    private static final long BYTE_COUNT = 1000;
    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of((Resource) () -> DataGenFactory.incrementingBytes(BYTE_COUNT))
        );
    }
    
    @ParameterizedTest
    @MethodSource("testData")
    public void testWrite_3args(Resource testData) throws Exception {
        try (Cleaner c = new Cleaner()) {
            MultiOutputStream mout = new MultiOutputStream(
                    c.closeLater(new AssertOutputStream("first", testData.inputStream(), BYTE_COUNT)),
                    c.closeLater(new AssertOutputStream("second", testData.inputStream(), BYTE_COUNT)),
                    c.closeLater(new AssertOutputStream("third", testData.inputStream(), BYTE_COUNT))
            );

            long bytesCopied = testData.copyTo(mout);
            assertEquals(BYTE_COUNT, bytesCopied);
        }
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testWrite_int(Resource testData) throws Exception {
        try (Cleaner c = new Cleaner()) {
            MultiOutputStream mout = new MultiOutputStream(
                    c.closeLater(new AssertOutputStream("first", testData.inputStream(), BYTE_COUNT)),
                    c.closeLater(new AssertOutputStream("second", testData.inputStream(), BYTE_COUNT)),
                    c.closeLater(new AssertOutputStream("third", testData.inputStream(), BYTE_COUNT))
            );

            InputStream in = testData.inputStream();
            int byteRead;
            long totalBytes = 0;
            while ((byteRead = in.read()) != -1) {
                mout.write(byteRead);
                totalBytes++;
            }
            assertEquals(BYTE_COUNT, totalBytes);
        }
    }

    @Test
    public void testClose() throws Exception {
        CountOutputStream cout = new CountOutputStream();
        MultiOutputStream mout = new MultiOutputStream(
                cout,
                cout,
                cout,
                cout
        );
        mout.close();
        assertEquals(4, cout.closeCount);
    }

    @Test
    public void testFlush() throws Exception {
        CountOutputStream cout = new CountOutputStream();
        MultiOutputStream mout = new MultiOutputStream(
                cout,
                cout,
                cout,
                cout,
                cout
        );
        mout.flush();
        assertEquals(5, cout.flushCount);
    }
    
    private class CountOutputStream extends OutputStream {

        int closeCount;
        int flushCount;
        
        @Override
        public void write(int b) throws IOException {
            
        }

        @Override
        public void flush() throws IOException {
            flushCount++;
        }

        @Override
        public void close() throws IOException {
            closeCount++;
        }
    }

    /**
     * Used to compare the data written with the provided input stream.
     */
    private class AssertOutputStream extends OutputStream {

        private final CountingInputStream in;
        private final String name;
        private final long expectedBytesWritten;

        public AssertOutputStream(String name, InputStream in, long expectedBytesWritten) {
            this.in = new CountingInputStream(in);
            this.name = name;
            this.expectedBytesWritten = expectedBytesWritten;
        }

        @Override
        public void write(int b) throws IOException {
            byte bByte = (byte) b;
            byte readByte = (byte) in.read();
            assertEquals(bByte, readByte, String.format("Stream: %s, Byte No: %s", name, in.getByteCount()));
        }

        @Override
        public void close() throws IOException {
            assertEquals(expectedBytesWritten, in.getByteCount(), "Verify all bytes were written.");
            in.close();
        }
    }
}
