package au.id.simo.useful.io.local;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface LocalSessionRegistryTest {

    LocalSessionRegistry getInstance();

    @ParameterizedTest
    @MethodSource("testGetSessionIdOrNullParams")
    default void testGetSessionIdOrNull(String hostname, String expectedSessionId) {
        Assertions.assertEquals(
                expectedSessionId,
                getInstance().getSessionIdOrNull(hostname)
        );
    }
    static Stream<Arguments> testGetSessionIdOrNullParams() {
        return Stream.of(
                // should work
                Arguments.of("namespace.sessionID", "sessionID"),
                Arguments.of(".sessionID", "sessionID"),
                Arguments.of("namespace.0", "0"),
                Arguments.of("namespace.-12345", "-12345"),
                Arguments.of("namespace.1.2.3.4.5.6.7.8", "1.2.3.4.5.6.7.8"),
                // should be null with no exceptions thrown
                Arguments.of("sessionID", null),
                Arguments.of("namespace.", null),
                Arguments.of("namespace,sessionID", null),
                Arguments.of("namespacesessionID", null),
                Arguments.of("", null),
                Arguments.of(null, null)
        );
    }
}
