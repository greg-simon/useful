package au.id.simo.useful;

import java.util.Iterator;
import java.util.Objects;

/**
 * Byte buffer for use with streams.
 * <p>
 * Allows writing to it forever without additional memory usage.
 */
public class ByteRingBuffer implements Iterable<Byte> {

    private final byte[] buffer;

    /**
     * Add index. Points to last written location. Increment then write.
     */
    private int head;
    /**
     * Remove index. Points to oldest value to read from.
     */
    private int tail;

    public ByteRingBuffer(int size) {
        buffer = new byte[size];
        head = -1; //empty
        tail = -1; //empty
    }

    /**
     * Logic for wrapping indexes on array length.
     *
     * @param index head or tail.
     * @return new incremented value, possibly wrapped back to 0 if required.
     */
    protected int incrementIndex(int index) {
        return incrementIndex(index, 1);
    }

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
        head = incrementIndex(head);
        buffer[head] = i;

        // adjust tail if required.
        if (tail == -1) {
            tail = head;
        } else if (tail == head) {
            tail = incrementIndex(tail);
        }
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
     *
     * @param index where 0 means the oldest item in the collection.
     * @return the value that is {@code index} positions from the oldest item in
     * the collection.
     */
    public byte get(int index) {
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
     * removed returned value from the buffer.
     *
     * @return oldest value or throws ArrayIndexOutOfBounds exception if empty.
     */
    public byte remove() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is empty");
        }
        byte t = buffer[tail];
        buffer[tail] = 0;
        if (tail == head) {
            tail = -1;
        } else {
            tail = incrementIndex(tail);
        }
        return t;
    }

    public int size() {
        if (tail == -1) {
            return 0; // initial empty state.
        }

        int size;
        if (tail == head) {
            size = 0;
        } else if (tail > head) {
            size = (buffer.length + head) - tail;
        } else {
            size = head - tail;
        }
        return size + 1;
    }

    public int maxSize() {
        return buffer.length;
    }

    public boolean isEmpty() {
        return tail == -1 || size() == 0;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isFull() {
        return tail == incrementIndex(head);
    }

    public byte[] toArray() {
        return toArray(new byte[size()]);
    }

    public byte[] toArray(byte[] a) {
        int size = size();
        if (size == 0) {
            return a;
        }

        if (size > a.length) {
            a = new byte[size()];
        }

        if (head >= tail) {
            System.arraycopy(buffer, tail, a, 0, size);
        } else {
            //[0,0,h, , ,t,0]
            //[^s1 ^]   [^ ^]  <- seg2
            // segment two to start of newArray
            int segTwoLength = buffer.length - tail;
            System.arraycopy(buffer, tail, a, 0, segTwoLength);
            // segment one to end of newArray
            System.arraycopy(buffer, 0, a, segTwoLength, head + 1);
        }
        return a;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ByteRingBuffer");
        if (head == -1) {
            sb.append('+');
        }
        if (tail == -1) {
            sb.append('-');
        }
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
            match = match && Objects.equals(get(i), array[i]);
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
        head = -1;
        tail = -1;
    }

    private class ByteRingBufferIterator implements Iterator<Byte> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public Byte next() {
            return get(index++);
        }
    }
}
