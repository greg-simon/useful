package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import au.id.simo.useful.CheckUtil;

/**
 * An interface to a resizable bundle of bytes.
 * <p>
 * Implementations are usually more convenient than raw byte arrays.
 */
public interface ByteBundle extends Resource {

    /**
     * The maximum size of array to allocate (not allocated unless necessary).
     * Some VMs reserve some header words in an array. Attempts to allocate
     * larger arrays may result in OutOfMemoryError: Requested array size
     * exceeds VM limit
     */
    int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    static void checkCopyArgs(int pos, int copyLimit, byte[] array, int arrayIdx, int length) {
        Objects.requireNonNull(array);

        // negative index check
        if (pos < 0 || copyLimit < 0 || arrayIdx < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        // max index check
        if (pos > copyLimit || arrayIdx > array.length) {
            throw new IndexOutOfBoundsException();
        }
        // length checks
        if (length > (copyLimit - pos) || length > (array.length - arrayIdx)) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Copies bytes from the provided {@code src} array into the ByteBundle.
     *
     * @param pos the position in the ByteBuffer the bytes will start being
     * copied to
     * @param src the provided array containing bytes to be copied
     * @param srcPos the array index to start coping from the source array
     * @param length number of bytes to copy from the provided source array
     * @throws IndexOutOfBoundsException if the pos, srcPos or length arguments
     * point to array elements outside the ByteBuffer or source array.
     */
    void copyIn(int pos, byte[] src, int srcPos, int length);

    /**
     * Copies bytes from the provided {@code src} array into the ByteBundle.
     * <p>
     * Equivalent to calling {@link #copyIn(int, byte[], int, int)} with
     * {@code srcPos = 0} and {@code length = src.length}
     * 
     * @see #copyIn(int, byte[], int, int)
     * @param pos the position in the ByteBuffer the bytes will start being
     * copied to
     * @param src the provided array containing bytes to be copied
     * @throws IndexOutOfBoundsException if the pos arguments point to array
     * elements outside the ByteBuffer or the length of the src byte plus the
     * pos argument is larger than the maximum capacity of the ByteBundle.
     */
    default void copyIn(int pos, byte[] src) {
        copyIn(pos, src, 0, src.length);
    }

    /**
     * Copies bytes from the provided InputStream into this ByteBundle.
     * 
     * @param pos the position in this ByteBundle to begin coping into
     * @param in the InputStream to copy the bytes from
     * @throws IOException if there is any issues in reading from the provided
     * InputStream
     * @throws IndexOutOfBoundsException if the max capacity of this ByteBundle
     * is less than the bytes read from the stream
     */
    default void copyIn(int pos, InputStream in) throws IOException {
        IOUtils.copy(in, ((total, src, srcLength) -> 
                copyIn(pos + (int) total, src, 0, srcLength))
        );
    }

    /**
     * Copies the provided byte array into the ByteBundle after any existing
     * bytes.
     * <p>
     * Equivalent to calling {@link #copyIn(int, byte[], int, int)} with
     * {@code pos = this.size()}.
     * @param src the provided array containing bytes to be copied
     * @param srcPos the array index to start coping from the source array
     * @param length number of bytes to copy from the provided source array
     */
    default void append(byte[] src, int srcPos, int length) {
        copyIn(size(), src, srcPos, length);
    }

    /**
     * Copies the provided byte array into the ByteBundle after any existing
     * bytes.
     * <p>
     * Equivalent to calling {@link #copyIn(int, byte[], int, int)} with
     * {@code pos = this.size()} and {@code srcPos = 0} and
     * {@code length = src.length}.
     * @param src the provided array containing bytes to be copied
     */
    default void append(byte[] src) {
        copyIn(size(), src, 0, src.length);
    }

    int copyOut(int pos, byte[] dest, int destPos, int length);

    default int copyOut(int pos, byte[] dest) {
        return copyOut(pos, dest, 0, Math.min(dest.length, size() - pos));
    }

    default int copyOut(byte[] dest) {
        return copyOut(0, dest);
    }

    void clear();

    void trim();

    /**
     *
     * @return The number of bytes that can be held without more allocating more
     * memory.
     */
    int capacity();

    /**
     *
     * @return Maximum number of bytes that can be held in this ByteBundle
     */
    int maxCapacity();

    /**
     *
     * @return The number of bytes that has been copied in
     */
    int size();

    /**
     *
     * @return The number of bytes that can be copied in without throwing an
     * {@link IndexOutOfBoundsException}
     */
    default int remainingMaxCapacity() {
        return maxCapacity() - size();
    }

    @Override
    default InputStream inputStream() {
        return new InputStream() {
            private int index = 0;
            private final byte[] singleByteBuf = new byte[1];
            private final int length = size();
            private final Latch latch = new Latch("Stream Closed");

            @Override
            public int read() throws IOException {
                latch.throwIfClosed();
                if (index >= length) {
                    return -1;
                }
                copyOut(index++, singleByteBuf, 0, 1);
                return singleByteBuf[0] & 0xFF;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                CheckUtil.checkReadWriteArgs(b.length, off, len);
                latch.throwIfClosed();
                if (index >= length) {
                    return -1;
                }
                int remaining = length - index;
                int bytesRequested = Math.min(b.length - off, len);
                int bytesToCopy = Math.min(remaining, bytesRequested);
                index += copyOut(index, b, off, bytesToCopy);
                return bytesToCopy;
            }

            @Override
            public void close() {
                latch.close();
            }
        };
    }

    default byte[] getBytes() {
        byte[] bytes = new byte[size()];
        copyOut(bytes);
        return bytes;
    }
}
