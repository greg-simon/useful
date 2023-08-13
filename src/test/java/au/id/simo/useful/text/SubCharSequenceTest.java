package au.id.simo.useful.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SubCharSequenceTest {

    @Test
    void testLength() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 0, 5);
        assertEquals(5, subSequence.length());
    }

    @Test
    void testCharAt() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 0, 5);
        assertEquals('H', subSequence.charAt(0));
        assertEquals('o', subSequence.charAt(4));
    }

    @Test
    void testSubSequence() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 0, 5);
        CharSequence newSubSequence = subSequence.subSequence(1, 4);
        assertEquals("ell", newSubSequence.toString());
    }

    @Test
    void testToString() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 7, 12);
        assertEquals("World", subSequence.toString());
    }

    @Test
    void testInvalidIndices() {
        CharSequence original = "Hello, World!";
        assertThrows(IndexOutOfBoundsException.class, () -> new SubCharSequence(original, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> new SubCharSequence(original, 0, 20));
    }

    @Test
    void testInvalidCharAt() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 0, 5);
        assertThrows(IndexOutOfBoundsException.class, () -> subSequence.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> subSequence.charAt(5));
    }

    @Test
    void testInvalidSubSequence() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 2, 7);
        assertThrows(IndexOutOfBoundsException.class, () -> subSequence.subSequence(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> subSequence.subSequence(2, 10));
    }

    @Test
    void testImmutableSubSequence() {
        CharSequence original = "Hello, World!";
        SubCharSequence subSequence = new SubCharSequence(original, 0, 5);
        original = "Modified";
        assertEquals("Hello", subSequence.toString());
    }
}
