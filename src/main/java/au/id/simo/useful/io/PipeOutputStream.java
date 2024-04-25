package au.id.simo.useful.io;

import au.id.simo.useful.collections.ByteRingBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ring buffer using locks to manage concurrent access.
 * <p>
 * An alternative to {@link java.io.PipedOutputStream} and
 * {@link java.io.PipedInputStream} that does not attach to specific
 * producer and consumer threads.
 * <p>
 * Usage Example:
 * <pre>
 *    // For use in the producer thread
 *    PipeOutputStream pout = new PipeOutputStream(1024);
 *    // For use in the consumer thread.
 *    InputStream pin = pout.getInputStream();
 * </pre><p>
 * The OutputStream will block when the internal buffer is full, waiting
 * for space to be freed by the pared InputStream reading data.
 * <p>
 * The InputStream will block when the internal buffer is empty, waiting
 * for data to be written from the paired OutputStream.
 * <p>
 * The OutputStream needs to be closed for the paired InputStream to reach
 * the End Of Stream.
 * <p>
 * While it is possible to use both streams in the same thread, it will
 * deadlock when trying to read and empty buffer, or write to a full
 * buffer.
 */
public class PipeOutputStream extends OutputStream {

    private final ByteRingBuffer buffer;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;
    private final Latch writeLatch;
    private final InputStream inputStream;

    /**
     * Create a new PipeOutputStream with the specified buffer size.
     * @param bufferSize size of the buffer in bytes.
     */
    public PipeOutputStream(int bufferSize) {
        buffer = new ByteRingBuffer(bufferSize);
        lock = new ReentrantLock(true);
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
        writeLatch = new Latch("Stream closed.");
        inputStream = new PipeInputStream();
    }

    /**
     * This will always return the same instance of InputStream as only
     * one is ever created.
     *
     * @return An InputStream that reads from the buffer shared with this class.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int remainingLen = len;
        // if the provided len is larger than the buffer capacity, chop it up and write a bit at a time.
        while(remainingLen > 0) {
            int writeAmount = Math.min(buffer.capacity(), remainingLen);
            int adjustedOffset = len - remainingLen;
            writeSegment(b, adjustedOffset, writeAmount);
            remainingLen -= writeAmount;
        }
    }

    private void writeSegment(byte[] b, int off, int len) throws IOException {
        writeLatch.throwIfClosed();
        lock.lock();
        try {
            while (buffer.getFreeSpace() < len) {
                // block to await enough space to write len
                notFull.await();
            }
            buffer.write(b, off, len);
            notEmpty.signal();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        writeLatch.close();
        lock.lock();
        try {
            // signal InputStream to stop waiting for more bytes
            // and notice this OutputStream has been closed.
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    private class PipeInputStream extends InputStream {

        private final Latch readLatch = new Latch("Stream closed.");

        @Override
        public int read() throws IOException {
            byte[] singleByteBuf = new byte[1];
            int readCount = read(singleByteBuf, 0, 1);
            if (readCount == -1) {
                return -1;
            }
            return singleByteBuf[0] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            readLatch.throwIfClosed();
            lock.lock();
            try {
                while (buffer.isEmpty()) {
                    // check closed status before waiting
                    if (buffer.isEmpty() && writeLatch.isClosed()) {
                        // End of stream reached
                        return -1;
                    }
                    // block to await bytes to be written, or for the
                    // paired PipeOutputStream to close.
                    notEmpty.await();
                }
                int readLength = Math.min(len, buffer.size());
                buffer.read(b, off, readLength);
                notFull.signal();
                return readLength;
            } catch (InterruptedException e) {
                throw new IOException(e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int available() throws IOException {
            readLatch.throwIfClosed();
            return buffer.size();
        }

        @Override
        public void close() {
            readLatch.close();
            // close writer as well, no point having the writer waste resources.
            writeLatch.close();
        }
    }
}
