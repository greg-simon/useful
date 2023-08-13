package au.id.simo.useful.text;

import au.id.simo.useful.CheckUtil;

/**
 * A class that represents a subsequence of a {@link CharSequence}.
 * The SubCharSequence class allows you to create a view of a portion of a given CharSequence,
 * providing a convenient way to work with sub-parts of the original sequence without creating a new copy.
 */
public class SubCharSequence implements CharSequence {

    private final CharSequence sequence;
    private final int fromIndex;
    private final int toIndex;

    /**
     * Constructs a SubCharSequence object with the specified range of the original CharSequence.
     *
     * @param sequence The original CharSequence from which to create the subsequence.
     * @param fromIndex The starting index (inclusive) of the subsequence within the original CharSequence.
     * @param toIndex The ending index (exclusive) of the subsequence within the original CharSequence.
     * @throws IndexOutOfBoundsException if fromIndex or toIndex are out of range.
     */
    public SubCharSequence(CharSequence sequence, int fromIndex, int toIndex) {
        this.sequence = sequence;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        CheckUtil.checkFromToIndex(fromIndex, toIndex, sequence.length() + fromIndex);
    }

    /**
     * Returns the length of the SubCharSequence.
     *
     * @return The length of the SubCharSequence.
     */
    @Override
    public int length() {
        return toIndex - fromIndex;
    }

    /**
     * Returns the character at the specified index in the SubCharSequence.
     *
     * @param index The index of the character to retrieve.
     * @return The character at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public char charAt(int index) {
        CheckUtil.checkIndex(index, length());
        return sequence.charAt(index + fromIndex);
    }

    /**
     * Returns a new CharSequence that is a subsequence of this SubCharSequence.
     *
     * @param start The starting index (inclusive) of the subsequence to return.
     * @param end The ending index (exclusive) of the subsequence to return.
     * @return A new CharSequence that represents the specified subsequence of this SubCharSequence.
     * @throws IndexOutOfBoundsException if start or end are out of range.
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        CheckUtil.checkFromToIndex(start, end, length());
        return new SubCharSequence(sequence, start + fromIndex, end + fromIndex);
    }

    /**
     * Returns a string representation of the SubCharSequence.
     *
     * @return A string representation of the SubCharSequence.
     */
    @Override
    public String toString() {
        return new StringBuilder(length()).append(this).toString();
    }
}
