package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Keeps track of the number of bytes that pass through it.
 */
public class CountingInputStream extends FilterInputStream {

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
        int bytesRead = super.read(b, off, len);
        if (bytesRead > 0) {
            byteCount += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        int byt = super.read();
        if (byt != -1) {
            byteCount++;
        }
        return byt;
    }
}
