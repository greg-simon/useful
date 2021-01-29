package au.id.simo.useful.experimental;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ArrayBagTest {
    @Test
    public void testSimpleUsage() {
        ArrayBag<Integer> bag = new ArrayBag<>();
        bag.add(1, 2, 3);
        bag.add(new Integer[]{4, 5, 6});
        
        assertEquals(6, bag.size());
        
        assertEquals(1, bag.get(0));
        assertEquals(2, bag.get(1));
        assertEquals(3, bag.get(2));
        assertEquals(4, bag.get(3));
        assertEquals(5, bag.get(4));
        assertEquals(6, bag.get(5));
    }
    
    @Test
    public void testCopyTo() {
        ArrayBag<Integer> bag = new ArrayBag<>();
        bag.add(1, 2);
        bag.add(3, 4);
        bag.add(5);
        bag.add(6);
        
        assertEquals(6, bag.size());
        
        Integer[] destArray = new Integer[6];
        bag.copyTo(0, destArray, 0, 6);
        assertArrayEquals(new Integer[]{1,2,3,4,5,6}, destArray);
    }
    
    @Test
    public void testAddingZeroLengthArrays() {
        ArrayBag<Integer> bag = new ArrayBag<>();
        bag.add();
        bag.add();
        
        assertEquals(0, bag.size());
        
        Exception e = assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            bag.get(0);
        });
    }
}
