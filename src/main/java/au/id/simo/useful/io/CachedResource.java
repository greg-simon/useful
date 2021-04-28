package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Caches the data read from the provided resource to speed up subsequent reads.
 * <p>
 * Cache size is not allocated up front. If the cache size is exceeded on the
 * first read then caching is disabled and will not used for subsequent reads.
 * Instead the underlying Resource will be read again.
 */
public class CachedResource extends Resource {

    private final Resource resource;
    private final int maxCacheSize;
    private byte[] buffer;
    private boolean limitExceeded;

    public CachedResource(Resource resource) {
        this(resource, Integer.MAX_VALUE - 8);
    }

    public CachedResource(Resource resource, int maxCacheSize) {
        this.resource = resource;
        this.maxCacheSize = maxCacheSize;
        limitExceeded = false;
    }

    @Override
    public InputStream inputStream() throws IOException {
        if (limitExceeded) {
            return resource.inputStream();
        }
        if (buffer == null) {
            InputStream in = resource.inputStream();
            RecorderInputStream rin = new RecorderInputStream(in, maxCacheSize) {
                @Override
                public void endStream() {
                    limitExceeded = this.isExceededBuffer();
                    if (limitExceeded == false) {
                        buffer = this.getReadByteArray();
                    }
                }
            };
            return rin;
        }
        return new ByteArrayInputStream(buffer);
    }

    public void clearCache() {
        buffer = null;
    }
}
