package au.id.simo.useful.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Random;

import au.id.simo.useful.io.LimitedInputStream;
import au.id.simo.useful.io.LimitedReader;

/**
 * Factory for obtaining test data generators.
 */
public class DataGenFactory {
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
            private final CloseState closeState = CloseState.stream();
            private long counter=0;
            @Override
            public int read() throws IOException {
                closeState.ensureOpen();
                return expectedByte(counter++);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closeState.close();
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
            private final CloseState closeState = CloseState.stream();
            @Override
            public int read() throws IOException {
                closeState.ensureOpen();
                return random.nextInt(256);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closeState.close();
            }           
        },limit);
    }
    
    public static Reader ascendingChars(long limit) {
        return new LimitedReader(new Reader() {
            private static final String CHARS = "abcdefghijklmnopqrstuvwxyz ";
            private final CloseState closeState = CloseState.reader();
            private volatile int index = 0;
            
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                closeState.ensureOpen();
                for(int i=0;i<len;i++) {
                    index = nextIndex(index);
                    cbuf[off + i] = CHARS.charAt(index);
                }
                return len;
            }

            @Override
            public int read() throws IOException {
                closeState.ensureOpen();
                index = nextIndex(index);
                return CHARS.charAt(index);
            }
            
            private int nextIndex(int currentIndex) {
                return (currentIndex + 1) % CHARS.length();
            }

            @Override
            public void close() throws IOException {
                closeState.close();
            }
        }, limit);
    }
}
