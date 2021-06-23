package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Reads the provided InputStreams one after the other in the order they are
 * provided. As if all provided InputStreams are concatenated.
 */
public class ConcatInputStream extends InputStream {

    private final Iterable<InputStream> streams;
    private final Iterator<InputStream> streamsItr;
    private InputStream currentStream;

    public ConcatInputStream(InputStream... streams) {
        this(Arrays.asList(streams));
    }
    
    public ConcatInputStream(Iterable<InputStream> streams) {
        this.streams = streams;
        this.streamsItr = streams.iterator();
    }

    /**
     * Obtains the current InputStream, or the next InputStream if the current
     * InputStream is not set.
     * <p>
     * {@code null} is returned to flag there is no further InputStreams
     * available.
     *
     * @return The current InputStream if set, The next InputStream if current
     * is not set and there is at least one more, or null if the current
     * InputStream is not set and no further InputStreams are available.
     */
    private InputStream currentStream() {
        if (currentStream == null && streamsItr.hasNext()) {
            currentStream = streamsItr.next();
        }
        return currentStream;
    }

    /**
     * Forces the current InputStream to be unset and the next InputStream to be
     * set to current and returned.
     *
     * @return The next InputStream, or {@code null} if none remain.
     */
    private InputStream nextStream() {
        currentStream = null;
        return currentStream();
    }

    @Override
    public int read() throws IOException {
        InputStream current = currentStream();
        while (current != null) {
            int returnValue = current.read();
            if (returnValue == -1) {
                current = nextStream();
            } else {
                return returnValue;
            }
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        InputStream current = currentStream();
        while (current != null) {
            int readCount = current.read(b, off, len);
            if (readCount == -1) {
                current = nextStream();
            } else {
                return readCount;
            }
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        for (InputStream in : streams) {
            in.close();
        }
    }
}
