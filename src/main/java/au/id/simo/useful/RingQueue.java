package au.id.simo.useful;

import java.util.AbstractQueue;
import java.util.Iterator;

/**
 * A fixed size implementation of {@link Queue}.
 * <p>
 * It will always accept new entries as they will silently overwrite existing
 * entries if the collection is full.
 * @param <E>
 */
public class RingQueue<E> extends AbstractQueue<E> {
    /**
     * Flag to indicate the head or tail index pointer is not set.
     */
    private static final int INDEX_EMPTY = -1;
    private final Object[] array;
    /**
     * Add index. Points to last written location. Increment then write.
     */
    private int head;
    /**
     * Remove index. Points to oldest value to read from.
     */
    private int tail;

    public RingQueue(int size) {
        array = new Object[size];
        head = INDEX_EMPTY;
        tail = INDEX_EMPTY;
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
        return (index + incrementBy) % array.length;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new RingQueueIterator();
    }

    @Override
    public int size() {
        if (tail == INDEX_EMPTY) {
            return 0; // initial empty state.
        }

        int size;
        if (tail == head) {
            size = 0;
        } else if (tail > head) {
            size = (array.length + head) - tail;
        } else {
            size = head - tail;
        }
        return size + 1;
    }

    /**
     * Will overwrite oldest entries to fit new entries in.
     * 
     * @param e
     * @return 
     */
    @Override
    public boolean offer(E e) {
        head = incrementIndex(head);
        array[head] = e;

        // adjust tail if required.
        if (tail == INDEX_EMPTY) {
            tail = head;
        } else if (tail == head) {
            tail = incrementIndex(tail);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        Object t = array[tail];
        array[tail] = null;
        if (tail == head) {
            tail = INDEX_EMPTY;
            head = INDEX_EMPTY;
        } else {
            tail = incrementIndex(tail);
        }
        return (E) t;
    }

    @Override
    public E peek() {
        if(isEmpty()) {
            return null;
        }
        return peek(0);
    }
    
    /**
     * 
     * @param index Relative index, where 0 means the oldest item in the collection.
     * @return the value that is {@code index} positions from the oldest item in
     * the collection.
     */
    @SuppressWarnings("unchecked")
    public E peek(int index) {
        if (index >= size()) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Index value %s is larger than the number of elements %s.",
                             index,
                             size()
                    )
            );
        }
        int relindex = incrementIndex(tail, index);
        return (E) array[relindex];
    }
    
    public int maxSize() {
        return array.length;
    }
    
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isFull() {
        return tail == incrementIndex(head);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RingQueue");
        if (head == INDEX_EMPTY) {
            sb.append('+');
        }
        if (tail == INDEX_EMPTY) {
            sb.append('-');
        }
        sb.append('[');
        for (int i = 0; i < array.length; i++) {
            if (head == i) {
                sb.append('+');
            }
            if (tail == i) {
                sb.append('-');
            }
            sb.append(String.valueOf(array[i]));
            sb.append(',');
        }
        sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        return sb.toString();
    }
    
    private class RingQueueIterator implements Iterator<E> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            return peek(index++);
        }
    }
}
