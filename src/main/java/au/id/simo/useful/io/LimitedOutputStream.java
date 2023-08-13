package au.id.simo.useful.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes to the output stream until the given limit has been reached, then
 * anything else written is silently ignored.
 */
public class LimitedOutputStream extends CountingOutputStream {

    private final long byteLimit;

    public LimitedOutputStream(OutputStream out, long byteLimit) {
        super(out);
        this.byteLimit = byteLimit;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        long remaining = byteLimit - getByteCount();
        int newLen = (int) Math.min(len, remaining);
        super.write(b, off, newLen);
    }

    @Override
    public void write(int b) throws IOException {
        if (getByteCount() < byteLimit) {
            super.write(b);
        }
    }
    
    /**
     * Has the limit been reached.
     * @return true if the byte limit has been reached, otherwise false.
     */
    public boolean isLimitMet() {
        return getByteCount() >= byteLimit;
    }
}
