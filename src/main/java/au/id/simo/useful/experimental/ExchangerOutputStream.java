package au.id.simo.useful.experimental;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;

/**
 *
 */
public class ExchangerOutputStream extends OutputStream {
    private final Exchanger<ByteBuffer> exchanger;
    
    private ByteBuffer buffer;
        
    public ExchangerOutputStream(ExchangerInputStream ein) {
        this.exchanger = ein.exchanger;
        buffer = ByteBuffer.allocate(ein.bufferSize);
    }
    
    @Override
    public void write(int b) throws IOException {
        if(buffer.position()>=buffer.limit()) {
            exchangeBuffers();
        }
        buffer.put((byte)b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if(b.length - off < len) {
            throw new ArrayIndexOutOfBoundsException(
                    "Specified length is too large: "+len);
        }
        int written=0;
        while ( written < len) {
            int write = Math.min(buffer.remaining(), len - written);
            if(write == 0) {
                exchangeBuffers();
                continue;
            }
            buffer.put(b, off + written, write);
            written += write;
        }
    }
    
    public void exchangeBuffers() throws IOException {
        try {
            buffer = exchanger.exchange(buffer);
            if (buffer == null) {
                throw new IOException("Pipe broken");
            }
            buffer.clear();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void flush() throws IOException {
        exchangeBuffers();
    }
    
    @Override
    public void close() throws IOException {
        try {
            if(buffer.position() > 0) {
                exchangeBuffers();
            }
            // flag that stream is closed by exchanging a null buffer.
            exchanger.exchange(null);
            buffer = null; // make buffer available for gc
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }
}
