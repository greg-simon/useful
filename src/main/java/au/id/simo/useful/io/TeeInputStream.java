package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * All data read from the InputStream is written to the OutputStream.
 */
public class TeeInputStream extends FilterInputStream {

    private final OutputStream out;

    public TeeInputStream(InputStream in, OutputStream out) {
        super(in);
        this.out = out;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read == -1) {
            return -1;
        }
        out.write(b, off, read);
        return read;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read == -1) {
            return -1;
        }
        out.write(read);
        return read;
    }

    @Override
    public void close() throws IOException {
        out.close();
        super.close();
    }
}
