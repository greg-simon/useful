package au.id.simo.useful.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class FileUtilTest {

    @Test
    public void testNewFileInDir(@TempDir Path testFolder) throws Exception {
        File parentDir = testFolder.resolve("parent-test").toFile();
        parentDir.mkdirs();
        File childFile = FileUtil.newFileInDir(parentDir, "child");
        assertEquals(parentDir, childFile.getParentFile());
    }

    @Test
    public void testNewFileInDir_ParentIsFile(@TempDir Path testFolder) throws Exception {
        Files.createDirectories(testFolder);
        File parentFile = testFolder.resolve("parentIsFile-test").toFile();
        // make sure file exists by writing something to it.
        Files.write(parentFile.toPath(), Arrays.asList("parent file"));

        // expect file creation to fail when parent is not a directory.
        assertThrows(IOException.class, () -> {
            File childFile = FileUtil.newFileInDir(parentFile, "child");
        });
    }

    @Test
    public void testNewFileBreakoutAttempt_DotDotSlash(@TempDir Path testFolder) throws Exception {
        File parentDir = testFolder.resolve("parent-test").toFile();
        parentDir.mkdirs();

        // expect file creation to fail here as a break out is attempted
        assertThrows(IOException.class, () -> {
            FileUtil.newFileInDir(parentDir, "../../outsideFile");
        });
    }

    /**
     * Using {@link java.io.File} alone will work with this case, where
     * FileUtils will throw an IOException.
     *
     * <pre><code>
     * // works
     * File newFile = new File(parentDir, "/outsideFile");
     * </code></pre>
     *
     * @throws Exception
     */
    @Test
    public void testNewFileBreakoutAttempt_Root(@TempDir Path testFolder) throws Exception {
        File parentDir = testFolder.resolve("parent-test").toFile();
        parentDir.mkdirs();
        // expect file creation to fail here as a break out is attempted
        assertThrows(IOException.class, () -> {
            FileUtil.newFileInDir(parentDir, "/outsideFile");
        });
    }
}
