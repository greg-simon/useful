package au.id.simo.useful.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import au.id.simo.useful.Cleaner;
import au.id.simo.useful.io.ConcurrentGeneratorResource.ConsumerInputStream;
import au.id.simo.useful.test.ManualExecutorService;
import org.junit.jupiter.api.Test;

import static au.id.simo.useful.io.ConcurrentGeneratorResourceTest.LineGenerator.testLines;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ConcurrentGeneratorResourceTest implements ResourceTest {

    @Override
    public Resource createResource(byte[] testData, Charset charset) throws IOException {
        return new ConcurrentGeneratorResource((OutputStream out) -> {
            out.write(testData);
        });
    }

    @Test
    public void testProducerConsumer() throws Exception {
        int lineCount = 100;
        Generator lineGen = new LineGenerator(lineCount, false);
        ExecutorService service = Executors.newCachedThreadPool();
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(service, lineGen);

        testLines(lineCount, genRes.inputStream());
    }

    @Test
    public void testProducerConsumer_GeneratorIOException() throws Exception {
        int lineCount = 100;
        Generator lineGen = new LineGenerator(lineCount, true);

        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(lineGen, 1);

        assertThrows(IOException.class, () -> {
            testLines(lineCount, genRes.inputStream());
        });
    }
    
    @Test
    public void testProducerConsumer_GeneratorRuntimeException() throws Exception {
        int lineCount = 100;
        Generator gen = (OutputStream out) -> {
            throw new RuntimeException("runtime exception");
        };

        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(gen, 1);

        assertThrows(RuntimeException.class, () -> {
            testLines(lineCount, genRes.inputStream());
        });
    }
    
    @Test
    public void testProducerConsumer_GeneratorOutOfMemoryException() throws Exception {
        int lineCount = 100;
        Generator gen = (OutputStream out) -> {
            throw new OutOfMemoryError("fake oom");
        };

        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(gen, 1);

        // expect OOM to be wrapped in IOException
        IOException ioe = assertThrows(IOException.class, () -> {
            testLines(lineCount, genRes.inputStream());
        });
        assertTrue(ioe.getCause() instanceof OutOfMemoryError);
    }
    
    @Test
    public void testProducerConsumer_GeneratorInteruptedAfterStartException() throws Exception {
        // used to ensure generator has written a byte.
        CountDownLatch cdlatch = new CountDownLatch(1);
        
        Generator gen = (OutputStream out) -> {
            out.write(0);
            cdlatch.countDown();
            //block on next write due to buffer of size 1
            out.write(1);
        };
        ManualExecutorService service = new ManualExecutorService();
        final Thread generatorThread = new Thread(() -> service.runTaskLoop(1), "Generator Thread");

        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(service, gen, 1);
        // this will add the generator as a task to run on the service
        InputStream in = genRes.inputStream();
        // begin the task, which should pause on writing the first number as the
        // buffer is only 1 byte and nothing is being read just yet.
        generatorThread.start();
        // give the serviceThread a chance to write that first byte and be
        // blocked waiting for it to be read. The serviceThread will notify when
        // the first byte is written.
        cdlatch.await();
        // interupt the generator thread.
        generatorThread.interrupt();
        // close the consumer, which in turn throws the interupted exception
        // wrapped in a IOException
        IOException ioe = assertThrows(IOException.class, () -> {
            in.close();
        }, "Sometimes occurs.");
        assertTrue(ioe instanceof InterruptedIOException);
        generatorThread.join(1000); // wait one second at most
        assertFalse(generatorThread.isAlive());
    }
    
    @Test
    public void testProducerConsumer_GeneratorClosedBeforeStart() throws Exception {
        Generator gen = (OutputStream out) -> {
            out.write(0);
        };
        ManualExecutorService service = new ManualExecutorService();
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(service, gen, 1);
        InputStream in = genRes.inputStream();
        
        // expect no exception to be thrown when canceling a generator before it has been started.
        in.close();
    }
    
    @Test
    public void testProducerConsumer_GeneratorCanceledBeforeStart() throws Exception {
        Generator gen = (OutputStream out) -> {
            out.write(0);
        };
        ManualExecutorService service = new ManualExecutorService();
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(service, gen, 1);
        ConsumerInputStream in = (ConsumerInputStream) genRes.inputStream();
        Future<Object> genFuture = in.getFuture();
        genFuture.cancel(true);
        
        IOException ioe = assertThrows(IOException.class, () -> {
            in.close();
        });
        assertTrue(ioe.getCause() instanceof CancellationException);
    }
    
    @Test
    public void testProducerConsumer_GeneratorClosedBeforeWrite() throws Exception {
        CountDownLatch genLatch = new CountDownLatch(1);
        CountDownLatch consumerLatch = new CountDownLatch(1);
        Generator gen = (OutputStream out) -> {
            genLatch.countDown();
            try {
                consumerLatch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();//reset flag.
            }
            out.write(0);
        };
        ManualExecutorService service = new ManualExecutorService();
        final Thread generatorThread = new Thread(() -> service.runTaskLoop(1), "Generator Thread");
        
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(service, gen, 1);
        InputStream in = genRes.inputStream();
        
        generatorThread.start();
        genLatch.await();
        // expect no exception to be thrown when canceling a generator before it has been read, but after is has been started.
        in.close();
        consumerLatch.countDown();
        
        generatorThread.join(1000); // wait one second
        assertFalse(generatorThread.isAlive());
    }
    
    @Test
    public void testProducerConsumer_GeneratorRuntimeExceptionFromThread() throws Exception {
        int lineCount = 100;
        Generator gen = new LineGenerator(lineCount, false);

        try (Cleaner c = new Cleaner()) {
            ExecutorService es = Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    throw new RuntimeException("Runtime exception from thread");
                }
            });
            c.shutdownLater(es);
            
            ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(es, gen, 1);

            assertThrows(RuntimeException.class, () -> {
                InputStream in = genRes.inputStream();
            });
        }
    }

    @Test
    public void testProducerConsumer_CloseInputStreamFirst() throws Exception {
        Generator gen = (out) -> {
            for (int i=0;i<10000;i++) {
                // Consumer PipedInputstream should have closed
                // by now, so exception is thrown.
                out.write(i);
            }
        };
        
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(gen, 1);

        // Start a generator thread. Open stream and read a little.
        InputStream inputStream = genRes.inputStream();
        assertEquals(0, inputStream.read());
        assertEquals(1, inputStream.read());
        assertEquals(2, inputStream.read());
        assertEquals(3, inputStream.read());
        // close piped input stream before Generator has finished
        // writing. Should not throw exception during close method.
        inputStream.close();
    }

    /**
     * Chains four producer consumer threads together.
     *
     * @throws Exception
     */
    @Test
    public void testProducerConsumer_LengthyChain() throws Exception {
        int lineCount = 100;
        Generator lineGen = new LineGenerator(lineCount, true);
        ConcurrentGeneratorResource genRes01 = new ConcurrentGeneratorResource(lineGen, 1);

        ConcurrentGeneratorResource genRes02 = new ConcurrentGeneratorResource(new Generator() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                genRes01.copyTo(out);
            }
        }, 1);

        ConcurrentGeneratorResource genRes03 = new ConcurrentGeneratorResource(new Generator() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                genRes02.copyTo(out);
            }
        }, 1);

        ConcurrentGeneratorResource genRes04 = new ConcurrentGeneratorResource(new Generator() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                genRes03.copyTo(out);
            }
        }, 1);

        assertThrows(IOException.class, () -> {
            testLines(lineCount, genRes04.inputStream());
        });
    }
    
    @Test
    public void testChangeDefaultExecutorService() {
        ManualExecutorService manualService = new ManualExecutorService();
        ExecutorService defaultService = ConcurrentGeneratorResource.setDefaultExecutorService(manualService);
        assertFalse(defaultService == manualService);
        
        ExecutorService replacedManualService = ConcurrentGeneratorResource.setDefaultExecutorService(defaultService);
        assertTrue(manualService == replacedManualService);
    }

    protected static class LineGenerator implements Generator {

        private final int lineCount;
        private final boolean throwException;

        public LineGenerator(int lineCount, boolean throwException) {
            this.lineCount = lineCount;
            this.throwException = throwException;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            PrintStream ps = new PrintStream(out);
            for (int i = 1; i <= lineCount; i++) {
                ps.println("This is line: " + i);
            }
            ps.println("End");
            ps.flush();
            if (throwException) {
                throw new IOException("Manually thrown exception");
            }
        }

        public static void testLines(int loopLineCount, InputStream in) throws IOException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                int lineNo = 1;
                while ((line = br.readLine()) != null) {
                    if (lineNo < loopLineCount) {
                        assertEquals("This is line: " + lineNo, line);
                        lineNo++;
                    } else {
                        assertEquals("End", br.readLine());
                    }
                }
                assertNull(br.readLine());
            }
        }
    }
}
