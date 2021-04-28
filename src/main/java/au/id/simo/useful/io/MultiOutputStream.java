package au.id.simo.useful.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Write bytes to an arbitrary number of {@link OutputStream}s.
 * <p>
 * Any exception thrown by an underlying {@link OutputStream} will likely result
 * in remaining {@link OutputStream}s not being written-to/flushed/closed.
 */
public class MultiOutputStream extends OutputStream {

    private final OutputStream[] outputStreams;

    public MultiOutputStream(OutputStream... streams) {
        this.outputStreams = streams;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream out : outputStreams) {
            out.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream out : outputStreams) {
            out.write(b);
        }
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream out : outputStreams) {
            out.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream out : outputStreams) {
            out.close();
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream out : outputStreams) {
            out.flush();
        }
    }
}
