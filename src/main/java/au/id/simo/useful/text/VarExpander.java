package au.id.simo.useful.text;

/**
 *
 */
public interface VarExpander {

    /**
     * Adds variable names and values.
     *
     * @param varName The name of the variable to replace
     * @param varValue The value of the variable to replace the name with
     * @return the same VarExpander instance to enable method chaining
     */
    VarExpander put(String varName, String varValue);

    /**
     * @param sourceStr String containing unexpanded variables
     * @return String containing resolved and expanded variables.
     */
    String expand(String sourceStr);

    /**
     * Is the expected next character actually next in the given String.
     *
     * @param expected Test that the next character is this one.
     * @param sourceStr the String to run the test on.
     * @param currentIndex The index of the String to test from.
     * @return true if the character at index + 1 is the same as the character
     * provided as first argument. Otherwise false is returned.
     */
    default boolean isNextChar(char expected, String sourceStr, int currentIndex) {
        if (currentIndex >= sourceStr.length()) {
            return false;
        }

        return expected == sourceStr.charAt(currentIndex + 1);
    }

    /**
     * Is the character acceptable to use in a variable name.
     *
     * @param c Test if this character is an acceptable character to use in a
     * variable name.
     * @return true if the provided character isn't reserved in some way,
     * otherwise false.
     */
    default boolean isAcceptableVarNameChar(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }
}
