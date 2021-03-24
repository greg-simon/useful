package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads in characters from the underlying Reader until the given limit is
 * reached.
 */
public class LimitedReader extends CountingReader {
    private final long charLimit;
    
    public LimitedReader(Reader in, long limit) {
        super(in);
        this.charLimit = limit;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        long charCount = getCharCount();
        if (charCount >= charLimit) {
            return -1;
        }
        if (charCount + len > charLimit) {
            int newLen = (int) ((charCount + len) - charLimit);
            return super.read(cbuf, off, newLen);
        }
        return super.read(cbuf, off, len);
    }

    @Override
    public int read() throws IOException {
        if (getCharCount() >= charLimit) {
            return -1;
        } else {
            return super.read();
        }
    }
}
