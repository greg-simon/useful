package au.id.simo.useful.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that counts all bytes written to it.
 */
public class CountingOutputStream extends FilterOutputStream {
    private long byteCount;
    
    public CountingOutputStream(OutputStream out) {
        super(out);
        byteCount = 0;
    }

    public long getByteCount() {
        return byteCount;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        byteCount+=len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.out.write(b);
        byteCount+=b.length;
    }

    @Override
    public void write(int b) throws IOException {
        super.out.write(b);
        byteCount++;
    }
}
