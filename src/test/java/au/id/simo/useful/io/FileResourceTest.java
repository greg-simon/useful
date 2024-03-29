package au.id.simo.useful.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class FileResourceTest implements ResourceTest {
    
    @TempDir
    static Path tempDir;
    static Path tempFile;

    @Override
    public FileResource createResource(byte[] testData, Charset charset) throws IOException {
        tempFile = Files.createFile(tempDir.resolve("test.txt"));
        Files.write(tempFile, testData);
        return new FileResource(tempFile);
    }
    
    @AfterEach
    public void cleanup() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }
    
    private static Stream<Arguments> stringConstructorTests() {
        return Stream.of(
            Arguments.of("/test.txt", "/test.txt", new String[]{}),
            Arguments.of("/test.txt", "/", new String[]{"test.txt"}),
            Arguments.of("/dir1/dir2/dir3/test.txt", "/", new String[]{
                "dir1",
                "dir2",
                "dir3",
                "test.txt"
            }),
            Arguments.of("/dir1/test.txt", "/", new String[]{
                "dir1",
                "dir2",
                "dir3",
                "../",
                "../",
                "test.txt"
            })
        );
    }
    
    @ParameterizedTest
    @MethodSource("stringConstructorTests")
    public void testConstructor_String(String expectedAbsolutePath, String path, String... morePath) {
        FileResource fr = new FileResource(path, morePath);
        assertEquals(Paths.get(expectedAbsolutePath).toAbsolutePath(), fr.getPath().toAbsolutePath());
    }
    
    @Test
    public void testConstructor_StringNoMore() {
        String pathStr = "/file.txt";
        FileResource fr = new FileResource(pathStr);
        assertEquals(Paths.get(pathStr), fr.getPath());
    }
    
    @Test
    public void testConstructor_File() throws IOException {
        FileResource fr = createResource("Hello there".getBytes(), null);
        assertEquals(tempFile.toAbsolutePath(), fr.getPath().toAbsolutePath());
        assertArrayEquals("Hello there".getBytes(), Files.readAllBytes(tempFile));
    }
}
