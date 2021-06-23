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
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < hash.length; i++) {
            sb.append(String.format("%02x", hash[i]));
        }
        return sb.toString();
    }
}