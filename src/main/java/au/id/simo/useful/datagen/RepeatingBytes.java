package au.id.simo.useful.datagen;

public class RepeatingBytes {
    private final byte[] bytes;
    private final WrapIndex wrapIndex;

    public RepeatingBytes(byte[] bytes) {
        this.bytes = bytes;
        this.wrapIndex = new WrapIndex(bytes.length - 1);
    }

    public byte next() {
        return bytes[wrapIndex.getAndAdd(1)];
    }

    public byte byteAt(int index) {
        return bytes[wrapIndex.calcValue(index)];
    }
}
