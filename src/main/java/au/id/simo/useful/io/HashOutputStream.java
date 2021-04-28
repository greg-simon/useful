package au.id.simo.useful.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An OutputStream implementation to build a hash of all bytes written to it.
 */
public class HashOutputStream extends CountingOutputStream implements Hasher {

    private final MessageDigest messageDigest;

    public HashOutputStream(OutputStream out, String messageDigestAlgorithmn) throws NoSuchAlgorithmException {
        super(out);
        this.messageDigest = MessageDigest.getInstance(messageDigestAlgorithmn);
    }

    public HashOutputStream(OutputStream out, MessageDigest messageDigest) {
        super(out);
        this.messageDigest = messageDigest;
    }

    @Override
    public byte[] getHash() {
        return messageDigest.digest();
    }

    @Override
    public void write(int b) throws IOException {
        messageDigest.update((byte) b);
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        messageDigest.update(b, off, len);
        super.write(b, off, len);
    }
}
