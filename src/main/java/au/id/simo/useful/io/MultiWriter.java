package au.id.simo.useful.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Write characters to an arbitrary number of {@link Writer}s.
 * <p>
 * Any exception thrown by an underlying {@link Writer} will likely result
 * in remaining {@link Writer}s not being written-to/flushed/closed.
 */
public class MultiWriter extends Writer {

    private final Writer[] writers;

    public MultiWriter(Writer... writers) {
        this.writers = writers;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (Writer writer : writers) {
            writer.write(cbuf, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        for (Writer writer : writers) {
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (Writer writer : writers) {
            writer.close();
        }
    }

    @Override
    public Writer append(char c) throws IOException {
        for (Writer writer : writers) {
            writer.append(c);
        }
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        for (Writer writer : writers) {
            writer.append(csq, start, end);
        }
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        for (Writer writer : writers) {
            writer.append(csq);
        }
        return this;
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        for (Writer writer : writers) {
            writer.write(str, off, len);
        }
    }

    @Override
    public void write(String str) throws IOException {
        for (Writer writer : writers) {
            writer.write(str);
        }
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        for (Writer writer : writers) {
            writer.write(cbuf);
        }
    }

    @Override
    public void write(int c) throws IOException {
        for (Writer writer : writers) {
            writer.write(c);
        }
    }
}
