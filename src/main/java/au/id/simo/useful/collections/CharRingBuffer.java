package au.id.simo.useful.collections;

import java.util.Objects;

/**
 * Character focused implementation of RingBuffer with efficient methods for
 * reading and writing bytes.
 */
public class CharRingBuffer extends AbstractRingBuffer<Character> implements CharSequence {

    private final char[] buffer;

    public CharRingBuffer(int capacity) {
        // use default char value '\u0000'
        super(capacity, Character.MIN_VALUE);
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
            // if old value was overridden, update the tail.
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
     * Same as {@link #add(CharSequence)}} except an exception will be thrown if there is no
     * space.
     * @param chars the sequence of chars to put on the buffer.
     * @throws ArrayIndexOutOfBoundsException if there is no free space left on
     * the buffer for all the chars in the sequence.
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
        return typelessPeek(buffer, dest, start, length);
    }

    public char[] toArray() {
        char[] array = new char[size];
        peek(array, 0, size);
        return array;
    }

    @Override
    public String toString() {
        return toString("CharRingBuffer", " ", 20);
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
