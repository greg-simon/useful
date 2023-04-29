package au.id.simo.useful;

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

import au.id.simo.useful.io.Latch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class DeferTest {

    /**
     *
     */
    @Test
    public void testRegisterOnShutdown() {
        Defer defer = new Defer();
        assertFalse(defer.isShutdownHookRegistered());
        
        assertSame(defer, defer.registerShutdownHook());
        assertTrue(defer.isShutdownHookRegistered());
        // register again to test multiple calls.
        assertSame(defer, defer.registerShutdownHook());
        assertTrue(defer.isShutdownHookRegistered());
        
        assertSame(defer, defer.unregisterShutdownHook());
        assertFalse(defer.isShutdownHookRegistered());
        // unregister again to test multiple calls
        assertSame(defer, defer.unregisterShutdownHook());
        assertFalse(defer.isShutdownHookRegistered());
    }

    @Test
    public void testSetErrorHandler() {
        CountErrorHandler errorHandler = new CountErrorHandler();
        Defer defer = new Defer();
        defer.setErrorHandler(errorHandler);
        assertEquals(0, errorHandler.getClosableCount());
        assertEquals(0, defer.size());

        defer.close(() -> {
            throw new RuntimeException();
        });
        defer.shutdown(new MockExecutorService());
        
        defer.close();
        assertEquals(2, errorHandler.getClosableCount());
        assertEquals(0, defer.size());
    }

    @Test
    public void testCloseWithExceptions() {
        CountCloseable countCloseable = new CountCloseable();
        
        Defer defer = new Defer();
        defer.close(countCloseable);
        defer.close(() -> {throw new RuntimeException();});
        defer.close(countCloseable);
        defer.shutdown(new MockExecutorService());
        defer.close(countCloseable);
        defer.close(() -> {throw new RuntimeException();});
        defer.close(countCloseable);
        
        assertEquals(7, defer.size());
        defer.close();
        assertEquals(0, defer.size());
        assertEquals(4, countCloseable.getCount(), "Verify countRun ran before and after each exception throwing task");
    }

    @Test
    public void testShutdown() {
        Defer defer = new Defer();
        ExecutorService service = defer.shutdown(Executors.newCachedThreadPool());
        assertEquals(1, defer.size());
        assertFalse(service.isShutdown());
        defer.close();
        assertEquals(0, defer.size());
        assertTrue(service.isShutdown());
    }
    
    @Test
    public void testShutdownNull() {
        Defer defer = new Defer();
        ExecutorService service = defer.shutdown(null);
        assertNull(service);
        assertEquals(0, defer.size());
    }

    @Test
    public void testClose() {
        Defer defer = new Defer();
        Latch latch = defer.close(new Latch());
        assertEquals(1, defer.size());
        assertFalse(latch.isClosed());
        defer.close();
        assertEquals(0, defer.size());
        assertTrue(latch.isClosed());
    }
    
    @Test
    public void testCloseNull() {
        Defer defer = new Defer();
        AutoCloseable closable = defer.close(null);
        assertNull(closable);
        assertEquals(0, defer.size());
    }
    
    @Test
    public void testCloseSelf() {
        Defer defer = new Defer();
        assertThrows(IllegalArgumentException.class, ()-> {
            defer.close(defer);
        });
    }
    
    public class CountCloseable implements AutoCloseable {
        private final AtomicInteger count = new AtomicInteger();
        
        @Override
        public void close() {
            count.incrementAndGet();
        }
        
        public int getCount() {
            return count.get();
        }
    }

    public class CountErrorHandler implements DeferErrorHandler {
        private final AtomicInteger closableCount = new AtomicInteger();

        @Override
        public void handle(AutoCloseable closable, Exception exception) {
            closableCount.incrementAndGet();
        }

        public int getClosableCount() {
            return closableCount.get();
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
