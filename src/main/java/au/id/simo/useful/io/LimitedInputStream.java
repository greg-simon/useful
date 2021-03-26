package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads in data from the underlying InputStream until the given limit is
 * reached.
 */
public class LimitedInputStream extends CountingInputStream {

    private final long byteLimit;

    public LimitedInputStream(InputStream in, long byteLimit) {
        super(in);
        this.byteLimit = byteLimit;
    }

    @Override
    public int read() throws IOException {
        if (getByteCount() >= byteLimit) {
            return -1;
        } else {
            return super.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long byteCount = getByteCount();
        if (byteCount >= byteLimit) {
            return -1;
        }
        
        long remaining = byteLimit - byteCount;
        int newLen = (int) Math.min((long)len, remaining);
        return super.read(b, off, newLen);
    }

    @Override
    public long skip(long n) throws IOException {
        long byteCount = getByteCount();
        if (byteCount >= byteLimit) {
            return 0;
        }
        long remaining = byteLimit - byteCount;
        long skipAmount = Math.min(remaining, n);
        return super.skip(skipAmount);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
