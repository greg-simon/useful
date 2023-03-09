package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Caches the data read from the provided resource to speed up subsequent reads.
 * <p>
 * Cache size is not allocated up front. If the cache size is exceeded on the
 * first read then caching is disabled and will not be used for subsequent reads.
 * Instead, the underlying Resource will be read again.
 */
public class CachedResource extends FilterResource {

    private final int maxCacheSize;
    
    private byte[] buffer;
    private boolean limitExceeded;
    private long cacheHits;
    private long cacheMisses;

    public CachedResource(Resource resource) {
        this(resource, Integer.MAX_VALUE - 8);
    }

    public CachedResource(Resource resource, int maxCacheSize) {
        super(resource);
        this.maxCacheSize = maxCacheSize;
        limitExceeded = false;
    }
    
    public long cacheHits() {
        return cacheHits;
    }
    
    public long cacheMisses() {
        return cacheMisses;
    }
    
    public void resetStats() {
        cacheHits = 0;
        cacheMisses = 0;
    }

    @Override
    public InputStream inputStream() throws IOException {
        if (limitExceeded) {
            cacheMisses++;
            return super.inputStream();
        }
        if (buffer == null) {
            cacheMisses++;
            InputStream in = super.inputStream();
            RecorderInputStream rin = new RecorderInputStream(in, maxCacheSize);
            rin.onEndStream(bytes -> {
                limitExceeded = rin.isExceededBuffer();
                if (!limitExceeded) {
                    buffer = bytes;
                }
            });
            return rin;
        }
        cacheHits++;
        return new ByteArrayInputStream(buffer);
    }

    public void clearCache() {
        buffer = null;
    }
}
