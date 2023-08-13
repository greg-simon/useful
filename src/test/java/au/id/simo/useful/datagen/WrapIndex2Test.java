package au.id.simo.useful.datagen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrapIndex2Test {
    @Test
    public void testBoundIndex_Basic() {
        WrapIndex idx = new WrapIndex(0, 2);
        assertEquals(0, idx.value(), "Initial value is lower bound range");
        assertEquals(1, idx.next());
        assertEquals(2, idx.next());
        assertEquals(0, idx.next(), "Expect value to loop around");
        assertEquals(2, idx.previous(), "Expect value to loop around backwards");
    }

    @Test
    public void testBoundIndex_StraddleZero() {
        WrapIndex idx = new WrapIndex(-1, 1);
        assertEquals(-1, idx.value(), "Initial value is lower bound range");
        assertEquals(0, idx.next());
        assertEquals(1, idx.next());
        assertEquals(-1, idx.next(), "Expect value to loop around");
        assertEquals(1, idx.previous(), "Expect value to loop around backwards");
    }
    @Test
    public void testBoundIndex_Plus5() {
        WrapIndex idx = new WrapIndex(5, 7);
        assertEquals(5, idx.value(), "Initial value is lower bound range");
        assertEquals(6, idx.next());
        assertEquals(7, idx.next());
        assertEquals(5, idx.next(), "Expect value to loop around");
        assertEquals(7, idx.previous(), "Expect value to loop around backwards");
    }
}
