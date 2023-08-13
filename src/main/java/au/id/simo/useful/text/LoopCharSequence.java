package au.id.simo.useful.text;

import au.id.simo.useful.CheckUtil;
import au.id.simo.useful.datagen.RepeatingChars;

/**
 * A CharSequence that will repeat the provided sequence of characters until the provided limit.
 * <p>
 * E.g.
 * <pre>
 * // This will print: ABCABCABCA
 * System.out.println(new LoopCharSequence("ABC", 10));
 * </pre>
 */
public class LoopCharSequence implements CharSequence {
    private final RepeatingChars chars;
    private final int length;

    public LoopCharSequence(CharSequence sequence, int length) {
        this.chars = new RepeatingChars(sequence);
        this.length = length;
    }

    public LoopCharSequence(RepeatingChars chars, int fromIndex, int toIndex) {
        this.chars = chars;
        this.length = toIndex - fromIndex;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        CheckUtil.checkIndex(index, length);
        return chars.charAt(index);
    }

    @Override
    public String toString() {
        return new StringBuilder(length()).append(this).toString();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new SubCharSequence(this, start, end);
    }
}
