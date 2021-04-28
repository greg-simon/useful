package au.id.simo.useful.experimental;

import java.util.ArrayList;

/**
 * A collection of arrays that can be used like a single array.
 * <p>
 * To be used in where arrays are often grown such as buffers and caches. It is
 * cheap to extend and won't copy any data internally. It can also hold more
 * than the usual limit of 2^32 items due to using long for it's index type.
 * 
 * Current implementation is simplistic and not at all optimised.
 * 
 * @param <C> the class of objects contained.
 */
public class ArrayBag<C> {
    private final ArrayList<ArrayWrapper> arrayList;
    
    private long totalSize;
    
    public ArrayBag() {
        this.arrayList = new ArrayList<>();
    }

    public C get(long index) {
        for (ArrayWrapper wrap: arrayList) {
            if (wrap.indexStart <= index && (wrap.indexStart + wrap.array.length) > index) {
                return wrap.array[(int)(index - wrap.indexStart)];
            }
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Index out of range: %d for length: %d", index, totalSize));
    }
    
    public void copyTo(long indexStart, C[] destArray, int destOffset, int length) {
        for (int i = destOffset; i < destOffset + length; i++) {
            destArray[i] = get(indexStart + i);
        }
    }
    
    /**
     *
     * @param array Add this array to the bag.
     */
    @SuppressWarnings("unchecked")
    public void add(C... array) {
        // don't bother with zero length arrays
        if (array.length == 0) {
            return;
        }
        
        arrayList.add(new ArrayWrapper(totalSize, array));
        totalSize += array.length;
    }

    public long size() {
        return totalSize;
    }
    
    private class ArrayWrapper {
        protected final long indexStart;
        protected final C array[];

        public ArrayWrapper(long indexStart, C[] array) {
            this.indexStart = indexStart;
            this.array = array;
        }
    }
}
