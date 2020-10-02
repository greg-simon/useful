package au.id.simo.useful.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ConcurrentGeneratorResourceTest {

    public void testLines(int loopLineCount, InputStream in) throws IOException {
        try (BufferedReader br
                = new BufferedReader(new InputStreamReader(in))) {
            for (int i = 1; i <= loopLineCount; i++) {
                String line = br.readLine();
                assertEquals("This is line: " + i, line);
            }
            assertEquals("End", br.readLine());
        }
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
    public void testProducerConsumer_GeneratorException() throws Exception {
        int lineCount = 100;
        Generator lineGen = new LineGenerator(lineCount, true);

        ConcurrentGeneratorResource genRes
                = new ConcurrentGeneratorResource(lineGen, 1);

        assertThrows(IOException.class, () -> {
            testLines(lineCount, genRes.inputStream());
        });
    }

    @Test
    public void testProducerConsumer_CloseInputStream() throws Exception {
        // Start a generator thread.
        ConcurrentGeneratorResource genRes
                = new ConcurrentGeneratorResource(new Generator() {
                    @Override
                    public void writeTo(OutputStream out) throws IOException {
                        try {
                            // 5 ms is enough to cause the consumer
                            // close to happen first.
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        // Consumer PipedInputstream should have closed
                        // by now, so exception is thrown.
                        out.write(0);
                    }
                });

        // close piped input stream before Generator has finished
        // writing.
        assertThrows(IOException.class, () -> {
            genRes.inputStream().close();
        }, "Piped closed");
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

    private class LineGenerator implements Generator {

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
    }
}
