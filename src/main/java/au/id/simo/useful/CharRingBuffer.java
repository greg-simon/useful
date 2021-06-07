package au.id.simo.useful;

import java.util.Objects;

/**
 * Character focused implementation of RingBuffer with efficient methods for
 * reading and writing bytes.
 */
public class CharRingBuffer extends AbstractRingBuffer<Character> implements CharSequence {

    private final char[] buffer;

    public CharRingBuffer(int capacity) {
        super(capacity);
        buffer = new char[capacity];
    }

    @Override
    protected void setToArray(int index, Character value) {
        buffer[index] = value;
    }

    @Override
    protected Character getFromArray(int index) {
        return buffer[index];
    }

    public void add(char i) {
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

    public void add(CharSequence chars) {
        for (int i = 0; i < chars.length(); i++) {
            add(chars.charAt(i));
        }
    }

    /**
     * Same as {@link add} except an exception will be thrown if there is no
     * space.
     * @param chars the sequence of chars to put on the buffer.
     * @throws ArrayIndexOutOfBoundsException if there is no free space left on
     * the buffer for all of the chars in the sequence.
     */
    public void put(CharSequence chars) {
        if (chars.length() > getFreeSpace()) {
            throw new ArrayIndexOutOfBoundsException("Not enough free space to add all chars.");
        }
        for (int i = 0; i < chars.length(); i++) {
            put(chars.charAt(i));
        }
    }

    /**
     * Copies chars into the provided array.
     *
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values
     * into
     * @param length the number of values to copy.
     * @return the number of chars copied into the destination array.
     */
    public int read(char[] dest, int start, int length) {
        int totalReadLength = peek(dest, start, length);
        tail = incrementIndex(tail, totalReadLength);
        size -= totalReadLength;
        return totalReadLength;
    }

    /**
     * More efficient implementation of {@code peek(Byte[], int, int) }.
     *
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values
     * into
     * @param length the number of values to copy.
     * @return the number of values copied into the provided array.
     */
    public int peek(char[] dest, int start, int length) {
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

    public char[] toArray() {
        char[] array = new char[size];
        peek(array, 0, size);
        return array;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CharRingBuffer");
        sb.append('[');
        int maxLoop = Math.min(buffer.length, 50);
        for (int i = 0; i < maxLoop; i++) {
            if (head == i) {
                sb.append('+');
            }
            if (tail == i) {
                sb.append('-');
            }
            // is this char actual data or uncleared noise?
            if (isData(i)) {
                sb.append(buffer[i]);
            } else {
                sb.append(" ");
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

    public boolean contains(CharSequence charSeq) {
        Objects.requireNonNull(charSeq);
        if (charSeq.length() > size) {
            return false;
        }
        
        if (charSeq.length() == 0) {
            return size == 0;
        }
        
        // look for first element
        int offset = -1;
        char firstItem = charSeq.charAt(0);
        for (int i = 0; i < charSeq.length(); i++) {
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
        // does the rest of the buffer match the charSeq?
        if ((size - offset) < charSeq.length()) {
            // not enough chars left to match.
            return false;
        }

        for (int i = 0; i < charSeq.length(); i++) {
            int relIndex = incrementIndex(tail, offset + i);
            if (buffer[relIndex] != charSeq.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int length() {
        return size;
    }

    @Override
    public char charAt(int index) {
        return peek(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // TODO: Verify args
        char[] buf = new char[(end - start)];
        peek(buf, start, end);
        return new String(buf);
    }
}
