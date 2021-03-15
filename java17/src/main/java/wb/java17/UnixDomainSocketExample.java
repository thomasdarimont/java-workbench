package wb.java17;

import java.io.File;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class UnixDomainSocketExample {

    static volatile boolean listening;

    public static void main(String[] args) {

        String pathname = "/tmp/udstest.socket";
        new File(pathname).deleteOnExit();

        var udsa = UnixDomainSocketAddress.of(pathname);
        var executor = Executors.newFixedThreadPool(2);

        var server = executor.submit(() -> {
            try (var serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
                serverChannel.bind(udsa);

                listening = true;

                try (var channel = serverChannel.accept()) {
                    for (int i = 0; i < 10; i++) {
                        System.out.println("Server: writing... " + i);
                        writeMessageToSocket(channel, String.format("%s", i));
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    writeMessageToSocket(channel, "/close");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        var client = executor.submit(() -> {
            try (var channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {

                while (!listening) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }

                channel.connect(udsa);
                while (true) {
                    var message = readMessageFromSocket(channel);
                    if (message == null) {
                        continue;
                    }

                    System.out.printf("Client: reading... %s%n", message);
                    if (message.contains("/close")) {
                        channel.close();
                        break;
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        executor.shutdown();

        Stream.of(client, server)
                .map(ExSupplier::wrap)
                .forEach(ExSupplier::getSilently);

        System.out.println("Exit");
    }

    interface ExSupplier<V> {

        V get() throws Exception;

        default V getSilently() {
            try {
                return get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static <V> ExSupplier<V> wrap(Future<V> f) {
            return f::get;
        }
    }

    private static String readMessageFromSocket(SocketChannel channel) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0) {
            return null;
        }

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        return new String(bytes);
    }

    private static void writeMessageToSocket(SocketChannel socketChannel, String message) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(message.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }
}
