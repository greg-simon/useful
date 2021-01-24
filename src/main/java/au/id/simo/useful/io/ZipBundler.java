package au.id.simo.useful.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Used with {@link URLSession#forEachResource(au.id.simo.useful.io.URLSession.ResourceCollector) } to
 * write all resources to zip bundle and write it to an OutputStream.
 */
public class ZipBundler implements Closeable, URLSession.ResourceCollector {
    
    private final ZipOutputStream zipOut;

    public ZipBundler(OutputStream out) {
        if (out instanceof ZipOutputStream) {
            this.zipOut = (ZipOutputStream) out;
        } else {
            this. zipOut = new ZipOutputStream(out);
        }
    }
    
    @Override
    public void add(String zipPath, Resource resource) throws IOException {
        ZipEntry entry = new ZipEntry(zipPath);
        zipOut.putNextEntry(entry);
        resource.copyTo(zipOut);
        zipOut.flush();
    }

    @Override
    public void close() throws IOException {
        zipOut.close();
    }
}
