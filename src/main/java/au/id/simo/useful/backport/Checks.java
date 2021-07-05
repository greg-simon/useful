package au.id.simo.useful.backport;

import java.util.Objects;

/**
 * Utility class for Java 8, containing select new library features from Java
 * 9+.
 * <p>
 * Check individual methods descriptions for their replacements.
 */
public class Checks {
    
    private Checks() {}
    /**
     * Returns the first argument if it is non-{@code null} and
     * otherwise returns the non-{@code null} second argument.
     * <p>
     * NOTE: Backported from Java 9+ Objects class.
     *
     * @param obj an object
     * @param defaultObj a non-{@code null} object to return if the first argument
     *                   is {@code null}
     * @param <T> the type of the reference
     * @return the first argument if it is non-{@code null} and
     *        otherwise the second argument if it is non-{@code null}
     * @throws NullPointerException if both {@code obj} is null and
     *        {@code defaultObj} is {@code null}
     * @since 9
     */
    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");
    }
}
