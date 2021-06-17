package au.id.simo.useful;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import au.id.simo.useful.io.CloseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CleanerTest {

    /**
     * Uses reflection to test awkward to access static variables.
     * <p>
     * Choice was made to use reflection over providing protected methods in the
     * Cleaner class.
     *
     * @throws Exception if there is any issues in using reflection.
     */
    @Test
    public void testOnShutdown() throws Exception {
        Cleaner cleaner = Cleaner.onShutdown();
        assertSame(
                cleaner,
                Cleaner.onShutdown(),
                "Verify multiple calls to onShutdown returns the same instance"
        );
        
        // obtain the shutdown thread instance via reflection to verify it had
        // been registered
        Class<Cleaner> cc = Cleaner.class;
        Field staticCleanerThread = cc.getDeclaredField("onShutdownThread");
        staticCleanerThread.setAccessible(true);
        // null because it's a static field requiring no instance to access.
        Thread thread = (Thread) staticCleanerThread.get(null);
        assertNotNull(thread);
        
        // Short of creating and shutting down a JVM, this is the only test I
        // can use to verify the Cleaner was registered.
        // This will also break the onDemand functionallity as the Cleaner will
        // not register itself again for the life of this JVM.
        assertTrue(Runtime.getRuntime().removeShutdownHook(thread));
        
        // This will repair the breakage by resetting the state of the shutdown
        // cleaner instance to null. Causing a new one to be created and
        // registered next call to onShutdown().
        Field onShutdownInstance = cc.getDeclaredField("onShutdownInstance");
        onShutdownInstance.setAccessible(true);
        onShutdownInstance.set(null, null);
        staticCleanerThread.set(null, null);
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
    public void testCleanWithExceptions() {
        CountRunnable countRun = new CountRunnable();
        
        Cleaner cleaner = new Cleaner();
        cleaner.runOnClean(countRun);
        cleaner.closeOnClean(() -> {throw new RuntimeException();});
        cleaner.runOnClean(countRun);
        cleaner.shutdownOnClean(new MockExecutorService());
        cleaner.runOnClean(countRun);
        cleaner.runOnClean(() -> {throw new RuntimeException();});
        cleaner.runOnClean(countRun);
        
        assertEquals(7, cleaner.size());
        cleaner.clean();
        assertEquals(0, cleaner.size());
        assertEquals(4, countRun.runCount(), "Verify countRun ran before and after each exception throwing task");
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
    public void testClose() {
        Cleaner cleaner = new Cleaner();
        CountRunnable countRun = cleaner.runOnClean(new CountRunnable());
        
        assertEquals(1, cleaner.size());
        cleaner.close();
        assertEquals(0, cleaner.size());
        assertEquals(1, countRun.runCount());
    }
    
    @Test
    public void testRunOnCleanNull() {
        Cleaner cleaner = new Cleaner();
        Runnable runnable = cleaner.runOnClean(null);
        assertNull(runnable);
        assertEquals(0, cleaner.size());
    }
    
    @Test
    public void testRunOnCleanSelf() {
        Cleaner cleaner = new Cleaner();
        assertThrows(IllegalArgumentException.class, ()-> {
            cleaner.runOnClean(cleaner);
        });
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
    
    @Test
    public void testCloseOnCleanSelf() {
        Cleaner cleaner = new Cleaner();
        assertThrows(IllegalArgumentException.class, ()-> {
            cleaner.closeOnClean(cleaner);
        });
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
    
    /**
     * Will throw {@link UnsupportedOperationException} if any method is called.
     */
    public class MockExecutorService implements ExecutorService {

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<?> submit(Runnable task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable command) {
            throw new UnsupportedOperationException();
        }
        
    }
}
