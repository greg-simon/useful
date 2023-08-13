package au.id.simo.useful.datagen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class WrapIndexTest {

    @Nested
    @DisplayName("Index Range: 0 to 10")
    class ZeroToFiveIndexRange {
        private WrapIndex wrapIndex;

        @BeforeEach
        public void setUp() {
            // Initialize the WrapIndex with a range of 0 to 10
            wrapIndex = new WrapIndex(0, 10);
        }

        @Test
        public void testInitialValue() {
            assertEquals(0, wrapIndex.value(), "Initial value should be the starting index: 0");
        }

        @Test
        public void testNext() {
            assertEquals(1, wrapIndex.next(), "First next() call should return initial value plus one.");
            assertEquals(1, wrapIndex.value(), "After first next() call, the value should be incremented.");
        }

        @Test
        public void testPrevious() {
            // Decrementing the value should result in wrapping back to the end.
            assertEquals(10, wrapIndex.previous(), "Initial previous call should return max value in range");
            assertEquals(10, wrapIndex.value(), "After previous() call, value should be decremented");
        }

        @Test
        public void testAddAndGet() {
            assertEquals(2, wrapIndex.addAndGet(2), "Add to the value then return it: 2");
            assertEquals(2, wrapIndex.value(), "Value should be the same as the previously returned value");
        }
        @Test
        public void testAddAndGetWrapAround() {
            assertEquals(1, wrapIndex.addAndGet(12));
            assertEquals(1, wrapIndex.value(), "Value should be the same as the previously returned value");
        }

        @Test
        public void testSetValue() {
            // Setting the value to 15 should result in 4 (15 % 11).
            wrapIndex.setValue(15);
            assertEquals(4, wrapIndex.value());
        }

        @Test
        public void testInvalidRange() {
            // Test that the constructor throws an IllegalArgumentException for an invalid range.
            assertThrows(IllegalArgumentException.class, () -> new WrapIndex(5, 5));
            assertThrows(IllegalArgumentException.class, () -> new WrapIndex(10, 5));
        }
    }

    @Nested
    @DisplayName("Index range: -5 to 5")
    class NegativePositiveIndexRange {

        private WrapIndex wrapIndex;

        @BeforeEach
        public void setUp() {
            // Initialize the WrapIndex with a range from -5 to 5
            wrapIndex = new WrapIndex(-5, 5);
        }

        @Test
        public void testInitialValue() {
            // The initial value should be the start value, which is -5
            assertEquals(-5, wrapIndex.value(), "The initial value should eb the start of the range: -5");
        }

        @Test
        public void testNext() {
            assertEquals(-4, wrapIndex.next(), "First next() call should return the initial value plus one");
            assertEquals(-4, wrapIndex.value(), "After a next() call the value should be unchanged.");
        }

        @Test
        public void testPrevious() {
            assertEquals(5, wrapIndex.previous(), "Fist previous() call should wrap back to largest value");
            assertEquals(5, wrapIndex.value(), "Value should be the same as last previous() call.");
        }

        @Test
        public void testAddAndGet() {
            //[-5,-4,-3,-2,-1,0,1,2,3,4,5]
            assertEquals(-1, wrapIndex.addAndGet(15), "Starting at -5, plus 15, moving the index 15 times left should wrap to -1");
            assertEquals(-1, wrapIndex.value(), "Should be same value as last returned value.");
        }

        @Test
        public void testSetValue() {
            // Setting the value to 10 should result in 4 (10 % 11).
            wrapIndex.setValue(10);
            //assertEquals(4, wrapIndex.getValue());

            // Setting the value to -10 should result in -4 (-10 % 11).
            wrapIndex.setValue(-10);
            //assertEquals(-4, wrapIndex.getValue());
        }

        @Test
        public void testNegativeToPositiveRange() {
            // Incrementing the value 11 times should wrap around the range.
            for (int i = 0; i < 11; i++) {
                wrapIndex.next();
            }
            //assertEquals(-5, wrapIndex.getValue());

            // Decrementing the value 12 times should wrap around the range in the opposite direction.
            for (int i = 0; i < 12; i++) {
                wrapIndex.next();
            }
            //assertEquals(6, wrapIndex.getValue());
        }

        @Test
        public void testInvalidRange() {
            // Test that the constructor throws an IllegalArgumentException for an invalid range.
            assertThrows(IllegalArgumentException.class, () -> new WrapIndex(5, 5));
            assertThrows(IllegalArgumentException.class, () -> new WrapIndex(10, 5));
        }
    }
}

