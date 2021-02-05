package au.id.simo.useful.test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Factory for obtaining test data generators.
 */
public class DataGenFactory {
    /**
     * 
     * @return an InputStream that returns starts at zero and counts up for each byte
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
}
