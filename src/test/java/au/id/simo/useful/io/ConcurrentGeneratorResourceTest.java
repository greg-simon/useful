package au.id.simo.useful.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import au.id.simo.useful.Cleaner;
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

        ConcurrentGeneratorResource genRes
                = new ConcurrentGeneratorResource(lineGen);

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
    public void testProducerConsumer_GeneratorInteruptedException() throws Exception {
        int lineCount = 100;
        Generator gen = (OutputStream out) -> {
            for(int i=0;i<1000;i++) {
                out.write(i);
            }
        };

        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(gen, 1);
        Thread currentThread = Thread.currentThread();
        InputStream in = genRes.inputStream();
        currentThread.interrupt();
        in.close();
        assertTrue(currentThread.isInterrupted(), "Thread should still be interrupted");
        assertThrows(InterruptedException.class, () -> {
            // clean interupt flag by having an InterruptedException to be thrown.
            Thread.sleep(0);
        });
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
        // Start a generator thread.
        ConcurrentGeneratorResource genRes = new ConcurrentGeneratorResource(new Generator() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                for (int i=0;i<10000;i++) {
                    // Consumer PipedInputstream should have closed
                    // by now, so exception is thrown.
                    out.write(i);
                }
            }
        }, 1);

        // open stream and read a little.
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
