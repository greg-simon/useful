package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A FilterInputStream implementation to build a has of all bytes read through it.
 */
public class HashInputStream extends CountingInputStream {
    
    private final MessageDigest messageDigest;

    /**
     * Uses MD5 algorithm by default.
     * 
     * @param inputStream
     * @throws NoSuchAlgorithmException 
     */
    public HashInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
        super(inputStream);
        MessageDigest md = MessageDigest.getInstance("MD5");
        this.messageDigest = md;
    }

    public HashInputStream(InputStream inputStream, String messageDigestAlgorithmn) throws NoSuchAlgorithmException {
        super(inputStream);
        MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithmn);
        this.messageDigest = md;
    }

    public HashInputStream(InputStream inputStream, MessageDigest messageDigest) {
        super(inputStream);
        this.messageDigest = messageDigest;
    }

    public byte[] getHash() {
        return messageDigest.digest();
    }

    public String getHashString() {
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

    @Override
    public int read() throws IOException {
        int byt = super.read();
        if (byt != -1) {
            messageDigest.update((byte) byt);
        }
        return byt;
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readByteCount = super.read(b, off, len);
        if (readByteCount > 0) {
            messageDigest.update(b, off, readByteCount);
        }

        return readByteCount;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
