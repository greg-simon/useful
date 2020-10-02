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
        if (in instanceof CountingInputStream) {
            inputStream = (CountingInputStream) in;
        } else {
            inputStream = new CountingInputStream(in);
        }
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
        if (inputStream.getByteCount() >= byteLimit) {
            return -1;
        }
        if (inputStream.getByteCount() + len >= byteLimit) {
            int newLen = (int) ((inputStream.getByteCount() + len) - byteLimit);
            return inputStream.read(b, off, newLen);
        }
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
