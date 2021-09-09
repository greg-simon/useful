package au.id.simo.useful.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ArrayByteBundleTest implements ByteBundleTest {
    
    @Override
    public ByteBundle newByteBundle(int initialCapacity, int maxCapacity) {
        return  new ArrayByteBundle(initialCapacity, maxCapacity);
    }
    
    @Test
    public void testDefaultConstructor() {
        ArrayByteBundle abb = new ArrayByteBundle();
        assertTrue(abb.capacity() > 0);
    }
    
    @Test
    public void testTooLargeMaxCapacity() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            new ArrayByteBundle(10, Integer.MAX_VALUE);
        });
    }
    
    @Test
    public void testEnsureCapacity() {
        ArrayByteBundle abb = new ArrayByteBundle(10,100);
        abb.ensureCapacity(10);
        assertTrue(abb.capacity() >= 10);
        abb.ensureCapacity(60);
        assertTrue(abb.capacity() >= 60);
        abb.ensureCapacity(10);
        assertTrue(abb.capacity() >= 60);
        abb.ensureCapacity(99);
        assertTrue(abb.capacity() >= 99);
        abb.ensureCapacity(100);
        assertTrue(abb.capacity() == 100);
        assertThrows(IndexOutOfBoundsException.class, () -> {
                abb.ensureCapacity(1000);
        });
        assertTrue(abb.capacity() == 100);
    }
    
    @Test
    public void testResizeCount() {
        ArrayByteBundle abb = new ArrayByteBundle(10);
        assertEquals(0, abb.getResizeCount());
        assertEquals(10, abb.capacity());
        abb.append(new byte[]{1,2,3,4,5,6,7,8,9,0,11});
        assertEquals(1, abb.getResizeCount());
        assertTrue(abb.capacity() > 10);
    }
    
    @Test
    public void testTotalAllocation() {
        ArrayByteBundle abb = new ArrayByteBundle(10);
        assertEquals(10, abb.totalAllocation());
        abb.append(new byte[]{1,2,3,4,5,6,7,8,9,0,11});
        assertTrue(abb.totalAllocation() > 10);
    }
}
