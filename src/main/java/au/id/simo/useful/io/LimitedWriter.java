package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes characters to the provided Writer until the character limit is
 * reached, then ignores any thing else written.
 * 
 */
public class LimitedWriter extends CountingWriter {
    private final long charLimit;
    
    public LimitedWriter(Writer out, long charLimit) {
        super(out);
        this.charLimit = charLimit;
    }

    @Override
    public void write(int c) throws IOException {
        if (getCharCount() < charLimit) {
            super.write(c);
        }
        // ignore if past the charLimit
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        long remaining = charLimit - getCharCount();
        if (remaining > len) {
            super.write(cbuf, off, len);
            return;
        }
        if (getCharCount() < charLimit) {
            int charsToWrite = (int) (charLimit - getCharCount());
            if (charsToWrite <= len && charsToWrite > 0) {
                super.write(cbuf, off, charsToWrite);
            }
        }
        // nothing more can be written, so ignore the rest.
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        long remaining = charLimit - getCharCount();
        if (remaining > len) {
            super.write(str, off, len);
            return;
        }
        if (getCharCount() < charLimit) {
            int charsToWrite = (int) (charLimit - getCharCount());
            if (charsToWrite <= len && charsToWrite > 0) {
                super.write(str, off, charsToWrite);
            }
        }
        // nothing more can be written, so ignore the rest.
    }   
}
