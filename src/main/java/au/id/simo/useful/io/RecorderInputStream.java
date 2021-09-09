package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Records all data read from provided InputStream up to a byte array buffer up
 * to a set limit.
 */
public class RecorderInputStream extends FilterInputStream {

    /**
     * The maximum size of array to allocate (not allocated unless necessary).
     * Some VMs reserve some header words in an array. Attempts to allocate
     * larger arrays may result in OutOfMemoryError: Requested array size
     * exceeds VM limit
     */
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final ArrayByteBundle buffer;
    
    private boolean exceededBuffer;
    /**
     * Has method endStream been run.
     */
    private boolean streamEnded;
    private Consumer<byte[]> endStreamConsumer;

    public RecorderInputStream(InputStream in) {
        this(in, MAX_ARRAY_SIZE);
    }

    public RecorderInputStream(InputStream in, int maxBufferSize) {
        super(in);
        int initialCapacity = Math.min(10, maxBufferSize);
        this.buffer = new ArrayByteBundle(initialCapacity, maxBufferSize);
        this.exceededBuffer = false;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readCount = super.read(b, off, len);
        if (readCount == -1) {
            endStreamIfRequired();
            return -1;
        }
        int writeToBuf = Math.min(buffer.remainingMaxCapacity(), readCount);
        if (writeToBuf > 0) {
            buffer.append(b, off, writeToBuf);
        }
        if (writeToBuf < readCount) {
            exceededBuffer = true;
        }
        return readCount;
    }

    @Override
    public int read() throws IOException {
        int byteRead = super.read();
        if (byteRead == -1) {
            endStreamIfRequired();
            return -1;
        }
        if (buffer.remainingMaxCapacity() > 0) {
            buffer.append(new byte[]{(byte)byteRead});
        } else {
            // limit reached.
            exceededBuffer = true;
        }
        return byteRead;
    }

    private void endStreamIfRequired() {
        if (!streamEnded) {
            streamEnded = true;
            if (endStreamConsumer != null) {
                endStreamConsumer.accept(buffer.getBytes());
            }
        }
    }

    /**
     * Adds a consumer to be run when the end of stream has been reached.
     * <p>
     * Not run if closed before end of stream is reached.
     *
     * @param endStreamConsumer The Consumer to be run when the end of stream
     * has been reached.
     * @return a reference to this RecorderInputStream instance. To aid in
     * running this method inline during construction.
     */
    public RecorderInputStream onEndStream(Consumer<byte[]> endStreamConsumer) {
        this.endStreamConsumer = endStreamConsumer;
        return this;
    }

    public byte[] getReadByteArray() {
        return buffer.getBytes();
    }

    protected int getResizeCount() {
        return buffer.getResizeCount();
    }

    protected int getRecordedByteCount() {
        return buffer.size();
    }

    public boolean isExceededBuffer() {
        return exceededBuffer;
    }
}
