package au.id.simo.useful.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import au.id.simo.useful.io.local.LocalProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class ZipBundlerTest {

    public static List<URLSession> sessionsToTest() throws IOException {
        return Arrays.asList(
                new FileSession(),
                LocalProtocol.newSession()
        );
    }

    @ParameterizedTest
    @MethodSource("sessionsToTest")
    public void testBasicUsage(URLSession sessionInstance) throws Exception {
        try (URLSession session = sessionInstance) {
            session.register("/path/to/file1.txt", new StringResource("File1 contents"));
            session.register("file2.txt", new StringResource("File2 contents"));

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ZipBundler zipBundler = new ZipBundler(bout)) {
                session.forEachResource(zipBundler);
            }

            assertZipFileEntries(Arrays.asList(
                    "path/to/file1.txt",
                    "file2.txt"
            ),bout.toByteArray());
        }
    }

    @Test
    public void testConstructor_OutputStream() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ZipBundler zipBundler = new ZipBundler(bout)) {
            zipBundler.add("path1", new StringResource("Path1"));
            zipBundler.add("path2", new StringResource("Path2"));
        }
        assertZipFileEntries(Arrays.asList(
                "path1",
                "path2"
        ), bout.toByteArray());
    }
    
    @Test
    public void testConstructor_ZipOutputStream() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        try (ZipBundler zipBundler = new ZipBundler(zout)) {
            zipBundler.add("path1", new StringResource("Path1"));
            zipBundler.add("path2", new StringResource("Path2"));
        }
        assertZipFileEntries(Arrays.asList(
                "path1",
                "path2"
        ), bout.toByteArray());
    }

    public void assertZipFileEntries(List<String> expectedEntries, byte[] zipFileAsBytes) throws IOException {
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipFileAsBytes));
        List<String> entryList = new ArrayList<>(expectedEntries.size());
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            entryList.add(entry.getName());
        }
        Collections.sort(entryList);
        Collections.sort(expectedEntries);
        
        assertEquals(expectedEntries.size(), entryList.size(), "Zip file contained unexpected number of entries");
        for (int i=0;i<expectedEntries.size();i++) {
            assertEquals(expectedEntries.get(i), entryList.get(i));
        }
    }
}
