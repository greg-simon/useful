package au.id.simo.useful.test;

import java.io.IOException;

/**
 * Tracks close state for streams and reader/writers.
 */
public class CloseState {
    private final String errorMsg;
    private volatile boolean closed;

    public CloseState(String errorMsg) {
        this.errorMsg = errorMsg;
        this.closed = false;
    }
    /**
     * Throws IOException if in a closed state.
     * @throws IOException if close() has been called.
     */
    public void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException(errorMsg);
        }
    }
    
    public void close() {
        this.closed = true;
    }
}
