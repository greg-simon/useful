package au.id.simo.useful.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes to the output stream until the given limit has been reached, then
 * anything else written is ignored.
 */
public class LimitedOutputStream extends OutputStream {

    private final CountingOutputStream cout;
    private final long byteLimit;

    public LimitedOutputStream(OutputStream out, long byteLimit) {
        this.cout = new CountingOutputStream(out);
        this.byteLimit = byteLimit;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        long byteCount = cout.getByteCount();
        if ((byteCount + len) <= byteLimit) {
            cout.write(b, off, len);
            return;
        }
        // can any bytes be written
        if (byteCount < byteLimit) {
            // how many bytes can I write?
            int bytesToWrite = (int) (byteLimit - byteCount);
            if (bytesToWrite <= len && bytesToWrite > 0) {
                cout.write(b, off, bytesToWrite);
            }
        }
        // nothing more can be written, just discarded.
    }

    @Override
    public void write(int b) throws IOException {
        if (cout.getByteCount() < byteLimit) {
            cout.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        cout.close();
    }

    @Override
    public void flush() throws IOException {
        cout.flush();
    }
}
