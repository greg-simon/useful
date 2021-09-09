package au.id.simo.useful.io;

import java.util.ArrayList;

/**
 * An array like class that is backed by multiple arrays much like disk
 * segments.
 * <p>
 * Best used when adding a significantly large number of bytes without knowing
 * the size before hand, yet also only allocating close to the memory required.
 */
public class SegmentByteBundle implements ByteBundle {

    /**
     * 250KB
     */
    protected static final int DEFAULT_SEGMENT_SIZE = 256_000;

    private final ArrayList<byte[]> segmentList;
    private final int segmentSize;
    private final int maxCapacity;

    private int size;

    public SegmentByteBundle() {
        this(DEFAULT_SEGMENT_SIZE, MAX_ARRAY_SIZE);
    }

    public SegmentByteBundle(int segmentSize) {
        this(segmentSize, MAX_ARRAY_SIZE);
    }
    
    public SegmentByteBundle(int segmentSize, int maxCapacity) {
        this.segmentList = new ArrayList<>();
        this.segmentSize = segmentSize;
        this.maxCapacity = maxCapacity;
    }
    
    public int getSegmentSize() {
        return segmentSize;
    }
    
    public int getSegmentCount() {
        return segmentList.size();
    }

    protected int getSegmentId(int index) {
        return index / segmentSize;
    }

    protected int getSegmentIndex(int index) {
        return index % segmentSize;
    }

    protected byte[] getSegment(int index) {
        int segmentId = getSegmentId(index);
        return getOrCreateSegment(segmentId);
    }

    protected byte[] getOrCreateSegment(int segmentId) {
        byte[] segment;
        if (segmentId < segmentList.size()) {
            segment = segmentList.get(segmentId);
            if (segment == null) {
                segment = new byte[segmentSize];
                segmentList.set(segmentId, segment);
            }
            return segment;
        }
        segmentList.ensureCapacity(segmentId);
        int expandBy = segmentId - segmentList.size();
        for (int i = 1; i < expandBy; i++) {
            segmentList.add(null);
        }
        segment = new byte[segmentSize];
        segmentList.add(segment);
        return segment;
    }

    @Override
    public int capacity() {
        return Math.min(segmentList.size() * segmentSize, maxCapacity);
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }
    
    @Override
    public int size() {
        return size;
    }

    @Override
    public void copyIn(int pos, byte[] src, int srcPos, int length) {
        ByteBundle.checkCopyArgs(pos, maxCapacity(), src, srcPos, length);
        int bytesRemaining = length;
        int currentPos = pos;
        while (bytesRemaining > 0) {
            int srcIdx = srcPos + (length - bytesRemaining);
            byte[] seg = getSegment(currentPos);
            int segIdx = getSegmentIndex(currentPos);
            int copyLength = Math.min(seg.length - segIdx, bytesRemaining);
            System.arraycopy(src, srcIdx, seg, segIdx, copyLength);
            bytesRemaining -= copyLength;
            currentPos += copyLength;
            size = Math.max(size, currentPos);
        }
    }

    @Override
    public int copyOut(int pos, byte[] dest, int destPos, int length) {
        ByteBundle.checkCopyArgs(pos, capacity(), dest, destPos, length);
        int bytesRemaining = length;
        int currentPos = pos;
        while (bytesRemaining > 0) {
            int segIdx = getSegmentIndex(currentPos);
            byte[] seg = getSegment(currentPos);
            int currDestPos = destPos + (length - bytesRemaining);
            int copyLength = Math.min(seg.length - segIdx, bytesRemaining);
            System.arraycopy(seg, segIdx, dest, currDestPos, copyLength);
            bytesRemaining -= copyLength;
            currentPos += copyLength;
        }
        return length;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public void trim() {
        int segId = getSegmentId(size);

        // if there is any bytes in the last segment, then dont trim it
        // remove the next one instead.
        if (getSegmentIndex(size) > 0) {
            segId++;
        }

        for (int i = segmentList.size() - 1; i >= segId; i--) {
            segmentList.remove(i);
        }
    }
}
