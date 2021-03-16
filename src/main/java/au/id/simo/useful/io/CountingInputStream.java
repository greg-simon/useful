package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Keeps track of the number of bytes that pass through it.
 */
public class CountingInputStream extends FilterInputStream {
    
    /**
     * Insures the wrapped InputStream is accessible as a CountingInputStream.
     * @param in The stream to wrap in a new CountingInputStream.
     * @return If provided stream is already a CountingInputStream, then the
     * same instance will be returned. Otherwise a new CountingInputStream will
     * be created.
     */
    public static CountingInputStream wrap(InputStream in) {
        if (in instanceof CountingInputStream) {
            return (CountingInputStream) in;
        }
        return new CountingInputStream(in);
    }

    private long byteCount;

    public CountingInputStream(InputStream in) {
        super(in);
        byteCount = 0;
    }

    public long getByteCount() {
        return byteCount;
    }

    public void resetByteCount() {
        byteCount = 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = in.read(b, off, len);
        if (bytesRead > 0) {
            byteCount += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        int byt = in.read();
        if (byt != -1) {
            byteCount++;
        }
        return byt;
    }
}
