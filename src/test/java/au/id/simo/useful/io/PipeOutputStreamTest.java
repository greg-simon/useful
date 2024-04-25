package au.id.simo.useful.io;

import au.id.simo.useful.Defer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PipeOutputStreamTest {

    @Test
    void testSameThreadHappy() throws IOException {
        PipeOutputStream rout = new PipeOutputStream(100);
        final byte[] writeArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final byte[] expectedArray = writeArray;

        rout.write(writeArray);
        rout.close();

        byte[] readArray = IOUtils.getBytes(rout.getInputStream());
        assertArrayEquals(expectedArray, readArray);
    }

    @Test
    void testTwoThreadsHappy() throws InterruptedException, ExecutionException {
        PipeOutputStream pipeOutputStream = new PipeOutputStream(1024);
        InputStream in = pipeOutputStream.getInputStream();

        final String testString = "data to write";
        final byte[] writeArray = testString.getBytes(StandardCharsets.UTF_8);

        try (Defer defer = new Defer()) {
            ExecutorService service = defer.shutdown(Executors.newCachedThreadPool());
            Future<PipeOutputStream> writeFuture = service.submit(() -> {
                pipeOutputStream.write(writeArray);
                pipeOutputStream.close();
                return pipeOutputStream;
            });

            Future<String> readFuture = service.submit(() -> IOUtils.getStringAsUTF8(in));

            PipeOutputStream result = writeFuture.get();
            String readString = readFuture.get();
            assertEquals(testString, readString);
            System.out.println(readString);
        }
    }
}
