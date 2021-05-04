package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;

import au.id.simo.useful.ByteRingBuffer;

/**
 * A pass through InputStream that can run MatchListeners when a specified
 * series of bytes is detected.
 *
 * Detected bytes are discarded and not provided to the consumer of this stream.
 */
public class DetectionInputStream extends InputStream {

    private final InputStream in;
    private final MatchListener[] listeners;
    private final ByteRingBuffer buffer;
    private final byte[] detectBytes;
    private boolean isClosed = false;
    private boolean matched = false;

    public DetectionInputStream(InputStream in, byte[] detectBytes, MatchListener... listeners) {
        this.in = in;
        this.detectBytes = detectBytes;
        this.listeners = listeners;
        this.buffer = new ByteRingBuffer(detectBytes.length);
    }

    private void fillBuffer() throws IOException {
        int byt = -2; // -2 is an arbitary out-of-band init marker.
        while (!isClosed && !buffer.isFull() && (byt = in.read()) != -1) {
            buffer.add(byt);
        }
        if (byt == -1) {
            isClosed = true;
        }
    }

    @Override
    public int read() throws IOException {
        if (matched && buffer.isEmpty()) {
            return in.read();
        }

        // try to fill buffer
        if (!buffer.isFull()) {
            fillBuffer();
        }

        // check for prompt if not matched
        if (!matched && buffer.isFull() && buffer.contains(detectBytes)) {
            matched = true;
            for (MatchListener listener : listeners) {
                listener.match(detectBytes);
            }
            // buffer contents is the prompt
            // clear the buffer and refill.
            buffer.clear();
            fillBuffer();
        }

        if (buffer.isEmpty()) {
            return -1;
        } else {
            return buffer.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (matched) {
            return in.read(b, off, len);
        } else {
            return super.read(b, off, len);
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (matched) {
            return in.read(b);
        } else {
            return super.read(b);
        }
    }

    @FunctionalInterface
    public interface MatchListener {

        void match(byte[] detected) throws IOException;
    }
}
