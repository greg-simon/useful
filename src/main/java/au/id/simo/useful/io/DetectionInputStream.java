package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.id.simo.useful.collections.ByteRingBuffer;

/**
 * A pass through InputStream that can run MatchListeners when a specified
 * series of bytes is detected.
 * <p>
 * Detected bytes are discarded and not provided to the consumer of this stream.
 */
public class DetectionInputStream extends FilterInputStream {

    private static final int MIN_BUFFER_SIZE = 10;
    /**
     * Match constant that will have no modifying behavior, and not match
     * anything.
     */
    private static final Match NO_OP = new Match(new byte[0], bytes -> false);
    private final Match match;
    
    private final ByteRingBuffer buffer;
    private final Latch inStatus;

    public DetectionInputStream(InputStream in) {
        this(in, null);
    }
    
    public DetectionInputStream(InputStream in, Match match) {
        super(in);
        if (match == null) {
            this.match = NO_OP;
        } else {
            this.match = match;
        }
        // ensure buffer is never zero
        int maxBufferRequired = Math.max(MIN_BUFFER_SIZE, this.match.matchBytes.length);
        this.buffer = new ByteRingBuffer(maxBufferRequired);
        this.inStatus = new Latch();
    }

    private void fillBuffer() throws IOException {
        if(inStatus.isClosed()) {
            // no more in the underlying input stream, no need to read anymore.
            return;
        }
        int byt = -2; // -2 is an arbitrary out-of-band init marker.
        while (buffer.isNotFull() && (byt = in.read()) != -1) {
            byte b = (byte) byt;
            buffer.add(b);
        }
        if (byt == -1) {
            inStatus.close();
        }
    }
    
    /**
     * Only works if checked each byte read from underlying stream.
     * 
     * @return true if some bytes have been skipped in the buffer. False if none
     * have.
     * @throws IOException if there is any error in running a MatchListener.
     */
    private boolean checkMatch() throws IOException {
        int matchIndex = buffer.indexOf(match.matchBytes);
        if (matchIndex != 0) {
            return false;
        }
        if (match.onMatch.filter(match.matchBytes)) {
            buffer.skip(match.matchBytes.length);
            return true;
        }
        return false;
    }

    @Override
    public int read() throws IOException {
        while(inStatus.isOpen() && buffer.isNotFull()) {
            fillBuffer();
        }
        
        while (checkMatch()) {
            fillBuffer();
        }
        
        if(buffer.isEmpty()) {
            return -1;
        }
        
        return buffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // copied directly from InputStream implementation.
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }
        } catch (IOException ee) {
            // do nothing
        }
        return i;
    }
    
    @Override
    public long skip(long n) throws IOException {
        long skipped = 0;
        while (skipped < n) {
            int readByte = this.read();
            if (readByte == -1) {
                return skipped;
            }
            skipped++;
        }
        return skipped;
    }
    
    

    public static class Match {
        private final byte[] matchBytes;
        private final OnMatch onMatch;

        public Match(byte[] matchBytes, OnMatch matchAction) {
            this.matchBytes = matchBytes;
            this.onMatch = matchAction;
        }
    }
    
    @FunctionalInterface
    public interface OnMatch {
        /**
         * Executed when the associated bytes have been detected in the
         * underlying InputStream.
         *
         * @param detect the bytes that were matched on.
         * @return true if matched bytes are to be filtered from the InputStream
         * and not be read by the consumer code of this stream. false is to be
         * returned if the matched bytes are to be readable by the consumer code
         * of this stream.
         * @throws IOException if there is a problem in performing work after
         * matching some bytes.
         */
        boolean filter(byte[] detect) throws IOException;
    }
}
