package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import au.id.simo.useful.concurrent.NamedThreadFactory;
import au.id.simo.useful.concurrent.ShutdownExecutorHook;

/**
 * When the {@link InputStream} is requested, it runs the {@link Generator} in
 * another thread to create the data on demand, which is then read from the
 * returned {@link InputStream}.
 * <p>
 * This avoids both loading the {@link Generator} content in to memory first,
 * and using temporary files.
 */
public class ConcurrentGeneratorResource extends Resource {

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final ExecutorService EXEC_SERVICE = createExecutorService();
    private final Generator generator;
    private final int bufferSize;

    /**
     * Used to obtain any exception thrown by the Generator.
     */
    private Future<Object> future;

    /**
     * Constructor.
     *
     * @param generator The Generator that will executed when an InputStream is
     * requested
     */
    public ConcurrentGeneratorResource(Generator generator) {
        this(generator, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor.
     *
     * @param generator The Generator that will executed when an InputStream is
     * requested
     * @param bufferSize The number of bytes in size of the buffer between the
     * OutputStream the Generator s writing to and the created InputStream.
     */
    public ConcurrentGeneratorResource(Generator generator, int bufferSize) {
        this.generator = generator;
        this.bufferSize = bufferSize;
    }

    /**
     * Runs the generation of the Generator on another thread.
     * <p>
     * An ExecutorService is used to run the Generator.
     *
     * @return the stream of data generated from the Generator object.
     * @throws IOException if there is an issue in connecting the
     * PipedOutputStream with a PipedInputStream, or if the Generator throws an
     * exception
     * @see Generator#writeTo(java.io.OutputStream)
     * @see ExecutorService
     */
    @Override
    public InputStream inputStream() throws IOException {
        PipedInputStream in = new PipedInputStream(bufferSize);
        PipedOutputStream out = new PipedOutputStream(in);
        Callable<Object> producer = new GeneratorCallable(out);
        future = EXEC_SERVICE.submit(producer);
        return new SourceErrorInputStreamWrapper(in);
    }

    /**
     * Allows the caller to wait for the Generator running in another thread to
     * complete.
     * <p>
     * If the generator thread was never started, this method returns
     * immediately
     *
     * @throws IOException If Generator threw one, otherwise throws
     * IllegalStateException.
     */
    public void waitForGenerator() throws IOException {
        if (future == null) {
            return;
        }

        try {
            future.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException ex) {
            throw new IOException(ex);
        } catch (ExecutionException ex) {
            // As ExecutorServices wraps Callable exceptions
            // potentially several times.
            // Rethrow any real IOExceptions or RuntimeExceptions
            // Wrap others.
            Throwable causedBy = ex;
            while (causedBy instanceof ExecutionException) {
                causedBy = causedBy.getCause();
                if (causedBy == null) {
                    break;
                }
                if (causedBy instanceof ExecutionException) {
                    continue;
                }
                if (causedBy instanceof IOException) {
                    throw (IOException) causedBy;
                }
                if (causedBy instanceof RuntimeException) {
                    throw (RuntimeException) causedBy;
                }
                throw new IllegalStateException(
                        "Exception from code run by ExecutorService. "
                        + causedBy.getMessage(),
                        causedBy);
            }

            // Just throw IllegalStateException
            throw new IllegalStateException(
                    "Exception from code run by ExecutorService. "
                    + ex.getMessage(),
                    ex);
        }
    }

    /**
     * Call method will always return null.
     * <p>
     * Callable interface was used instead of Runnable, because it allows
     * Exceptions to be thrown.
     */
    private class GeneratorCallable implements Callable<Object> {

        private final OutputStream out;

        public GeneratorCallable(OutputStream out) {
            this.out = out;
        }

        @Override
        public Object call() throws Exception {
            try (OutputStream localOS = this.out) {
                generator.writeTo(localOS);
            }
            return null;
        }
    }

    /**
     * Wraps the PipedInputStream to cause any exceptions thrown by the
     * Generator thread, to be thrown when this stream is closed.
     * <p>
     * This ensures any problems in the Generator must be dealt with instead of
     * silently ignored.
     */
    private class SourceErrorInputStreamWrapper extends FilterInputStream {

        public SourceErrorInputStreamWrapper(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            waitForGenerator();
        }
    }

    /**
     * Creates an ExecutorService for all instances of this class to use.
     * <p>
     * A shutdown hook is also registered to shutdown this service when the
     * application is shutdown.
     *
     * @return An ExecutorService shared among all instances of this class for
     * running {@code Generator}'s in.
     */
    private static ExecutorService createExecutorService() {
        ExecutorService newThreadPool = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                5L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new NamedThreadFactory("concurrent-generator-resource", true));
        ShutdownExecutorHook.registerService(newThreadPool);
        return newThreadPool;
    }
}
