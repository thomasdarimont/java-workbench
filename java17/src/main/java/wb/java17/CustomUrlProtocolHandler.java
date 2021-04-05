package wb.java17;

import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CustomUrlProtocolHandler {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.protocol.handler.pkgs",
                "wb.java17.protocol");

        var url = new URL("http://test/1");

        System.out.println(new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8));
    }

}
