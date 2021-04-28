/**
 * Grab bag of helpful utilities.
 * <p>
 * Notable items:
 * <ul>
 * <li>
 * Ring Buffers: For bytes ({@link ByteRingBuffer}), chars
 * ({@link CharRingBuffer}) and objects ({@link RingBuffer}). These are useful
 * for endless writes where you only care about the most recently written.
 * <li>
 * Variable Expanders: For expanding variables in POSIX shell format
 * ({@link EnvVarExpander}) and systemd service file format
 * ({@link PercentVarExpander}). These are useful for expanding variables in a
 * String.
 * <li> {@link CmdTokenizer}: Useful for breaking up a line of shell commands
 * into usable tokens.
 * <li> {@link Cleanup}: A centralised, statically accessible list of code to
 * run to help clean up resources.
 * </ul>
 */
package au.id.simo.useful;
