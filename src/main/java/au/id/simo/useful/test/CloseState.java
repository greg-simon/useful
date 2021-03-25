package au.id.simo.useful.test;

import java.io.IOException;

/**
 * Tracks close state for streams and reader/writers.
 */
public class CloseState {
    public static final String STREAM_MSG = "Stream Closed";
    public static final String READER_MSG = "Reader Closed";
    public static final String WRITER_MSG = "Writer Closed";
    
    public static CloseState reader() {
        return new CloseState(READER_MSG);
    }
    
    public static CloseState writer() {
        return new CloseState(WRITER_MSG);
    }
    
    public static CloseState stream() {
        return new CloseState(STREAM_MSG);
    }
    
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
