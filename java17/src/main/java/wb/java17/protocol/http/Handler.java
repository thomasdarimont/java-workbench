package wb.java17.protocol.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Handler extends URLStreamHandler {

    private static final Map<String, Map<String, byte[]>> MEMORY = new HashMap<>();

    static {
        MEMORY.put("test", Map.of("1", "Hallo Welt".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    protected URLConnection openConnection(URL u) {


        String memorySegment = u.getHost();
        String blob = u.getFile().substring(1);


        return new URLConnection(u) {
            @Override
            public void connect() throws IOException {
                getData(memorySegment, blob);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(getData(memorySegment, blob));
            }
        };
    }

    private byte[] getData(String memorySegment, String blob) throws IOException {
        Map<String, byte[]> segment = MEMORY.get(memorySegment);
        if (segment == null) {
            throw new IOException("Segment not found");
        }

        byte[] data = segment.get(blob);
        if (data == null) {
            throw new IOException("Blob not found");
        }
        return data;
    }
}
