package au.id.simo.useful.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import au.id.simo.useful.test.DataGenFactory;
import au.id.simo.useful.test.ManualExecutorService;
import com.vmlens.api.AllInterleavings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static au.id.simo.useful.io.ConcurrentGeneratorResourceTest.LineGenerator.testLines;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ConcurrentGeneratorResourceInterleave {

    @Test
    //@Timeout(unit = TimeUnit.SECONDS, value = 1)
    public void testAll() throws Exception {
        try (AllInterleavings allInterleavings = AllInterleavings.builder(getClass().getSimpleName() + "_ManualExecutorService")
                .showStatementsInExecutor()
                .showNonVolatileSharedMemoryAccess()
                .build()) {
            while (allInterleavings.hasNext()) {
                ManualExecutorService execService = new ManualExecutorService();
                ConcurrentGeneratorResource res = new ConcurrentGeneratorResource(execService, (out) -> {
                    IOUtils.copy(DataGenFactory.incrementingBytes(10), out);
                });
                InputStream in = res.inputStream();
                Thread genThread = new Thread(() -> {
                    execService.runTask();
                });
                genThread.start();
                IOUtils.drain(in);
                in.close();
                genThread.join();
            }
        }
    }
    
    @Test
    //@Timeout(unit = TimeUnit.SECONDS, value = 1)
    public void testCloseConsumerFirst() throws Exception {
        try (AllInterleavings allInterleavings = AllInterleavings.builder(getClass().getSimpleName() + "_CloseConsumerFirst")
                .showStatementsInExecutor()
                .showNonVolatileSharedMemoryAccess()
                .build()) {
            while (allInterleavings.hasNext()) {
                ConcurrentGeneratorResource res = new ConcurrentGeneratorResource((out) -> {
                    out.write(new byte[]{0,1,2,3,4,5,6,7,8,9});
                });
                try (InputStream inputStream = res.inputStream()) {
                    assertEquals(0, inputStream.read());
                    assertEquals(1, inputStream.read());
                    assertEquals(2, inputStream.read());
                    assertEquals(3, inputStream.read());
                }
            }
        }
    }
    
    @Test
    //@Timeout(unit = TimeUnit.SECONDS, value = 1)
    public void testHappy() throws Exception {
        try (AllInterleavings allInterleavings = AllInterleavings.builder(getClass().getSimpleName() + "_Happy")
                .showStatementsInExecutor()
                .showNonVolatileSharedMemoryAccess()
                .build()) {
            while (allInterleavings.hasNext()) {
                ConcurrentGeneratorResource res = new ConcurrentGeneratorResource((out) -> {
                    out.write(new byte[]{0,1,2,3,4,5,6,7,8,9});
                });
                res.copyTo(IOUtils.NULL_OS);
            }
        }
    }
    
    /**
     * Chains four producer consumer threads together.
     *
     * @throws Exception
     */
    @Test
    //@Timeout(unit = TimeUnit.SECONDS, value = 1)
    public void testProducerConsumer_LengthyChain() throws Exception {
        try (AllInterleavings allInterleavings = AllInterleavings.builder(getClass().getSimpleName())
                .showStatementsInExecutor()
                .showNonVolatileSharedMemoryAccess()
                .build()) {
            int counter = 0;
            while (allInterleavings.hasNext()) {
                counter++;
                int lineCount = 100;
                Generator lineGen = new ConcurrentGeneratorResourceTest.LineGenerator(lineCount, true);
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
//                InputStream in = genRes04.inputStream();
//                IOUtils.drain(in);
//                in.close();
//                try {
                assertThrows(IOException.class, () -> {
                    testLines(lineCount, genRes04.inputStream());
//                    System.out.println("No Error in run No.: "+counter);
                });
//                } catch (IOException e) {
//                    System.out.println("Error in run No.: "+counter);
//                }
            }
        }
    }
}
