package au.id.simo.useful;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 * Utility class for common method argument checks.
 * <p>
 * Check individual methods descriptions for any replacements in Java versions
 * after 8.
 */
public class CheckUtil {

    private CheckUtil() {
    }

    /**
     * Returns the first argument if it is non-{@code null} and otherwise
     * returns the non-{@code null} second argument.
     * <p>
     * NOTE: Backported from Java 9+ Objects class.
     *
     * @param obj an object
     * @param defaultObj a non-{@code null} object to return if the first
     * argument is {@code null}
     * @param <T> the type of the reference
     * @return the first argument if it is non-{@code null} and otherwise the
     * second argument if it is non-{@code null}
     * @throws NullPointerException if both {@code obj} is null and
     * {@code defaultObj} is {@code null}
     * @since 9
     */
    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");
    }

    public static void checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void checkFromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Validates the arguments commonly found in {@code read(T[], int, int)}
     * style methods.
     *
     * @see InputStream#read(byte[], int, int)
     * @see Reader#read(char[], int, int)
     * @see OutputStream#write(byte[], int, int)
     * @see Writer#write(char[], int, int)
     * @param arrayLength the length of the source or destination array.
     * @param arrayOffset the offset to begin reading or writing to the array.
     * @param copyLength the number of elements to in the array to read or
     * write.
     * @exception IndexOutOfBoundsException If {@code arrayOffset} is negative,
     * {@code copyLength} is negative, or {@code copyLength} is greater than
     * {@code arrayLength - arrayOffset}
     */
    public static void checkReadWriteArgs(int arrayLength, int arrayOffset, int copyLength) {
        if (arrayOffset < 0 || copyLength < 0 || copyLength > arrayLength - arrayOffset) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Checks the start and end arguments against the provided array length.
     * @param arrayLength The length of the array to check
     * @param start The start index of the array to check
     * @param end The end index of the array to check
     * @throws IndexOutOfBoundsException if {@code start} or {@code end} are
     * negative, if {@code end} is greater than {@code arrayLength}, or if
     * {@code start} is greater than {@code end}
     */
    public static void checkStartEnd(int arrayLength, int start, int end) {
        if (start < 0 || end < 0 || end > arrayLength || start > end) {
            throw new IndexOutOfBoundsException();
        }
    }
}
