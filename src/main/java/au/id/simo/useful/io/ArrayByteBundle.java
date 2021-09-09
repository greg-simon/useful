package au.id.simo.useful.io;

import java.util.Arrays;

/**
 * An array backed structure that expands as required up to a set limit.
 */
public class ArrayByteBundle implements ByteBundle {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private final int maxCapacity;

    private byte[] buffer;
    private int size;
    private int resizeCount;
    private long totalAllocation;

    public ArrayByteBundle() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ArrayByteBundle(int initialCapacity) {
        this(initialCapacity, MAX_ARRAY_SIZE);
    }

    public ArrayByteBundle(int initialCapacity, int maxCapacity) {
        if (maxCapacity > MAX_ARRAY_SIZE) {
            throw new IndexOutOfBoundsException(
                    "Cannot have a maxCapacity greater than " + MAX_ARRAY_SIZE
            );
        }
        this.maxCapacity = maxCapacity;
        this.buffer = new byte[initialCapacity];
        this.totalAllocation = initialCapacity;
    }

    /**
     * Ensures backing array is grown if required to contain the number bytes
     * specified.
     * <p>
     * Capacity may be grown to more than the requiredCapacity.
     *
     * @param requiredCapacity the minimum capacity that is required
     * @throws IndexOutOfBoundsException if requiredCapacity is larger than
     * {@link #maxCapacity()}
     */
    public void ensureCapacity(int requiredCapacity) {
        if (buffer.length >= requiredCapacity) {
            // no resizing required.
            return;
        }
        if (requiredCapacity > maxCapacity) {
            throw new IndexOutOfBoundsException(String.format(
                    "requiredCapacity (%d) is greater than maxCapacity (%d)",
                    requiredCapacity,
                    maxCapacity
            ));
        }
        // try doubling the existing capacity
        int newCapacity = buffer.length << 1;
        // use the required capacity if it's bigger than the double of existing.
        newCapacity = Math.max(newCapacity, requiredCapacity);
        // use the max limit for the capacity if the newCapacity is larger.
        newCapacity = Math.min(newCapacity, maxCapacity);
        buffer = Arrays.copyOf(buffer, newCapacity);
        totalAllocation += newCapacity;
        resizeCount++;
    }

    public int getResizeCount() {
        return resizeCount;
    }

    public long totalAllocation() {
        return totalAllocation;
    }

    @Override
    public void copyIn(int pos, byte[] src, int srcPos, int length) {
        ByteBundle.checkCopyArgs(pos, maxCapacity, src, srcPos, length);
        int newSize = Math.max(size, pos + length);
        ensureCapacity(newSize);
        System.arraycopy(src, srcPos, buffer, pos, length);
        size = newSize;
    }

    @Override
    public int copyOut(int pos, byte[] dest, int destPos, int length) {
        ByteBundle.checkCopyArgs(pos, buffer.length, dest, destPos, length);
        System.arraycopy(buffer, pos, dest, destPos, length);
        return length;
    }

    @Override
    public int capacity() {
        return buffer.length;
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public void trim() {
        if (buffer.length == size) {
            return;
        }
        buffer = Arrays.copyOf(buffer, size);
    }
}
