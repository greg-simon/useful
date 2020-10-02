package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A filter like InputStream to build a has of all bytes read through it.
 */
public class HashInputStream extends InputStream {

    private final InputStream inputStream;
    private final MessageDigest messageDigest;
    private long bytesRead;

    /**
     * Uses MD5 algorithm by default.
     * 
     * @param inputStream
     * @throws NoSuchAlgorithmException 
     */
    public HashInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        this.inputStream = inputStream;
        this.messageDigest = md;
    }

    public HashInputStream(InputStream inputStream, String messageDigestAlgorithmn) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithmn);

        this.inputStream = inputStream;
        this.messageDigest = md;
    }

    public HashInputStream(InputStream inputStream, MessageDigest messageDigest) {
        this.inputStream = inputStream;
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
                sb.append(Integer.toHexString((0xFF & hash[i])));
            } else {
                sb.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return sb.toString();
    }

    @Override
    public int read() throws IOException {
        int byt = inputStream.read();
        if (byt != -1) {
            bytesRead++;
            messageDigest.update((byte) byt);
        }
        return byt;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readByteCount = inputStream.read(b, off, len);
        if (readByteCount > 0) {
            bytesRead += readByteCount;
            messageDigest.update(b, off, readByteCount);
        }

        return readByteCount;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int readByteCount = inputStream.read(b);
        if (readByteCount > 0) {
            bytesRead += readByteCount;
            messageDigest.update(b, 0, readByteCount);
        }

        return readByteCount;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
