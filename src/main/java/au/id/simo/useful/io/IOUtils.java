package au.id.simo.useful.io;

import au.id.simo.useful.Defer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

/**
 *
 */
public class IOUtils {
    
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    
    /**
     * Discards all data written to it. Used in
     * {@link #drain(java.io.InputStream)}
     */
    public static final OutputStream NULL_OS = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            // discard
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // discards
        }
    };

    /**
     * Creates a {@link Callable} that can be used with an {@link java.util.concurrent.ExecutorService} to copy
     * bytes from an {@link InputStream} to an {@link OutputStream}.
     * <p>
     * Useful in copying bytes off the main thread of execution.
     * @param input The source of bytes to copy.
     * @param output The destination of the bytes to copy.
     * @return The callable used to actually perform the copying. This Callable will also carry the count of
     * the copied bytes.
     */
    public static Callable<Long> copyCallable(InputStream input, OutputStream output) {
        return () -> {
            try (Defer defer = new Defer().closeAll(input, output)) {
                return copy(input, output);
            }
        };
    }

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

    public static long copy(InputStream input, ByteCopyConsumer consumer) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            consumer.consume(count, buffer, n);
            count += n;
        }
        return count;
    }
    
    @FunctionalInterface
    public interface ByteCopyConsumer {
        /**
         * 
         * @param total the total number of bytes that have been copied before
         * this method call from a single InputStream.
         * @param src the byte buffer containing some bytes to copy
         * @param srcLength the number of bytes available in the src array
         */
        void consume(long total, byte[] src, int srcLength);
    }
    
    /**
     * Reads the provided InputStream and discards all data until end of stream
     * is reached.
     * 
     * @param in The InputStream to read from.
     * @return the number of bytes read from the InputStream before end of
     * stream is reached.
     * @throws java.io.IOException if there is any exception throw when reading
     * from the InputStream.
     */
    public static long drain(InputStream in) throws IOException {
        return copy(in, NULL_OS);
    }
}
