package au.id.simo.useful.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class CountingReader extends FilterReader {
    private long charCount;
    
    public CountingReader(Reader in) {
        super(in);
        charCount = 0;
    }
    
    public long getCharCount() {
        return charCount;
    }
    
    public void resetCharCount() {
        charCount = 0;
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = in.read(cbuf, off, len);
        if (charsRead > 0) {
            charCount += charsRead;
        }
        return charsRead;
    }

    @Override
    public int read() throws IOException {
        int byt = in.read();
        if (byt != -1) {
            charCount++;
        }
        return byt;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        charCount+=skipped;
        return skipped;
    }
}
