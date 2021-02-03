package au.id.simo.useful;

import java.util.Iterator;
import java.util.Objects;

/**
 * Byte buffer for use with streams.
 * <p>
 * Allows writing to it forever without additional memory usage.
 * <p>
 * Usage Modes:
 * <ul>
 * <li>Writes overwrite old values silently: Use add and get</li>
 * <li>Writes error if no free space: Use put and read</li>
 * </ul>
 */
public class ByteRingBuffer implements Iterable<Byte> {

    private final byte[] buffer;

    /**
     * Add index. Points to next location to write to. Write then increment.
     */
    private int head;
    /**
     * Remove index. Points to oldest value to read from. Read then increment.
     */
    private int tail;
    
    /**
     * Represents the number of values stored in this buffer.
     */
    private int size;

    public ByteRingBuffer(int maxSize) {
        buffer = new byte[maxSize];
        head = 0;
        tail = 0;
        size = 0;
    }

    /**
     * Logic for wrapping indexes on array length.
     *
     * @param index head or tail.
     * @param incrementBy the number to increment the index by.
     * @return new incremented value, possibly wrapped back to 0 if required.
     * Will never be a number that causes an ArrayOutpfBoundsException on the
     * buffer.
     */
    protected int incrementIndex(int index, int incrementBy) {
        return (index + incrementBy) % buffer.length;
    }

    /**
     *
     * @param i add value, overwriting oldest value if at capacity.
     */
    public void add(int i) {
        if (i > Byte.MAX_VALUE) {
            throw new IllegalArgumentException(i + " is greater than Byte.MAX_VALUE");
        }
        if (i < Byte.MIN_VALUE) {
            throw new IllegalArgumentException(i + " is smaller than Byte.MIN_VALUE");
        }
        add((byte) i);
    }

    public void add(byte i) {
        // write
        buffer[head] = i;
        // then increment
        head = incrementIndex(head, 1);
        
        if (isFull()) {
            // if old value was overriden, update the tail.
            tail = head;
        } else {
            size++;
        }
    }
    
    /**
     * Same as add except an exception will be thrown if there is no space.
     * @param i
     */
    public void put(int i) {
        if (isFull()) {
            throw new ArrayIndexOutOfBoundsException("Buffer is full");
        }
        add(i);
    }
    
    /**
     * Same as add except an exception will be thrown if there is no space.
     * @param i
     */
    public void put(byte i) {
        if (isFull()) {
            throw new ArrayIndexOutOfBoundsException("Buffer is full");
        }
        add(i);
    }

    /**
     *
     * @return oldest value or throws ArrayIndexOutOfBounds exception if empty.
     */
    public byte peek() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is empty");
        }
        return buffer[tail];
    }

    /**
     * Returns a value relative to the oldest value in the buffer.
     * <p>
     * This method as no side effects. No indexes are updated with this call,
     * no extra space is freed in the buffer.
     * 
     * @param index where 0 means the oldest item in the collection.
     * @return the value that is {@code index} positions from the oldest item in
     * the collection.
     */
    public byte peek(int index) {
        if (index >= size()) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Index value %s is larger than the number of elements %s.",
                             index,
                             size()
                    )
            );
        }
        int relindex = incrementIndex(tail, index);
        return buffer[relindex];
    }

    /**
     * Read and removed oldest value from the buffer.
     *
     * @return oldest value or throws ArrayIndexOutOfBounds exception if empty.
     */
    public byte read() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is empty");
        }
        // read
        byte t = buffer[tail];
        // zero out
        buffer[tail] = 0;
        
        // then increment
        tail = incrementIndex(tail, 1);
        size--;
        
        return t;
    }
    
    /**
     * 
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values into
     * @param length the number of values to copy.
     * @return 
     */
    public int read(byte[] dest, int start, int length) {
        int readLength = Math.min(size(), length);
        
        if (head >= tail) {
            // straight array copy
            System.arraycopy(buffer, tail, dest, start, readLength);
            tail = incrementIndex(tail, readLength);
            size -= readLength;
            return readLength;
        }
        
        // buffer array has two segments to copy out of order. One at start of
        // the buffer and one at the end. (h is head index, t is tail index)
        //[0,0,h, , ,t,0]
        //[^s1 ^]   [^ ^]  <- seg2
        // segment two to start of newArray, as seg two will be the oldest
        // values
        int segTwoLength = buffer.length - tail;
        int segTwoReadLength = Math.min(readLength, segTwoLength);
        System.arraycopy(buffer, tail, dest, start, segTwoReadLength);

        // segment one to end of newArray
        int segOneReadLength = Math.min(head+1, readLength - segTwoReadLength);
        System.arraycopy(buffer, 0, dest, start + segTwoReadLength, segOneReadLength);
        
        int totalReadLength = segTwoReadLength + segOneReadLength;
        tail = incrementIndex(tail, totalReadLength);
        size -= totalReadLength;
        return totalReadLength;
    }

    public int size() {
        return size;
    }

    public int maxSize() {
        return buffer.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isFull() {
        return size == buffer.length;
    }
    
    public boolean isNotFull() {
        return !isFull();
    }
    
    public int getFreeSpace() {
        return maxSize() - size();
    }

    public byte[] toArray() {
        return toArray(new byte[size()]);
    }

    public byte[] toArray(byte[] a) {
        if (size == 0) {
            return a;
        }

        if (size > a.length) {
            a = new byte[size];
        }

        if (head > tail) {
            System.arraycopy(buffer, tail, a, 0, size);
        } else {
            //[0,0,h, , ,t,0]
            //[^s1 ^]   [^ ^]  <- seg2
            // segment two to start of newArray
            int segTwoLength = buffer.length - tail;
            System.arraycopy(buffer, tail, a, 0, segTwoLength);
            // segment one to end of newArray
            System.arraycopy(buffer, 0, a, segTwoLength, head);
        }
        return a;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ByteRingBuffer");
        sb.append('[');
        for (int i = 0; i < buffer.length; i++) {
            if (head == i) {
                sb.append('+');
            }
            if (tail == i) {
                sb.append('-');
            }
            sb.append(String.valueOf(buffer[i]));
            sb.append(',');
        }
        sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        return sb.toString();
    }

    public boolean containsArray(byte[] array) {
        if (array.length > size()) {
            return false;
        }
        boolean match = true;
        for (int i = 0; i < array.length; i++) {
            match = match && Objects.equals(ByteRingBuffer.this.peek(i), array[i]);
            if (!match) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new ByteRingBufferIterator();
    }

    public void clear() {
        // copy 0 over entire array.
        System.arraycopy(new byte[buffer.length], 0, buffer, 0, buffer.length);
        head = 0;
        tail = 0;
        size = 0;
    }

    private class ByteRingBufferIterator implements Iterator<Byte> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public Byte next() {
            return ByteRingBuffer.this.peek(index++);
        }
    }
}
