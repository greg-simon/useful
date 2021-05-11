package au.id.simo.useful.text;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface VarExpanderTest {
    
    VarExpander getInstance();

    @ParameterizedTest
    @ValueSource(strings = {"abc$%{}"})
    default void testIsNextChar(String input) {
        VarExpander expander = getInstance();
        char chars[] = input.toCharArray();
        for (int i=0;i<chars.length; i++) {
            char aChar = chars[i];
            int previousIndex = i-1;
            assertTrue(expander.isNextChar(aChar, input, previousIndex));
            
            // check to ensure '.' is not the next char before testing isNextChar
            // otherwise use another character that isn't the next char.
            if (aChar != '.') {
                assertFalse(expander.isNextChar('.', input, previousIndex));
            } else {
                assertFalse(expander.isNextChar('-', input, previousIndex));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENV_VAR", "varname2", "3num_varname", "testing_123", "12345"})
    default void testIsAcceptableVarNameChar_true(String varName) {
        // only defautl implementation is tested for
        VarExpander expander = getInstance();
        for( char c: varName.toCharArray()) {
            assertTrue(expander.isAcceptableVarNameChar(c));
        }
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"!@#", ")*"})
    default void testIsAcceptableVarNameChar_false(String varName) {
        // only defautl implementation is tested for
        VarExpander expander = getInstance();
        for( char c: varName.toCharArray()) {
            assertFalse(expander.isAcceptableVarNameChar(c));
        }
    }
}
