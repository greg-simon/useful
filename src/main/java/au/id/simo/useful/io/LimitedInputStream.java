package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads in data from the underlying InputStream until the given limit is
 * reached.
 */
public class LimitedInputStream extends InputStream {

    private final long byteLimit;
    private final CountingInputStream inputStream;

    public LimitedInputStream(InputStream in, long byteLimit) {
        this.inputStream = CountingInputStream.wrap(in);
        this.byteLimit = byteLimit;
    }

    @Override
    public int read() throws IOException {
        if (inputStream.getByteCount() >= byteLimit) {
            return -1;
        } else {
            return inputStream.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long byteCount = inputStream.getByteCount();
        if (byteCount >= byteLimit) {
            return -1;
        }
        
        long maxPostReadByteCount = byteCount + len;
        if (maxPostReadByteCount > byteLimit) {
            int reduceLenBy = (int) (maxPostReadByteCount - byteLimit);
            int newLen = len - reduceLenBy;
            return inputStream.read(b, off, newLen);
        }
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
