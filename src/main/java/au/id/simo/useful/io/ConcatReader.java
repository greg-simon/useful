package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Reads the provided Readers one after the other in the order they are
 * provided. As if all provided Readers are concatenated.
 */
public class ConcatReader extends Reader {

    private final Iterable<Reader> readers;
    private final Iterator<Reader> readerItr;
    private Reader currentReader;

    public ConcatReader(Reader... readers) {
        this(Arrays.asList(readers));
    }

    public ConcatReader(Iterable<Reader> readers) {
        this.readers = readers;
        this.readerItr = readers.iterator();
    }

    /**
     * Obtains the current Reader, or the next Reader if the current Reader is
     * not set.
     * <p>
     * {@code null} is returned to flag there is no further readers available.
     *
     * @return The current Reader if set, The next Reader if current is not set
     * and there is at least one more, or null if the current Reader is not set
     * and no further Readers are available.
     */
    private Reader currentReader() {
        if (currentReader == null && readerItr.hasNext()) {
                currentReader = readerItr.next();
        }
        return currentReader;
    }

    /**
     * Forces the current Reader to be unset and the next Reader to be set to
     * current and returned.
     *
     * @return The next Reader, or {@code null} if none remain.
     */
    private Reader nextReader() {
        currentReader = null;
        return currentReader();
    }

    @Override
    public int read() throws IOException {
        Reader current = currentReader();
        while (current != null) {
            int returnValue = current.read();
            if (returnValue == -1) {
                current = nextReader();
            } else {
                return returnValue;
            }
        }
        return -1;
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
        Reader current = currentReader();
        while (current != null) {
            int readCount = current.read(b, off, len);
            if (readCount == -1) {
                current = nextReader();
            } else {
                return readCount;
            }
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        for (Reader in : readers) {
            in.close();
        }
    }
}
