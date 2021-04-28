package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A Writer implementation that allows marking ({@link #mark()) and undoing
 * ({@link #rewind()}) those written characters.
 * <p>
 * A buffer is not allocated and used unless {@link #mark()} is called.
 *
 */
public class RewindWriter extends Writer {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final Writer out;
    private final char[] charBuf;
    private int nextChar;

    private boolean marked;

    public RewindWriter(Writer out) {
        this(out, DEFAULT_BUFFER_SIZE);
    }

    public RewindWriter(Writer out, int bufferSize) {
        this.out = out;
        this.charBuf = new char[bufferSize];
        this.nextChar = 0;
    }

    /**
     * Marks the point that rewind() will rewind to. If marked has already been
     * called then the current contents of the buffer will be written out and
     * reset ready for new data.
     *
     * @throws java.io.IOException if there is an issue in writing to the
     * underlying writer when flushing anything previously marked.
     */
    public void mark() throws IOException {
        if (marked) {
            // already marked, write out existing buffer and set it again
            flushBuffer();
        }
        marked = true;
    }

    public boolean isMarked() {
        return marked;
    }

    /**
     * Drops anything previously written back until the last mark method call.
     * Unless the marked has been cleared.
     *
     */
    public void rewind() {
        nextChar = 0;
        marked = false;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (marked) {
            if ((len + nextChar) > charBuf.length) {
                // buffer is blown, write it out and clear the mark
                flushBuffer();
                out.write(cbuf, off, len);
            } else {
                System.arraycopy(cbuf, off, charBuf, nextChar, len);
                nextChar += len;
            }
        } else {
            out.write(cbuf, off, len);
        }
    }

    /**
     * Clears the mark.
     *
     * @throws IOException if there is an issue in writing to the underlying
     * writer when flushing anything previously marked.
     */
    private void flushBuffer() throws IOException {
        if (nextChar == 0) {
            return;
        }
        out.write(charBuf, 0, nextChar);
        nextChar = 0;
        marked = false;
    }

    /**
     * Clears the mark
     *
     * @throws IOException if there is an issue in writing to the underlying
     * writer when flushing anything buffered, and calling flush() on the
     * underlying stream.
     */
    @Override
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
}
