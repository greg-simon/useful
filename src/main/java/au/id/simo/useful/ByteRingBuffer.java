package au.id.simo.useful;

import java.util.Objects;

/**
 * Byte focused implementation of RingBuffer with efficient methods for reading
 * and writing bytes.
 */
public class ByteRingBuffer extends RingBuffer<Byte> {

    private final byte[] buffer;

    public ByteRingBuffer(int capacity) {
        super(capacity);
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
     */
    public int read(byte[] dest, int start, int length) {
        int totalReadLength = peek(dest, start, length);
        tail = incrementIndex(tail, totalReadLength);
        size -= totalReadLength;
        return totalReadLength;
    }

    /**
     * More efficient implementation of {@code peek(Byte[], int, int) }.
     *
     * @param dest destination byte array to copy bytes to
     * @param start the start index of the destination array
     * @param length the number of bytes to copy into the destination array
     * @return the number of bytes actually copied. Which could be different to
     * the length argument.
     */
    public int peek(byte[] dest, int start, int length) {
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
        int totalReadLength = segTwoReadLength + segOneReadLength;
        return totalReadLength;
    }

    public byte[] toArray() {
        byte[] array = new byte[size];
        peek(array, 0, size);
        return array;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ByteRingBuffer");
        sb.append('[');
        int maxLoop = Math.min(buffer.length, 50);
        for (int i = 0; i < maxLoop; i++) {
            if (head == i) {
                sb.append('+');
            }
            if (tail == i) {
                sb.append('-');
            }
            // is this byte actual data or uncleared noise?
            if (isData(i)) {
                sb.append(buffer[i] & 0xff);
            } else {
                sb.append("0");
            }
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        if (maxLoop < buffer.length) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public boolean contains(byte[] byteArray) {
        Objects.requireNonNull(byteArray);
        if (byteArray.length > size) {
            return false;
        }
        
        if (byteArray.length == 0) {
            return size == 0;
        }
        
        // look for first element
        int offset = -1;
        byte firstItem = byteArray[0];
        for (int i = 0; i < byteArray.length; i++) {
            int relIndex = incrementIndex(tail, i);
            if (buffer[relIndex] == firstItem) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            // no first item found in buffer
            return false;
        }
        // does the rest of the buffer match the array?
        if ((size - offset) < byteArray.length) {
            // not enough elements left to match.
            return false;
        }

        for (int i = 0; i < byteArray.length; i++) {
            int relIndex = incrementIndex(tail, offset + i);
            if (buffer[relIndex] != byteArray[i]) {
                return false;
            }
        }
        return true;
    }
}
