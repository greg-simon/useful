package au.id.simo.useful.io;

/**
 * Implemented by classes that hash the data passing through them.
 * <p>
 * Provides a consistent means of accessing the hash from implementing classes.
 *
 * @see HashInputStream
 * @see HashOutputStream
 */
public interface Hasher {

    byte[] getHash();

    default String getHashString() {
        byte[] hash = getHash();
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b: hash) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}