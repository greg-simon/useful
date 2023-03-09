package au.id.simo.useful.io;

import java.io.IOException;

/**
 * Used to track an open/closed state and to reduce boilerplate code around
 * throwing exceptions if closed.
 * <p>
 * Useful in IO class implementations that should throw exceptions when data is
 * read/written after being closed.
 * <p>
 * Example:
 * <pre>
 * public int read() throws IOException {
 *    latch.throwIfClosed();
 *    ...
 * }
 * </pre>
 */
public class Latch implements AutoCloseable {

    private volatile boolean closed;
    private final String closedErrorMessage;

    public Latch() {
        closedErrorMessage = null;
    }
    
    public Latch(String closedErrorMessage) {
        this.closedErrorMessage = closedErrorMessage;
    }

    public <T extends Throwable> void throwIfClosed(T throwable) throws T {
        if (closed) {
            throw throwable;
        }
    }

    public void throwIfClosed() throws IOException {
        if (closed) {
            throw new IOException(closedErrorMessage);
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    public void open() {
        closed = false;
    }

    public boolean isOpen() {
        return !closed;
    }

    public boolean isClosed() {
        return closed;
    }
}
