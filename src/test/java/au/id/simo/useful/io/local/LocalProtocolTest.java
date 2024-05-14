package au.id.simo.useful.io.local;

import java.util.stream.Stream;

import au.id.simo.useful.text.Text;
import org.junit.jupiter.api.Assertions;
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
    public void testNamespaceOrNull(String namespace, String expectedNamespace) {
        if (expectedNamespace == null) {
            assertNull(LocalProtocol.namespaceOrNull(namespace));
        } else {
            assertEquals(expectedNamespace, LocalProtocol.namespaceOrNull(namespace));
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
    @MethodSource("testSessionIdOrNullParams")
    public void testSessionIdOrNull(String hostname, String expectedSessionId) {
        Assertions.assertEquals(
                expectedSessionId,
                LocalProtocol.sessionIdOrNull(hostname)
        );
    }
    public static Stream<Arguments> testSessionIdOrNullParams() {
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

    @ParameterizedTest
    @MethodSource("testValidateRegistryNameParams")
    public void testValidateRegistryName(String registryName, Integer maxSessionIDLength, IllegalArgumentException expectedException) {
        if (expectedException != null) {
            Exception e = assertThrows(expectedException.getClass(), () -> LocalProtocol.validateRegistryName(registryName, maxSessionIDLength));
            assertEquals(expectedException.getMessage(), e.getMessage());
            return;
        }
        LocalProtocol.validateRegistryName(registryName, maxSessionIDLength);
    }

    public static Stream<Arguments> testValidateRegistryNameParams() {
        return Stream.of(
                Arguments.of("", 0,new IllegalArgumentException("Registry name must be a non-null, non-zero length String.")),
                Arguments.of(
                        Text.repeat('a',256),
                        0,
                        new IllegalArgumentException("Registry name and maximum session ID length must be less than 255 characters in total: name=256, sessionIDLength=0")),
                Arguments.of(
                        Text.repeat('a',200),
                        56,
                        new IllegalArgumentException("Registry name and maximum session ID length must be less than 255 characters in total: name=200, sessionIDLength=56")),
                Arguments.of(
                        Text.repeat('b',200),
                        55,
                        null),
                Arguments.of("namespace", 0, null)
            );
    }
}
