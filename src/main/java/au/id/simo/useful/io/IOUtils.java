package au.id.simo.useful.io;

import au.id.simo.useful.Defer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 *
 */
public class IOUtils {
    
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
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

    private IOUtils() {}

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

    public static long copyThenClose(InputStream input, OutputStream output) throws IOException {
        try (InputStream in = input; OutputStream out = output) {
            return copy(in, out);
        }
    }

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

    public static String getStringAsUTF8(InputStream inputStream) throws IOException {
        return new String(getBytes(inputStream), StandardCharsets.UTF_8);
    }

    public static String getStringAsUTF8(Resource resource) throws IOException {
        return new String(getBytes(resource), StandardCharsets.UTF_8);
    }

    public static String getString(Resource resource, Charset charset) throws IOException {
        return new String(getBytes(resource), charset);
    }

    public static String getString(InputStream inputStream, Charset charset) throws IOException {
        return new String(getBytes(inputStream), charset);
    }
    public static byte[] getBytes(Resource resource) throws IOException {
        return getBytes(resource.inputStream());
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(in, bout);
            return bout.toByteArray();
        }
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
