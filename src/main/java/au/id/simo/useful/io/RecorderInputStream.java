package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Records all data read from provided InputStream up to a byte array buffer up
 * to a set limit.
 * 
 */
public class RecorderInputStream extends FilterInputStream {
    /**
     * The maximum size of array to allocate (unless necessary).
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    private final int maxBufferSize;
    
    private byte[] buffer;
    private int index;
    private int resizeCount;
    private boolean exceededBuffer;
    /** Has method endStream been run. **/
    private boolean streamEnded;
    
    public RecorderInputStream(InputStream in) {
        this(in, MAX_ARRAY_SIZE);
    }
    
    public RecorderInputStream(InputStream in, int maxBufferSize) {
        super(in);
        this.maxBufferSize = maxBufferSize;
        this.index = 0;
        this.resizeCount = 0;
        int initialCapacity = Math.min(10, maxBufferSize);
        this.buffer = new byte[initialCapacity];
        this.exceededBuffer = false;
    }
    
    /**
     * 
     * @param newBytesCount
     * @return capacity available in the buffer.
     */
    private int ensureBufferCapacity(int newBytesCount) {
        int existingCapacity = buffer.length - index;
        if (existingCapacity >= newBytesCount) {
            // there is already enough space for the new bytes, do nothing.
            return existingCapacity;
        }
        
        // Adding any int could result in overflow if result in greater than
        // Integer.MAX_VALUE. So all math must use subtraction and remainders.
        int remainingIntLimit = MAX_ARRAY_SIZE - buffer.length;
        int remainingSizeLimit = maxBufferSize - buffer.length;
        int remainingLimit = Math.min(remainingSizeLimit, remainingIntLimit);
        
        int minGrowthRequired = newBytesCount - existingCapacity;
        int growth;
        if (remainingLimit <= minGrowthRequired) {
            growth = remainingLimit;
        } else {
            int nextIncrement = index >> 1;
            int growthFactor = Math.max(minGrowthRequired, nextIncrement);
            int limitedGrowthFactor = Math.min(growthFactor, remainingLimit);
            growth = limitedGrowthFactor;
        }
        
        if (growth == 0) {
            // no growth will happen, so don't bother allocating new buffer and
            // copying over the data.
            return existingCapacity;
        }
        // this addition is safe as the sum of both has been verified against
        // MAX_ARRAY_SIZE above using 'remainingLimit'.
        int newSize = buffer.length + growth;
        int newAvailableCapacity = newSize - index;
        byte[] newBuffer = new byte[newSize];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        buffer = newBuffer;
        resizeCount++;
        return newAvailableCapacity;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read == -1) {
            endStreamIfRequired();
            return -1;
        }
        int capacity = ensureBufferCapacity(read);
        if (capacity < read) {
            exceededBuffer = true;
        }
        int writeToBuf = Math.min(capacity, read);
        if (writeToBuf > 0) {
            System.arraycopy(b, off, buffer, index, writeToBuf);
            index+=writeToBuf;
        }
        return read;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read == -1) {
            endStreamIfRequired();
            return -1;
        }
        int capacity = ensureBufferCapacity(1);
        if (capacity > 0) {
            buffer[index] = (byte) read;
            index++;
        } else {
            exceededBuffer = true;
        }
        return read;
    }
    
    private void endStreamIfRequired() {
        if (!streamEnded) {
            streamEnded = true;
            endStream();
        }
    }
    
    /**
     * A hook added that can be overridden to run code when the end of the
     * underlying stream has been reached.
     * <p>
     * Not run if closed before end of stream is reached.
     */
    public void endStream() {
        
    }
    
    public byte[] getReadByteArray() {
        return Arrays.copyOf(buffer, index);
    }
    
    protected int getResizeCount() {
        return resizeCount;
    }
    
    protected int getRecordedByteCount() {
        return index;
    }

    public boolean isExceededBuffer() {
        return exceededBuffer;
    }
}
