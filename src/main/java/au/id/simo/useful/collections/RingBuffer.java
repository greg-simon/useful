package au.id.simo.useful.collections;

/**
 * Basic RingBuffer implementation for Objects.
 *
 * @param <T> The type of Object type contained within.
 */
public class RingBuffer<T> extends AbstractRingBuffer<T> {

    private final Object[] buffer;

    public RingBuffer(int capacity) {
        super(capacity, null);
        this.buffer = new Object[capacity];
    }

    @Override
    protected void setToArray(int index, T value) {
        buffer[index] = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getFromArray(int index) {
        return (T) buffer[index];
    }

    @Override
    public String toString() {
        return toString("RingBuffer", "null", 5);
    }
}
