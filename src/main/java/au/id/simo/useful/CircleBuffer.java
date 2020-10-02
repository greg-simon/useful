package au.id.simo.useful;

/**
 * Allows you to write forever with constant memory usage. When the size limit
 * is reached the oldest entry will be overwritten.
 * 
 * Useful for recording monitoring data.
 */
public class CircleBuffer {

    /**
     * The actual ring buffer
     */
    private final int array[];
    /**
     * The index in the array of the next free slot in the buffer.
     */
    private int indexNextFree;
    /**
     * count of the number of items added to the buffer.
     */
    private int addCount;

    /**
     *
     * @param size the size of the ring buffer, this is immutable.
     */
    public CircleBuffer(int size) {
        array = new int[size];
        indexNextFree = 0;
    }

    /**
     *
     * @return number of items added to the buffer. Never larger than the max
     * size of the buffer.
     */
    public int size() {
        return addCount;
    }

    /**
     *
     * @return the maximum size of the buffer.
     */
    public int maxSize() {
        return array.length;
    }

    /**
     * Add an integer to the ring buffer.
     *
     * @param number the number to add to the buffer
     */
    public synchronized void add(int number) {
        if (indexNextFree >= array.length) {
            indexNextFree = 0;
        }
        array[indexNextFree] = number;
        indexNextFree++;

        if (addCount < array.length) {
            addCount++;
        }
    }

    /**
     * Gets the sum of the last X numbers added.
     *
     * @param lastXNumbers the number of recent entries to sum.
     * @return the sum of the last X numbers added.
     */
    public int sumLast(int lastXNumbers) {
        int total = 0;
        int copyOfIndex = indexNextFree;
        int copyOfLast = lastXNumbers;

        // guard against requesting more numbers than
        // the buffer has
        if (copyOfLast > array.length) {
            copyOfLast = array.length;
        }

        for (int i = 0; i < copyOfLast; i++) {
            copyOfIndex--;
            if (copyOfIndex < 0) {
                copyOfIndex = array.length - 1;
            }
            total += array[copyOfIndex];
        }
        return total;
    }

    /**
     * Gets the average of the last X numbers. Integer only math is used. If the
     * given argument is larger than the number of items in the buffer, it will
     * be reduced to the number of items in the buffer.
     *
     * @param lastXNumbers the number of recent entries to average
     * @return the average of the last X entries
     */
    public int averageLast(int lastXNumbers) {
        // guard against requesting more numbers than
        // the buffer has received.
        int copyOfLast = lastXNumbers;
        if (copyOfLast > addCount) {
            copyOfLast = addCount;
        }

        //guard against dividing by zero
        if (copyOfLast == 0) {
            return 0;
        }
        return sumLast(copyOfLast) / copyOfLast;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CircleBuffer[");
        for (Integer num : array) {
            sb.append(num);
            sb.append(',');
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }
}
