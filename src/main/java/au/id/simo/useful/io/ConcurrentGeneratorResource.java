package au.id.simo.useful.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * When the {@link InputStream} is requested, it runs the {@link Generator} in
 * another thread to create the data on demand, which is then read from the
 * returned {@link InputStream}.
 * <p>
 * This avoids both loading the {@link Generator} content in to memory first,
 * and using temporary files.
 * <p>
 * {@link Executors#newCachedThreadPool() } is the default executor service used
 * for generating resources when no ExecutorService is passed in a constructor.
 */
public class ConcurrentGeneratorResource implements Resource {

    /**
     * Size of the buffer used by PipedInputStream/PipedOutputStream in
     * communicating between threads.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * The ExecutorService used by Generator to produce the resource data, if
     * one isn't specified in a constructor.
     */
    private static ExecutorService defaultExecutorService = Executors.newCachedThreadPool();

    /**
     * Replaces the default ExecutorService used to run the Generators.
     * <p>
     * It is recommended that the returned ExecutorService be shutdown by the
     * calling code.
     *
     * @param service The new ExecutorService.
     * @return The existing ExecutorService so the caller can shut it down
     * cleanly.
     */
    public static synchronized ExecutorService setDefaultExecutorService(ExecutorService service) {
        ExecutorService old = defaultExecutorService;
        defaultExecutorService = service;
        return old;
    }

    private final ExecutorService service;
    private final Generator generator;
    private final int bufferSize;

    /**
     * Constructor.
     *
     * @param generator The Generator that will be executed when an InputStream
     * is requested
     */
    public ConcurrentGeneratorResource(Generator generator) {
        this(defaultExecutorService, generator, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor.
     *
     * @param service The executor service that the Generator will use
     * @param generator The Generator that will be executed when an InputStream
     * is requested
     */
    public ConcurrentGeneratorResource(ExecutorService service, Generator generator) {
        this(service, generator, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor.
     *
     * @param generator The Generator that will be executed when an InputStream
     * is requested
     * @param bufferSize The number of bytes in size of the buffer between the
     * OutputStream the Generator s writing to and the created InputStream.
     */
    public ConcurrentGeneratorResource(Generator generator, int bufferSize) {
        this(defaultExecutorService, generator, bufferSize);
    }

    /**
     * Constructor.
     *
     * @param service The executor service that the Generator will use
     * @param generator The Generator that will be executed when an InputStream
     * is requested
     * @param bufferSize The number of bytes in size of the buffer between the
     * OutputStream the Generator s writing to and the created InputStream.
     */
    public ConcurrentGeneratorResource(ExecutorService service, Generator generator, int bufferSize) {
        this.service = service;
        this.generator = generator;
        this.bufferSize = bufferSize;
    }

    /**
     * Runs the generation of the Generator on another thread.
     * <p>
     * An ExecutorService is used to run the Generator.
     * <p>
     * NOTE: Resource leakage is very likely to occur if the returned
     * InputStream is not closed.
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
        // use a callable so exceptions can be thrown by the generator thread.
        // Runnable.run() doesn't throw Exception.
        Callable<Object> producer = () -> {
            try (OutputStream localOut = out) {
                generator.writeTo(localOut);
            } catch (InterruptedIOException e) {
                // reset interrupt status
                Thread.currentThread().interrupt();
                throw e;
            }
            return null;
        };
        Future<Object> future = service.submit(producer);
        return new ConsumerInputStream(in, future);
    }

    /**
     * Usually called on the consumer thread, it allows the caller to close the
     * Generator running in another thread.
     * <p>
     * If the generator thread was never started, this method returns
     * immediately
     *
     * @param future Represents the Generator running in another thread. Cannot
     * be null.
     * @throws IOException If Generator threw any kind of Exception. Any
     * Throwable that is not an Exception, such as OutOfMemoryError is not
     * caught or wrapped in an IOException.
     */
    protected static void closeGenerator(Future<Object> future) throws IOException {
        try {
            // throws any exceptions in the consumer thread that were thrown
            // in the generator thread.
            future.get(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            // this only occurs when the consumer thread is interrupted while
            // waiting <1ms for the future to complete. It's a very small window
            // but still possible.
            
            // ensure interrupt flag is reset, but otherwise ignore.
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            // This occurs when the consumer InputStream is closed before the
            // Generator is finished.
            // No need to throw an exception on the consumer thread. Just
            // interrupt the generator thread, as it's no longer being read from.
            future.cancel(true);
        } catch (CancellationException ex) {
            // Producer callable was canceled.
            throw wrapIOE(ex);
        } catch (ExecutionException ex) {
            // This occurs when an exception is thrown in the Generator before
            // the consumer is closed.
            // Because ExecutorServices wraps Callable exceptions, unwrap and
            // rethrow any IOExceptions or RuntimeExceptions, anything else is
            // wrapped in an IOException.
            Throwable causedBy = ex.getCause();
            if(causedBy == null) {
                // this scenario requires a custom ExecutorService to occur, but
                // is still technically possible.
                throw wrapIOE(ex);
            }
            if (causedBy instanceof IOException) {
                throw (IOException) causedBy;
            }
            if (causedBy instanceof RuntimeException) {
                throw (RuntimeException) causedBy;
            }
            throw wrapIOE(causedBy);
        }
    }
    
    private static IOException wrapIOE(Throwable ex) {
        return new IOException(String.format(
            "Exception from Generator: %s",
            ex.getMessage()
        ), ex);
    }

    /**
     * The consumer half of the producer/consumer thread pair.
     * <p>
     * Causes any exceptions thrown by the Generator/Producer thread to be re-thrown
     * when the input stream is closed.
     * <p>
     * This ensures any exceptions thrown in the Generators Thread are exposed to
     * the caller thread that is reading the InputStream.
     */
    protected static class ConsumerInputStream extends FilterInputStream {
        private final Future<Object> generatorFuture;
        
        public ConsumerInputStream(InputStream in, Future<Object> future) {
            super(in);
            this.generatorFuture = future;
        }

        @Override
        public void close() throws IOException {
            closeGenerator(generatorFuture);
            super.close();
        }
        
        protected Future<Object> getFuture() {
            return generatorFuture;
        }
    }
}
