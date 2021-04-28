package au.id.simo.useful;

/**
 *
 */
public interface VarExpander {

    /**
     * Adds variable names and values.
     *
     * @param varName
     * @param varValue
     * @return
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
     * @param expected
     * @param sourceStr
     * @param currentIndex
     * @return
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
     * @param c
     * @return
     */
    default boolean isAcceptableVarNameChar(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }
}
