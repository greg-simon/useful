package au.id.simo.useful;

/**
 *
 */
public class RingBufferTest implements AbstractRingBufferTest<Integer> {

    @Override
    public Integer[] testData(int arrayLength) {
        Integer[] testData = new Integer[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            testData[i] = i;
        }
        return testData;
    }

    @Override
    public AbstractRingBuffer<Integer> createRingBuffer(int capacity) {
        return new RingBuffer<>(capacity);
    }
}
