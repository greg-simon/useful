package au.id.simo.useful.io;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Keeps track of the number of characters written.
 */
public class CountingWriter extends FilterWriter {
    private long charCount;
    
    public CountingWriter(Writer out) {
        super(out);
    }
    
    public long getCharCount() {
        return charCount;
    }
    
    public void resetCharCount() {
        charCount = 0;
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
        charCount+=len;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        charCount+=len;
    }

    @Override
    public void write(int c) throws IOException {
        super.write(c);
        charCount++;
    }    
}
