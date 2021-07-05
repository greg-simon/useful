package au.id.simo.useful.backport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class IOUtils {
    
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private IOUtils() {}
    
    /**
     * Copies all the contents from the given input stream to the given output
     * stream.
     * <p>
     * Made redundant in Java 9+ by the new method
     * {@code InputStream.transferTo(OutputStream out)}
     *
     * @param input the input stream
     * @param output the output stream
     * @return the number of bytes that have been copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
