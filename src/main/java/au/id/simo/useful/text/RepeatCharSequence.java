package au.id.simo.useful.text;

import au.id.simo.useful.CheckUtil;

/**
 * A CharSequence for repeated characters. It's constructed with a single
 * character and a count of how many times it is repeated.
 * <p>
 * It allows for a kind of run length encoded string like object to be used to
 * save space on the heap.
 */
public class RepeatCharSequence implements CharSequence {
    
    private final char c;
    private final int count;

    /**
     * 
     * @param c the character that will be repeated.
     * @param count the number of times the character is to be repeated.
     * @throws IndexOutOfBoundsException thrown if {@code count} is less than
     * zero.
     */
    public RepeatCharSequence(char c, int count) {
        if (count < 0) {
            throw new IndexOutOfBoundsException("count cannot be less than zero");
        }
        this.c = c;
        this.count = count;
    }
    
    public char getChar() {
        return c;
    }
    
    public int getCount() {
        return count;
    }

    @Override
    public int length() {
        return count;
    }

    @Override
    public char charAt(int index) {
        CheckUtil.checkStartEnd(count, index, count);
        return c;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        CheckUtil.checkStartEnd(count, start, end);
        return new RepeatCharSequence(c, end - start);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(count);
        sb.append(this);
        return sb.toString();
    }
}
