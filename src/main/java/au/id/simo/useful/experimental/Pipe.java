package au.id.simo.useful.experimental;

import au.id.simo.useful.experimental.ExchangerOutputStream;
import au.id.simo.useful.experimental.ExchangerInputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class Pipe {
    private InputStream in;
    private OutputStream out;
    
    public Pipe(int bufferSize) {
        ExchangerInputStream ein = new ExchangerInputStream(bufferSize);
        in = ein;
        out = new ExchangerOutputStream(ein);
    }
    
    public InputStream getInputStream() {
        return in;
    }
    
    public OutputStream getOutputStream() {
        return out;
    }
}
