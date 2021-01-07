package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CachedResourceTest {

    @Test
    public void testCacheUsage() throws Exception {
        ReadCountResource rcr = new ReadCountResource();
        CachedResource cRes = new CachedResource(rcr);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        
        assertEquals(1, rcr.counter);
    }
    
    @Test
    public void testClearCache() throws Exception {
        ReadCountResource rcr = new ReadCountResource();
        CachedResource cRes = new CachedResource(rcr);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, rcr.counter);
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, rcr.counter);
        
        cRes.clearCache();
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(2, rcr.counter);
    }
    
    @Test
    public void testCacheSizeExceeded() throws Exception {
        ReadCountResource rcr = new ReadCountResource();
        CachedResource cRes = new CachedResource(rcr, 10);
        InputStream in;
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(1, rcr.counter);
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(2, rcr.counter);
        
        in = cRes.inputStream();
        while(in.read()!=-1){};
        assertEquals(3, rcr.counter);
    }
    
    private class ReadCountResource extends Resource {
        int counter=0;
        @Override
        public InputStream inputStream() throws IOException {
            counter++;
            return new ByteArrayInputStream(new byte[]{
                1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20
            });
        }
    }
}
