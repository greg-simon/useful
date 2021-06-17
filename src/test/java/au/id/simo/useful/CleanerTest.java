package au.id.simo.useful;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import au.id.simo.useful.io.CloseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CleanerTest {

    @Test
    public void testOnShutdown() {
    }

    @Test
    public void testOnDemand() {
        Cleaner cleaner = Cleaner.onDemand();
        assertNotNull(cleaner);
        assertEquals(0, cleaner.size());
        assertTrue(
            cleaner == Cleaner.onDemand(),
            "Verify same instance is provided by the second onDemand() call"
        );
    }

    @Test
    public void testOnDemandClean() {
        Cleaner cleaner = Cleaner.onDemand();
        assertEquals(0, cleaner.size());

        CountRunnable countRunnable = new CountRunnable();
        cleaner.runOnClean(countRunnable);
        assertEquals(1, cleaner.size(), "Verify runnable was added");
        assertEquals(0, countRunnable.runCount(), "Verify countRunnable has not been run");

        Cleaner.onDemandClean();
        assertEquals(0, cleaner.size(), "Verify no remaining tasks to cleanup");
        assertEquals(1, countRunnable.runCount(), "Verify countRunnable has been run once");
    }

    @Test
    public void testSetErrorHandler() {
        CountErrorHandler errorHandler = new CountErrorHandler();
        Cleaner cleaner = new Cleaner();
        cleaner.setErrorHandler(errorHandler);
        assertEquals(0, errorHandler.getTotalCount());
        assertEquals(0, cleaner.size());
        
        cleaner.runOnClean(() -> {
            throw new RuntimeException();
        });
        cleaner.closeOnClean(() -> {
            throw new RuntimeException();
        });
        
        cleaner.clean();
        assertEquals(1, errorHandler.getRunnableCount());
        assertEquals(1, errorHandler.getClosableCount());
        assertEquals(2, errorHandler.getTotalCount());
        assertEquals(0, cleaner.size());
    }

    @Test
    public void testClean() {
        CountRunnable countRun = new CountRunnable();
        Cleaner cleaner = new Cleaner();
        cleaner.runOnClean(countRun);
        
        assertEquals(1, cleaner.size());
        cleaner.clean();
        assertEquals(0, cleaner.size());
        assertEquals(1, countRun.runCount());
    }
    
    @Test
    public void testRun() {
        Cleaner cleaner = new Cleaner();
        CountRunnable countRun = cleaner.runOnClean(new CountRunnable());
        
        assertEquals(1, cleaner.size());
        cleaner.run();
        assertEquals(0, cleaner.size());
        assertEquals(1, countRun.runCount());
    }

    @Test
    public void testClose_0args() {
        Cleaner cleaner = new Cleaner();
        CountRunnable countRun = cleaner.runOnClean(new CountRunnable());
        
        assertEquals(1, cleaner.size());
        cleaner.close();
        assertEquals(0, cleaner.size());
        assertEquals(1, countRun.runCount());
    }
    
    @Test
    public void testExecNull() {
        Cleaner cleaner = new Cleaner();
        Runnable runnable = cleaner.runOnClean(null);
        assertNull(runnable);
        assertEquals(0, cleaner.size());
    }

    @Test
    public void testShutdownOnClean() {
        Cleaner cleaner = new Cleaner();
        ExecutorService service = cleaner.shutdownOnClean(Executors.newCachedThreadPool());
        assertEquals(1, cleaner.size());
        assertFalse(service.isShutdown());
        cleaner.clean();
        assertEquals(0, cleaner.size());
        assertTrue(service.isShutdown());
    }
    
    @Test
    public void testShutdownNull() {
        Cleaner cleaner = new Cleaner();
        ExecutorService service = cleaner.shutdownOnClean(null);
        assertNull(service);
        assertEquals(0, cleaner.size());
    }

    @Test
    public void testCloseOnClean() {
        Cleaner cleaner = new Cleaner();
        CloseStatus closeStatus = cleaner.closeOnClean(new CloseStatus());
        assertEquals(1, cleaner.size());
        assertFalse(closeStatus.isClosed());
        cleaner.clean();
        assertEquals(0, cleaner.size());
        assertTrue(closeStatus.isClosed());
    }
    
    @Test
    public void testCloseOnCleanNull() {
        Cleaner cleaner = new Cleaner();
        AutoCloseable closable = cleaner.closeOnClean(null);
        assertNull(closable);
        assertEquals(0, cleaner.size());
    }
    
    public class CountRunnable implements Runnable {
        private final AtomicInteger runCount = new AtomicInteger();
        
        @Override
        public void run() {
            runCount.incrementAndGet();
        }
        
        public int runCount() {
            return runCount.get();
        }
    }
    
    public class CountErrorHandler implements CleanerErrorHandler {
        private final AtomicInteger runnableCount = new AtomicInteger();
        private final AtomicInteger closableCount = new AtomicInteger();
        
        @Override
        public void handle(Runnable runnable, Throwable throwable) {
            runnableCount.incrementAndGet();
        }

        @Override
        public void handle(AutoCloseable closable, Throwable throwable) {
            closableCount.incrementAndGet();
        }

        public int getRunnableCount() {
            return runnableCount.get();
        }

        public int getClosableCount() {
            return closableCount.get();
        }
        
        public int getTotalCount() {
            return getRunnableCount() + getClosableCount();
        }
    }
}
