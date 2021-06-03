/**
 * Grab bag of helpful utilities.
 * <p>
 * Notable items:
 * <ul>
 * <li>
 * Ring Buffers: For bytes ({@link ByteRingBuffer}), chars
 * ({@link CharRingBuffer}) and objects ({@link SimpleRingBuffer}). These are useful
 * for endless writes where you only care about the most recently written.
 * <li> {@link Cleaner}: A centralised, statically accessible list of code to
 * run to help clean up resources.
 * </ul>
 */
package au.id.simo.useful;
