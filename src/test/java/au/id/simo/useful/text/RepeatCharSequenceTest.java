package au.id.simo.useful.text;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class RepeatCharSequenceTest {

    @Test
    public void testToString() {
        CharSequence cs = new RepeatCharSequence('a',5);
        assertEquals("aaaaa", cs.toString());
    }
    
    @ParameterizedTest
    @MethodSource("getLengthCharAtArgs")
    public void testLengthCharAt(char c, int count) {
        if (count < 0) {
            assertThrows(IndexOutOfBoundsException.class, () -> {
                new RepeatCharSequence(c,count);
            });
            return;
        }
        final RepeatCharSequence rcs  = new RepeatCharSequence(c,count);
        assertEquals(count, rcs.length());
        for (int i = 0; i < rcs.length(); i++) {
            assertEquals(c, rcs.charAt(i));
        }
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            rcs.charAt(count+1);
        });
    }
    
    private static Stream<Arguments> getLengthCharAtArgs() {
        return Stream.of(
                // char, count
                Arguments.of('a',5),
                Arguments.of('b',1),
                Arguments.of('c',-1),
                Arguments.of('d',-100),
                Arguments.of('e',0),
                Arguments.of('f',100)
        );
    }
}
