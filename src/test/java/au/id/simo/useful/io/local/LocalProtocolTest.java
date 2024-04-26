package au.id.simo.useful.io.local;

import java.util.stream.Stream;

import au.id.simo.useful.text.RepeatCharSequence;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class LocalProtocolTest {

    @ParameterizedTest
    @MethodSource("testNamespaceOrNullParams")
    public void testNamespaceOrNull(String nameSpace, String expected) {
        if (expected == null) {
            assertNull(LocalProtocol.namespaceOrNull(nameSpace));
        } else {
            assertEquals(expected, LocalProtocol.namespaceOrNull(nameSpace));
        }
    }

    public static Stream<Arguments> testNamespaceOrNullParams() {
        return Stream.of(
                Arguments.of("namespace.sessionId", "namespace"),
                Arguments.of("namespace,sessionId", null),
                Arguments.of("namespacesessionId", null),
                Arguments.of(".sessionId", null),
                Arguments.of("namespace.", "namespace"),
                Arguments.of("namespace.session.id", "namespace")
        );
    }
    @ParameterizedTest
    @MethodSource("testValidateRegistryNameParams")
    public void testValidateRegistryName(String registryName, IllegalArgumentException expectedException) {
        if (expectedException != null) {
            Exception e = assertThrows(expectedException.getClass(), () -> LocalProtocol.validateRegistryName(registryName));
            assertEquals(expectedException.getMessage(), e.getMessage());
            return;
        }
        LocalProtocol.validateRegistryName(registryName);
    }

    public static Stream<Arguments> testValidateRegistryNameParams() {
        return Stream.of(
                Arguments.of("", new IllegalArgumentException("Registry name must be a non-null, non-zero length String.")),
                Arguments.of(
                        new RepeatCharSequence('a', 256).toString(),
                        new IllegalArgumentException("Registry name must be less than 255 characters in length.")),
                Arguments.of("namespace", null)
        );
    }
}
