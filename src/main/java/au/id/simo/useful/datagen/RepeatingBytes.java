package au.id.simo.useful.datagen;

public class RepeatingBytes {
    private final byte[] bytes;
    private final WrapIndex wrapIndex;

    public RepeatingBytes(byte[] bytes) {
        this.bytes = bytes;
        this.wrapIndex = new WrapIndex(bytes.length);
    }

    public byte next() {
        return bytes[wrapIndex.next()];
    }

    public byte byteAt(int index) {
        return bytes[wrapIndex.calcValue(index)];
    }
}
