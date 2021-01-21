package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class FileSessionTest implements URLSessionTest {

    @Override
    public URLSession createURLSession() throws IOException {
        return new FileSession();
    }
    
    @Test
    public void testGetBaseDir(@TempDir Path testFolder) {
        File sessionRoot = testFolder.resolve("filesession").toFile();
        sessionRoot.mkdirs();
        FileSession session = new FileSession(sessionRoot);
        assertEquals(sessionRoot, session.getBaseDir());
    }
    
    @Test
    public void testRegisterResource(@TempDir Path testFolder) throws Exception {
        File sessionRoot = testFolder.resolve("filesession").toFile();
        sessionRoot.mkdirs();

        FileSession session = new FileSession(sessionRoot);
        String testFileContents = "This is a test file";
        String fileUrl = session.register("test.txt",
                new StringResource(testFileContents));

        // known file location
        File regFile = new File(sessionRoot, "test.txt");
        assertTrue(regFile.exists(), "File is expected to exist");

        // did the returned url make sense, and is it the same file?
        File urlFile = new File(new URI(fileUrl));
        assertTrue(urlFile.exists());
        assertEquals(regFile.getAbsolutePath(),
                urlFile.getAbsolutePath());

        // check the contents
        String regFileContents
                = String.join("\n", Files.readAllLines(regFile.toPath()));
        assertEquals(testFileContents, regFileContents);
    }

    @Test
    public void testClose(@TempDir Path testFolder) throws Exception {
        File sessionRoot = testFolder.resolve("filesession").toFile();
        sessionRoot.mkdirs();
        
        assertTrue(sessionRoot.exists());

        FileSession session = new FileSession(sessionRoot);
        session.register("test.txt",
                new StringResource("Contents of file"));
        assertTrue(sessionRoot.exists());

        File regFile = new File(sessionRoot, "test.txt");
        assertTrue(regFile.exists());

        session.close();
        assertTrue(session.isClosed());

        // session root exists because the FileSession didn't create it.
        assertTrue(sessionRoot.exists());

        assertFalse(regFile.exists());
    }

    @Test
    public void testClose_NonDestructiveDeletes(@TempDir Path testFolder) throws IOException {
        File sessionRoot = testFolder.resolve("filesession").toFile();
        sessionRoot.mkdirs();
        assertTrue(sessionRoot.exists());

        File regFile;
        Path otherFile;
        try (FileSession session = new FileSession(sessionRoot)) {
            session.register(
                    "/path/to/test.txt",
                    new StringResource("Contents of file")
            );
            regFile = new File(sessionRoot, "path/to/test.txt");
            assertTrue(regFile.exists());
            
            // create new file outside of session.
            otherFile = sessionRoot.toPath().resolve("path/other.txt");
            Files.write(otherFile, "File created outside the session".getBytes());
        }
        assertTrue(sessionRoot.exists(), "File preexisted, so it should still exist");

        // check created files
        assertFalse(regFile.exists());
        // check other file is not deleted.
        assertTrue(Files.exists(otherFile));

        // check created dirs
        Path sessionRootPath = sessionRoot.toPath();
        assertTrue(Files.exists(sessionRootPath.resolve("path")));
        assertFalse(Files.exists(sessionRootPath.resolve("path/to")));
    }

}
