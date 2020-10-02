package au.id.simo.useful.experimental;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GC friendly replacement for PipedInputStream using {@link Exchanger}.
 */
public class ExchangerInputStream extends InputStream {

    protected final Exchanger<ByteBuffer> exchanger;
    protected final int bufferSize;
    private ByteBuffer buffer;
    
    public ExchangerInputStream() {
        this(1024);
    }
    
    public ExchangerInputStream(int bufferSize) {
        this.exchanger = new Exchanger<>();
        this.bufferSize = bufferSize;
        buffer = (ByteBuffer) ByteBuffer.allocate(bufferSize).flip();
    }
    
    private boolean isEndOfStream() {
        return buffer == null;
    }
    
    @Override
    public int read() throws IOException {
        if(isEndOfStream()) {
            return -1;
        }
        if (!buffer.hasRemaining()) {
            exchangeBuffers();
            // check again
            if(isEndOfStream()) {
                return -1;
            }
        }
        return Byte.toUnsignedInt(buffer.get());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isEndOfStream()) {
            return -1;
        }
        if(b.length - off > len) {
            throw new ArrayIndexOutOfBoundsException(
                    "Specified length is too large: "+len);
        }
        if(!buffer.hasRemaining()) {
            exchangeBuffers();
            if(isEndOfStream()) {
                return -1;
            }
        }
        len = Math.min(len, buffer.remaining());
        buffer.get(b, off, len);
        return len;
    }
    
    private void exchangeBuffers() throws IOException {
        try {
            buffer = exchanger.exchange(buffer);
            // check for end of stream
            if(isEndOfStream()) {
                return;
            }
            // set buffer to read mode.
            buffer.flip();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            // signal breaking the connection.
            exchanger.exchange(null);
            buffer = null;
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }    
}
