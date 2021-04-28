package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Joins multiple Readers into one.
 */
public class ConcatReader extends Reader {

    private final List<Reader> readers;
    private final Iterator<Reader> readerItr;
    private Reader currentReader;

    public ConcatReader(Reader... readers) {
        this.readers = Arrays.asList(readers);
        this.readerItr = this.readers.iterator();
    }

    private Reader currentStream() {
        if (currentReader == null) {
            if (readerItr.hasNext()) {
                currentReader = readerItr.next();
            }
        }
        return currentReader;
    }

    private void currentEnded() {
        currentReader = null;
    }

    @Override
    public int read() throws IOException {
        Reader current = currentStream();
        while (current != null) {
            int returnValue = current.read();
            if (returnValue == -1) {
                currentEnded();
                current = currentStream();
            } else {
                return returnValue;
            }
        }
        return -1;
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
        Reader current = currentStream();
        while (current != null) {
            int readCount = current.read(b, off, len);
            if (readCount == -1) {
                currentEnded();
                current = currentStream();
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
