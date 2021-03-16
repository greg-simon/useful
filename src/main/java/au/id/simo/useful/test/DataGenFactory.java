package au.id.simo.useful.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import au.id.simo.useful.io.LimitedInputStream;

/**
 * Factory for obtaining test data generators.
 */
public class DataGenFactory {
    /**
     * 
     * @return an InputStream that returns starts at zero and counts up for each
     * byte.
     */
    public static InputStream incrementingBytes() {
        return new InputStream() {
            private int counter=0;
            @Override
            public int read() throws IOException {
                return counter++ & 0xff;
            }
        };
    }
    
    /**
     * 
     * @param limit The number of bytes to return before ending the stream.
     * @return an InputStream that returns starts at zero and counts up for each
     * byte.
     */
    public static InputStream incrementingBytesWithLimit(long limit) {
        return new LimitedInputStream(incrementingBytes(),limit);
    }
    
    public static InputStream randomBytes() {
        Random random = new Random();
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return random.nextInt(256);
            }
        };
    }
    
    public static InputStream randomBytesWithLimit(long limit) {
        return new LimitedInputStream(randomBytes(), limit);
    }
}
