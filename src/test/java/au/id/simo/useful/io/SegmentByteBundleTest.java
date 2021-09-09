package au.id.simo.useful.io;

import au.id.simo.useful.io.SegmentByteBundle;
import au.id.simo.useful.io.ByteBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SegmentByteBundleTest implements ByteBundleTest {

    @Override
    public ByteBundle newByteBundle(int initialCapacity, int maxCapacity) {
        return new SegmentByteBundle(initialCapacity, maxCapacity);
    }

    @Test
    @Override
    public void testCapacity() {
        SegmentByteBundle sbb = new SegmentByteBundle();
        sbb.append(new byte[]{1,2,3});
        assertEquals(sbb.getSegmentSize(), sbb.capacity());
    }

    @Test
    @Override
    public void testTrim() {
        SegmentByteBundle sbb = new SegmentByteBundle(5);
        sbb.trim();
        assertEquals(0, sbb.size());
        assertEquals(0, sbb.capacity());
        
        sbb.append(new byte[]{1});
        sbb.trim();
        assertEquals(1, sbb.size());
        assertEquals(sbb.getSegmentSize(), sbb.capacity(), "capacity of one segment");
        assertEquals(1, sbb.getSegmentCount());
        
        // use 3 segments. 15 bytes in total
        sbb.append(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14});
        assertEquals(3, sbb.getSegmentCount());
        
        sbb.clear();
        sbb.trim();
        assertEquals(0, sbb.size());
        assertEquals(0, sbb.getSegmentCount());
    }
    
    /**
     * Test the functionally around only allocating segments that contain bytes.
     */
    @Test
    public void testSparseSegments() {
        SegmentByteBundle sbb = new SegmentByteBundle(5);
        
        sbb.copyIn(95, new byte[]{1,2,3,4,5});
        assertEquals(100, sbb.size());
        assertEquals(19, sbb.getSegmentCount());
        
        sbb.copyIn(35, new byte[]{1,2,3,4,5});
        assertEquals(100, sbb.size());
        assertEquals(19, sbb.getSegmentCount());
        
        sbb.copyIn(60, new byte[]{1,2,3,4,5,6,7,8,9,10});
        assertEquals(100, sbb.size());
        assertEquals(19, sbb.getSegmentCount());
        
        sbb.clear();
        sbb.trim();
        assertEquals(0, sbb.size());
        assertEquals(0, sbb.getSegmentCount());
        assertEquals(0, sbb.capacity());
    }
}
