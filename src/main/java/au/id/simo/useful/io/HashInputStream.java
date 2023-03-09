package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An InputStream implementation to build a hash of all bytes read through it.
 */
public class HashInputStream extends CountingInputStream implements Hasher {

    private final MessageDigest messageDigest;

    public HashInputStream(InputStream inputStream, String messageDigestAlgorithm) throws NoSuchAlgorithmException {
        super(inputStream);
        this.messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
    }

    public HashInputStream(InputStream inputStream, MessageDigest messageDigest) {
        super(inputStream);
        this.messageDigest = messageDigest;
    }

    @Override
    public byte[] getHash() {
        return messageDigest.digest();
    }

    @Override
    public int read() throws IOException {
        int byt = super.read();
        if (byt != -1) {
            messageDigest.update((byte) byt);
        }
        return byt;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readByteCount = super.read(b, off, len);
        if (readByteCount > 0) {
            messageDigest.update(b, off, readByteCount);
        }
        return readByteCount;
    }
}
