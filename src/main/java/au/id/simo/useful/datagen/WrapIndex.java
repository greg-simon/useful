package au.id.simo.useful.datagen;

/**
 * The WrapIndex class represents an index that wraps around a specified range.
 * <p>
 * Useful for circular buffers and data generators.
 */
public class WrapIndex {

    private final int start;
    private final int rangeLength;
    private int value;

    /**
     * Constructs a new WrapIndex object with the specified maximum value.
     * The index will wrap around from 0 to the given maximum value (exclusive).
     *
     * @param maxValue The maximum value (exclusive) for the index.
     */
    public WrapIndex(int maxValue) {
        this(0, maxValue);
    }

    /**
     * Constructs a new WrapIndex object with the specified range.
     * The index will wrap around within the specified range.
     *
     * @param start The starting value of the index (inclusive).
     * @param end   The ending value of the index (inclusive).
     * @throws IllegalArgumentException if the start is greater than or equal to the end.
     */
    public WrapIndex(int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("start must be smaller and not equal to end.");
        }
        this.start = start;
        this.rangeLength = end - start + 1;
        this.value = start;
    }

    /**
     * Calculates the index value without changing the index.
     * The resulting value will wrap around within the specified range.
     *
     * @param value The value to apply to the specified range, without changing the index.
     * @return The resulting index value wrapped within the specified range if required.
     */
    public int calcValue(int value) {
        return calcAddValue(value, 0);
    }

    /**
     * Calculates the resulting index value after adding the given delta to the current index value.
     * The resulting value will wrap around within the specified range.
     *
     * @param delta The amount to add to the current index value.
     * @return The resulting index value after adding the delta.
     */
    public int calcAddValue(int delta) {
        return calcAddValue(value, delta);
    }

    /**
     * Calculates the resulting index value after adding the given delta to the specified value.
     * The resulting value will wrap around within the specified range.
     *
     * @param value The base value to which the delta will be added.
     * @param delta The amount to add to the base value.
     * @return The resulting index value after adding the delta.
     */
    public int calcAddValue(int value, int delta) {
        int zeroedValue = value - start;
        int newZeroedValue = (zeroedValue + delta) % rangeLength;
        if (newZeroedValue < 0) {
            newZeroedValue += rangeLength;
        }
        return newZeroedValue + start;
    }

    public int next() {
        return addAndGet(1);
    }

    public int previous() {
        return addAndGet(-1);
    }

    /**
     * Adds the specified delta to the current index value and updates the current value.
     * The resulting value will wrap around within the specified range.
     *
     * @param delta The amount to add to the current index value.
     * @return The updated value of the index after adding the delta.
     */
    public int addAndGet(int delta) {
        value = calcAddValue(delta);
        return value;
    }

    public int getAndAdd(int delta) {
        int preAddValue = value;
        value = calcAddValue(delta);
        return preAddValue;
    }

    /**
     * Sets the index value to the specified value.
     * The resulting value will wrap around within the specified range.
     *
     * @param value The new value to set for the index.
     */
    public void setValue(int value) {
        this.value = calcAddValue(value, 0);
    }

    /**
     * Gets the current value of the index.
     *
     * @return The current value of the index.
     */
    public int value() {
        return value;
    }
}
