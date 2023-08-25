package au.id.simo.useful.datagen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import au.id.simo.useful.io.Latch;
import au.id.simo.useful.io.LimitedInputStream;
import au.id.simo.useful.io.LimitedReader;

/**
 * Factory for obtaining test data generators.
 */
public class DataGenFactory {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz ";
    private static final byte[] CHARS_AS_BYTES = "abcdefghijklmnopqrstuvwxyz ".getBytes(StandardCharsets.UTF_8);
    public static final String STREAM_MSG = "Stream Closed";
    public static final String READER_MSG = "Reader Closed";
    
    /**
     * 
     * @return an InputStream that returns starts at zero and counts up for each
     * byte. Limited to Integer.MAX_VALUE by default (2GB).
     */
    public static InputStream incrementingBytes() {
        return incrementingBytes(Integer.MAX_VALUE);
    }
    
    public static InputStream incrementingBytes(long limit) {
        return new LimitedInputStream(new InputStream() {
            private final Latch closeStatus = new Latch(STREAM_MSG);
            private long counter=0;
            @Override
            public int read() throws IOException {
                closeStatus.throwIfClosed();
                return expectedByte(counter++);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closeStatus.close();
            }
        }, limit);
    }
    
    public static int expectedByte(long count) {
        int intValue = (int)(count % (((long)Integer.MAX_VALUE) + 1));
        return intValue & 0xff;
    }
    
    public static InputStream randomBytes() {
        return randomBytes(Integer.MAX_VALUE);
    }
    
    public static InputStream randomBytes(long limit) {
        Random random = new Random();
        return new LimitedInputStream(new InputStream() {
            private final Latch closeStatus = new Latch(STREAM_MSG);
            @Override
            public int read() throws IOException {
                closeStatus.throwIfClosed();
                return random.nextInt(256);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closeStatus.close();
            }           
        },limit);
    }

    public static InputStream ascendingCharsInputStream(long limit) {
        return new LimitedInputStream(new InputStream() {
            private final Latch closeStatus = new Latch(STREAM_MSG);
            private final RepeatingBytes repeatingBytes = new RepeatingBytes(CHARS_AS_BYTES);
            @Override
            public int read() throws IOException {
                closeStatus.throwIfClosed();
                return repeatingBytes.next() & 0xFF;
            }

            @Override
            public void close() throws IOException {
                closeStatus.close();
            }
        }, limit);
    }
    public static Reader ascendingChars(long limit) {
        return new LimitedReader(new Reader() {
            private final Latch closeStatus = new Latch(READER_MSG);
            private final RepeatingChars repeatingChars = new RepeatingChars(CHARS);

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                closeStatus.throwIfClosed();
                for (int i = 0; i < len; i++) {
                    cbuf[off + i] = repeatingChars.next();
                }
                return len;
            }

            @Override
            public int read() throws IOException {
                closeStatus.throwIfClosed();
                return repeatingChars.next();
            }

            @Override
            public void close() throws IOException {
                closeStatus.close();
            }
        }, limit);
    }
}
