package au.id.simo.useful.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class LimitedReader extends FilterReader {
    private final long charLimit;
    
    public LimitedReader(Reader in, long limit) {
        super(CountingReader.wrap(in));
        this.charLimit = limit;
    }
    
    private CountingReader getCountingReader() {
        return (CountingReader) super.in;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        CountingReader countingReader = getCountingReader();
        long charCount = countingReader.getCharCount();
        if (charCount >= charLimit) {
            return -1;
        }
        if (charCount + len > charLimit) {
            int newLen = (int) ((charCount + len) - charLimit);
            return countingReader.read(cbuf, off, newLen);
        }
        return countingReader.read(cbuf, off, len);
    }

    @Override
    public int read() throws IOException {
        CountingReader countingReader = getCountingReader();
        if (countingReader.getCharCount() >= charLimit) {
            return -1;
        } else {
            return countingReader.read();
        }
    }
}
