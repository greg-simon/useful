package au.id.simo.useful;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CheckUtilTest {
    
    public CheckUtilTest() {
    }

    public static Stream<Arguments> testCheckReadWriteArgs() {
        return Stream.of(
            Arguments.of(10, 0, 10, false),
            Arguments.of(10, 10, 0, false),
            Arguments.of(10, 10, 1, true),
            Arguments.of(-1, 0,  1, true),
            Arguments.of(10, -1, 2, true),
            Arguments.of(10, 0, -1, true),
            Arguments.of(10, 0, 11, true)
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void testCheckReadWriteArgs(int arrayLength, int arrayOffset, int length, boolean expectError) {
        if (expectError) {
            assertThrows(IndexOutOfBoundsException.class, () -> {
                CheckUtil.checkReadWriteArgs(arrayLength, arrayOffset, length);
            });
        } else {
            CheckUtil.checkReadWriteArgs(arrayLength, arrayOffset, length);
        }
    }
}
