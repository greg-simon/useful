package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import au.id.simo.useful.ByteRingBuffer;

/**
 * A pass through InputStream that can run MatchListeners when a specified
 * series of bytes is detected.
 *
 * Detected bytes are discarded and not provided to the consumer of this stream.
 */
public class DetectionInputStream extends FilterInputStream {

    private static final int MIN_BUFFER_SIZE = 10;
    /**
     * Match constant that will have no modifying behavior, and not match
     * anything.
     */
    private static final Match NO_OP = new Match(new byte[0], (byte[] bytes) -> {return false;});
    private final Match match;
    
    private final ByteRingBuffer buffer;
    private final CloseStatus inStream;

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
        this.inStream = new CloseStatus();
    }

    private void fillBuffer() throws IOException {
        if(inStream.isClosed()) {
            // no more in the underlying input stream, no need to read any more.
            return;
        }
        int byt = -2; // -2 is an arbitary out-of-band init marker.
        while (buffer.isNotFull() && (byt = in.read()) != -1) {
            byte b = (byte) byt;
            buffer.add(b);
        }
        if (byt == -1) {
            inStream.close();
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
        while(inStream.isOpen() && buffer.isNotFull()) {
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
