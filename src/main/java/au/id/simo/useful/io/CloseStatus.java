package au.id.simo.useful.io;

import java.io.IOException;

/**
 * Used to track an open/closed state and to reduce boiler plate code in
 * tracking it.
 */
public class CloseStatus implements AutoCloseable {

    private volatile boolean closed;
    private final IOException throwOnClosedCheck;

    public CloseStatus() {
        this.throwOnClosedCheck = new IOException();
    }
    
    public CloseStatus(String errorMessage) {
        this.throwOnClosedCheck = new IOException(errorMessage);
    }

    public <T extends Throwable> void throwIfClosed(T throwable) throws T {
        if (closed) {
            throw throwable;
        }
    }

    public void throwIfClosed() throws IOException {
        if (closed) {
            throw throwOnClosedCheck;
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
