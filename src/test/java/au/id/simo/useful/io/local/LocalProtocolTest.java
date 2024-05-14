package au.id.simo.useful.io.local;

import java.util.stream.Stream;

import au.id.simo.useful.text.RepeatCharSequence;
import au.id.simo.useful.text.Text;
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
