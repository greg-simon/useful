package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO: What happens when cache hit and miss counters overflow?
 */
public class CachedResourceTest implements ResourceTest {

    private static final byte[] TEST_DATA = new byte[]{
        1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20
    };
    
    @Override
    public CachedResource createResource(byte[] testData, Charset charset) throws IOException {
        return new CachedResource(new ByteArrayResource(testData));
    }

    @Test
    public void testCacheUsage() throws Exception {
        CachedResource cRes = createResource(TEST_DATA, null);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        
        assertEquals(1, cRes.cacheHits());
        assertEquals(1, cRes.cacheMisses());
    }
    
    @Test
    public void testClearCache() throws Exception {
        CachedResource cRes = createResource(TEST_DATA, null);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, cRes.cacheMisses());
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, cRes.cacheMisses());
        
        cRes.clearCache();
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(2, cRes.cacheMisses());
    }
    
    @Test
    public void testCacheSizeExceeded() throws Exception {
        CachedResource cRes = new CachedResource(new ByteArrayResource(TEST_DATA), 10);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, cRes.cacheMisses());
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(2, cRes.cacheMisses());
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(3, cRes.cacheMisses());
    }
    
    @Test
    public void testCacheSizeZero() throws Exception {
        CachedResource cRes = new CachedResource(new ByteArrayResource(TEST_DATA), 0);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, cRes.cacheMisses());
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(2, cRes.cacheMisses());
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(3, cRes.cacheMisses());
        
        assertEquals(0, cRes.cacheHits());
    }
}
