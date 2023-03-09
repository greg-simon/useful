package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes characters to the provided {@link Writer} until the character limit is
 * reached, then ignores anything else written.
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
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        long remaining = charLimit - getCharCount();
        if (remaining > 0) {
            int newLen = (int) Math.min(remaining, len);
            super.write(cbuf, off, newLen);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        long remaining = charLimit - getCharCount();
        if (remaining > 0) {
            int newLen = (int) Math.min(remaining, len);
            super.write(str, off, newLen);
        }
    }
}
