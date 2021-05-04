package au.id.simo.useful;

/**
 * Basic RingBuffer implementation for Objects.
 * @param <T> The type of Objects contained within.
 */
public class RingBufferImpl<T> extends RingBuffer<T> {

    private final Object[] buffer;
    
    public RingBufferImpl(int capacity) {
        super(capacity);
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
}
