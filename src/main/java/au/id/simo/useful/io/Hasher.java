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
            if ((0xff & hash[i]) < 0x10) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(0xFF & hash[i]));
        }
        return sb.toString();
    }
}
