package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads in characters from the underlying {@link Reader} until the given limit
 * is reached.
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
        long remaining = charLimit - charCount;
        if (remaining <= 0) {
            return -1;
        }

        int newLen = (int) Math.min(remaining, len);
        return super.read(cbuf, off, newLen);
    }

    @Override
    public int read() throws IOException {
        if (getCharCount() >= charLimit) {
            return -1;
        } else {
            return super.read();
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long remaining = charLimit - getCharCount();
        long skipAmount = Math.min(remaining, n);
        return super.skip(skipAmount);
    }
}
