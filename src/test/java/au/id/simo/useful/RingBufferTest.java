package au.id.simo.useful;

/**
 *
 */
public class RingBufferTest implements AbstractRingBufferTest<Integer> {

    @Override
    public Integer[] testData() {
        return new Integer[] {
            0,1,2,3,4,5,6,7,8,9,0,10
        };
    }

    @Override
    public AbstractRingBuffer<Integer> createRingBuffer(int capacity) {
        return new RingBuffer<>(capacity);
    }
}
