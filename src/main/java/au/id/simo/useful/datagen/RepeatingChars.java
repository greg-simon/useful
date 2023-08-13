package au.id.simo.useful.datagen;

import au.id.simo.useful.text.LoopCharSequence;

public class RepeatingChars {
    private final CharSequence sequence;
    private final WrapIndex seqIndex;

    public RepeatingChars(CharSequence sequence) {
        this.sequence = sequence;
        this.seqIndex = new WrapIndex(sequence.length());
    }

    public RepeatingChars(CharSequence sequence, int fromIndex, int toIndex) {
        this.sequence = sequence;
        this.seqIndex = new WrapIndex(fromIndex, toIndex);
    }

    private RepeatingChars(CharSequence sequence, WrapIndex index) {
        this.sequence = sequence;
        this.seqIndex = index;
    }

    public char next() {
        return sequence.charAt(seqIndex.next());
    }

    public char prev() {
        return sequence.charAt(seqIndex.previous());
    }

    public char charAt(int index) {
        return sequence.charAt(seqIndex.calcValue(index));
    }

    public CharSequence next(int charCount) {
        int currentIndex = seqIndex.value();
        return new LoopCharSequence(this, currentIndex, currentIndex + charCount);
    }

    public RepeatingChars copy() {
        WrapIndex idx = new WrapIndex(sequence.length());
        idx.setValue(this.seqIndex.value());
        return new RepeatingChars(sequence, idx);
    }
}
