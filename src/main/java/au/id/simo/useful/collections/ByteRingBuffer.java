package au.id.simo.useful.collections;

import au.id.simo.useful.CheckUtil;

import java.util.Objects;

/**
 * Byte focused implementation of RingBuffer with efficient methods for reading
 * and writing bytes.
 */
public class ByteRingBuffer extends AbstractRingBuffer<Byte> {

    private final byte[] buffer;

    public ByteRingBuffer(int capacity) {
        super(capacity, (byte)0);
        buffer = new byte[capacity];
    }

    @Override
    protected void setToArray(int index, Byte value) {
        buffer[index] = value;
    }

    @Override
    protected Byte getFromArray(int index) {
        return buffer[index];
    }

    public void add(int i) {
        // write
        buffer[head] = (byte)i;
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
     * Copies bytes into the provided array.
     *
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values
     * into
     * @param length the number of values to copy.
     * @return the number of bytes copied into the destination array.
     * @exception IndexOutOfBoundsException If {@code start} is negative,
     * {@code length} is negative, or {@code length} is greater than
     * {@code dest.length - start}
     */
    public int read(byte[] dest, int start, int length) {
        CheckUtil.checkReadWriteArgs(dest.length, start, length);
        int totalReadLength = peek(dest, start, length);
        tail = incrementIndex(tail, totalReadLength);
        size -= totalReadLength;
        return totalReadLength;
    }
    
    /**
     * Skips reading the next count of bytes.
     * 
     * @param count Moves the ring buffer tail forward @{code count} times, and
     * reduces the same amount from the size.
     */
    public void skip(int count) {
        if (count > size) {
            throw new ArrayIndexOutOfBoundsException("Can't skip more bytes that is already contained.");
        }
        tail = incrementIndex(tail, count);
        size -= count;
    }

    /**
     * More efficient implementation of {@code peek(Byte[], int, int) }.
     *
     * @param dest destination byte array to copy bytes to
     * @param start the start index of the destination array
     * @param length the number of bytes to copy into the destination array
     * @return the number of bytes actually copied. Which could be different to
     * the length argument.
     * @exception IndexOutOfBoundsException If {@code start} is negative,
     * {@code length} is negative, or {@code length} is greater than
     * {@code dest.length - start}
     */
    public int peek(byte[] dest, int start, int length) {
        CheckUtil.checkReadWriteArgs(dest.length, start, length);
        // buffer array could have two segments to copy out of order. One at
        // start of the buffer and one at the end.
        // (h is head index, t is tail index)
        // [0,0,0,0,h, , ,t,0,0,0,0]
        // [^ seg1  ^]   [^ seg2  ^]
        // Steps: Copy segment two to start of dest array, as seg two will be
        // the oldest values, then copy segment to the dest array after segment
        // one.
        // Ending with:
        // [t,0,0,0,0,0,0,0,0,h]
        // [^ seg2  ^][^ seg1 ^]
        int readLength = Math.min(size, length);

        // segment two to the start of the destination.
        int segTwoLength = buffer.length - tail;
        int segTwoReadLength = Math.min(readLength, segTwoLength);
        System.arraycopy(buffer, tail, dest, start, segTwoReadLength);

        // segment one to end of newArray
        int segOneReadLength = Math.min(head + 1, readLength - segTwoReadLength);
        System.arraycopy(buffer, 0, dest, start + segTwoReadLength, segOneReadLength);

        // sum segment lengths and return
        return segTwoReadLength + segOneReadLength;
    }

    public byte[] toArray() {
        byte[] array = new byte[size];
        peek(array, 0, size);
        return array;
    }

    @Override
    public String toString() {
        return toString("ByteRingBuffer", "0", 20);
    }
    
    public boolean contains(byte[] byteArray) {
        return indexOf(byteArray) > -1;
    }
    
    public int indexOf(byte[] byteArray) {
        Objects.requireNonNull(byteArray);
        if (byteArray.length > size) {
            return -1;
        }
        
        if (byteArray.length == 0) {
            if (size == 0) {
                return 0;
            } else {
                // not found
                return -1;
            }
        }
        
        // look for first element
        int offset = -1;
        byte firstItem = byteArray[0];
        for (int i = 0; i < size; i++) {
            int relIndex = incrementIndex(tail, i);
            if (buffer[relIndex] == firstItem) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            // no first item found in buffer
            return -1;
        }
        // does the rest of the buffer match the array?
        if ((size - offset) < byteArray.length) {
            // not enough elements left to match.
            return -1;
        }

        for (int i = 0; i < byteArray.length; i++) {
            int relIndex = incrementIndex(tail, offset + i);
            if (buffer[relIndex] != byteArray[i]) {
                return -1;
            }
        }
        return offset;
    }
}