package au.id.simo.useful.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that counts all bytes written to it.
 */
public class CountingOutputStream extends FilterOutputStream {
    
    /**
     * Insures the wrapped OutputStream is accessible as a CountingOutputStream.
     * @param out The stream to wrap in a new CountingOutputStream.
     * @return If provided stream is already a CountingOutputStream, then the
     * same instance will be returned. Otherwise a new CountingOutputStream will
     * be created.
     */
    public static CountingOutputStream wrap(OutputStream out) {
        if (out instanceof CountingOutputStream) {
            return (CountingOutputStream) out;
        }
        return new CountingOutputStream(out);
    }
    
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
    public void write(int b) throws IOException {
        super.out.write(b);
        byteCount++;
    }
}
