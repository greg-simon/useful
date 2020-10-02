package au.id.simo.useful;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircleBufferTest {

    @Test
    public void testSum() {
        CircleBuffer buffer = new CircleBuffer(3);
        assertEquals(buffer.toString(), "CircleBuffer[0,0,0]");
        assertEquals(buffer.sumLast(3), 0);
        buffer.add(1);
        assertEquals(buffer.toString(), "CircleBuffer[1,0,0]");
        assertEquals(buffer.sumLast(3), 1);
        buffer.add(2);
        assertEquals(buffer.toString(), "CircleBuffer[1,2,0]");
        assertEquals(buffer.sumLast(3), 3);
        buffer.add(3);
        assertEquals(buffer.toString(), "CircleBuffer[1,2,3]");
        assertEquals(buffer.sumLast(3), 6);
        buffer.add(4);
        assertEquals(buffer.toString(), "CircleBuffer[4,2,3]");
        assertEquals(buffer.sumLast(3), 9);
        buffer.add(5);
        assertEquals(buffer.toString(), "CircleBuffer[4,5,3]");
        assertEquals(buffer.sumLast(3), 12);
        buffer.add(6);
        assertEquals(buffer.toString(), "CircleBuffer[4,5,6]");
        assertEquals(buffer.sumLast(3), 15);
    }

    @Test
    public void testAverage() {
        CircleBuffer buffer = new CircleBuffer(3);
        assertEquals(buffer.toString(), "CircleBuffer[0,0,0]");
        assertEquals(buffer.averageLast(3), 0);

        buffer.add(1);
        assertEquals(buffer.toString(), "CircleBuffer[1,0,0]");
        assertEquals(buffer.averageLast(1), 1);
        assertEquals(buffer.averageLast(2), 1);// only use first number
        assertEquals(buffer.averageLast(3), 1);
        assertEquals(buffer.averageLast(4), 1);

        buffer.add(2);
        assertEquals(buffer.toString(), "CircleBuffer[1,2,0]");
        assertEquals(buffer.averageLast(1), 2);
        assertEquals(buffer.averageLast(2), 1);
        assertEquals(buffer.averageLast(3), 1);
        assertEquals(buffer.averageLast(4), 1);

        buffer.add(3);
        assertEquals(buffer.toString(), "CircleBuffer[1,2,3]");
        assertEquals(buffer.averageLast(1), 3);
        assertEquals(buffer.averageLast(2), 2);
        assertEquals(buffer.averageLast(3), 2);
        assertEquals(buffer.averageLast(4), 2);
    }

    /**
     * Runs 200 thread all adding 1000 numbers (all the numbers are 1) to the
     * one buffer instance that only holds the last 10 numbers.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testConcurrent() throws Exception {
        CircleBuffer buffer = new CircleBuffer(10);
        Integer[] numbersToAdd = new Integer[1000];

        // init number to add
        for (int i = 0; i < numbersToAdd.length; i++) {
            numbersToAdd[i] = 1;
        }

        // init threads
        Thread threads[] = new Thread[200];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new AdderRunnable(buffer, numbersToAdd));
        }

        //start threads
        for (Thread t : threads) {
            t.start();
        }

        // wait for them all to finish
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(buffer.sumLast(10), 10);
        assertEquals(buffer.averageLast(10), 1);
    }

    private static class AdderRunnable implements Runnable {

        private Integer[] numbersToAdd;
        private CircleBuffer buffer;

        public AdderRunnable(CircleBuffer buffer, Integer... numbersToAdd) {
            this.numbersToAdd = numbersToAdd;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            for (Integer num : numbersToAdd) {
                buffer.add(num);
            }
        }
    }
}
