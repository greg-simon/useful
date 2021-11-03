package au.id.simo.useful.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import au.id.simo.useful.CheckUtil;

/**
 * Allows adding to it forever without additional memory usage.
 * <p>
 * Usage Modes:
 * <ul>
 * <li>Writes overwrite old values silently: Use add methods</li>
 * <li>Writes throw error if no free space: Use put methods</li>
 * </ul>
 *
 * @param <T> The types contained within the buffer.
 */
public abstract class AbstractRingBuffer<T> implements Iterable<T> {
    
    /**
     * The maximum capacity of this ring buffer.
     */
    protected final int capacity;
    /**
     * Used for zeroing out read elements in the buffer storage.
     */
    protected final T nullValue;
    /**
     * Add index. Points to next location to write to. Write then increment.
     */
    protected int head;
    /**
     * Remove index. Points to oldest value to read from. Read then increment.
     */
    protected int tail;

    /**
     * Represents the number of values stored in this buffer.
     */
    protected int size;

    
    protected AbstractRingBuffer(int capacity, T nullValue) {
        this.capacity = capacity;
        this.nullValue = nullValue;
    }

    /**
     * Set a value to the underlying data store of this buffer.
     * @param index the index of the underlying store of values. Not the
     * relative index.
     * @param value the value to set in the specified index.
     */
    protected abstract void setToArray(int index, T value);
    
    /**
     * Obtain a value from the underlying data store of this buffer.
     * @param index the index of the underlying data store.
     * @return the value in that storage slot
     */
    protected abstract T getFromArray(int index);

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
        return (index + incrementBy) % capacity;
    }
    
    /**
     *
     * @param i add value, overwriting oldest value if at capacity.
     */
    public void add(T i) {
        // write
        setToArray(head, i);
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
     * Same as {@link add} except an exception will be thrown if there is no
     * space.
     * @param i the object to put on the buffer.
     * @throws ArrayIndexOutOfBoundsException if there is no free space left on
     * the buffer for this object.
     */
    public void put(T i) {
        if (isFull()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is full");
        }
        add(i);
    }

    /**
     *
     * @return oldest value or throws ArrayIndexOutOfBounds exception if empty.
     */
    public T peek() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is empty");
        }
        return getFromArray(tail);
    }

    /**
     * Returns a value relative to the oldest value in the buffer.
     * <p>
     * This method as no side effects. No indexes are updated with this call, no
     * extra space is freed in the buffer.
     *
     * @param index where 0 means the oldest item in the collection.
     * @return the value that is {@code index} positions from the oldest item in
     * the collection.
     */
    public T peek(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format(
                            "Index value %s is larger than the number of elements %s.",
                            index,
                            size
                    )
            );
        }
        int relindex = incrementIndex(tail, index);
        return getFromArray(relindex);
    }

    /**
     * Read and removed oldest value from the buffer.
     *
     * @return oldest value or throws ArrayIndexOutOfBounds exception if empty.
     */
    public T read() {
        if (isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("RingBuffer is empty");
        }
        // read
        T t = getFromArray(tail);
        // clear element
        setToArray(tail, nullValue);
        // then increment
        tail = incrementIndex(tail, 1);
        size--;

        return t;
    }

    /**
     * Copies object into the provided array.
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values
     * into
     * @param length the number of values to copy.
     * @return the number of items copied into the destination array, which
     * could be fewer than the provided length when the buffer contains fewer
     * elements.
     */
    public int read(T[] dest, int start, int length) {
        CheckUtil.checkReadWriteArgs(dest.length, start, length);
        int readLength = Math.min(size, length);
        for (int i = 0; i < readLength; i++) {
            dest[start + i] = read();
        }
        return readLength;
    }

    /**
     * Copies buffer contents into the provided array, without consuming values.
     * <p>
     * No indexes will be adjusted, no side effects will occur.
     *
     * @param dest destination array to copy values into
     * @param start the index of the destination array to start copying values
     * into
     * @param length the number of values to copy.
     * @return the number of values copied into the provided array.
     */
    public int peek(T[] dest, int start, int length) {
        CheckUtil.checkReadWriteArgs(dest.length, start, length);
        int readLength = Math.min(size, length);
        for (int i = 0; i < readLength; i++) {
            dest[start + i] = peek(i);
        }
        return readLength;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isNotEmpty() {
        return size > 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isNotFull() {
        return size < capacity;
    }

    public int getFreeSpace() {
        return capacity - size;
    }

    /**
     * {@code toString} helper for subclasses.
     * <p>
     * {@code className[+-showNullValue,showNullValue,showNullValue]}
     * e.g. with className = "RingBuffer", showNullValue = "0" and maxPrint = 2
     * {@code RingBuffer[+-0,0...]}
     * 
     * @param className The returned string prefix.
     * @param showNullValue Any empty element is substituted for this string in the returned String.
     * @param maxPrint the max number of elements to show before adding ellipses.
     * @return A String showing the current state of the Ring Buffer.
     */
    protected String toString(String className, String showNullValue, int maxPrint) {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append('[');
        int maxLoop = Math.min(capacity, maxPrint);
        for (int i = 0; i < maxLoop; i++) {
            if (head == i) {
                sb.append('+');
            }
            if (tail == i) {
                sb.append('-');
            }
            // is this byte actual data or uncleared noise?
            if (isData(i)) {
                sb.append(String.valueOf(getFromArray(i)));
            } else {
                sb.append(showNullValue);
            }
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        if (maxPrint < capacity) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }

    protected boolean isData(int index) {
        if (size == 0) {
            return false;
        }
        if (head > tail) {
            return index >= tail && index < head;
        }
        return index >= tail || index < head;
    }

    public boolean containsArray(T[] array) {
        if (array.length > size) {
            return false;
        }
        // look for first element
        int offset = -1;
        T firstItem = array[0];
        for (int i = 0; i < size; i++) {
            if (Objects.equals(peek(i),firstItem)) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            // no first item found in buffer
            return false;
        }
        // does the rest of the buffer match the array?
        if ((size - offset) < array.length) {
            // not enough elements left to match.
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (!Objects.equals(peek(offset + i), array[i])) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        head = 0;
        tail = 0;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new RingBufferIterator();
    }

    private class RingBufferIterator implements Iterator<T> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public T next() throws NoSuchElementException {
            if (index >= size()) {
                throw new NoSuchElementException("No further elements exist.");
            }
            return peek(index++);
        }
    }
}
