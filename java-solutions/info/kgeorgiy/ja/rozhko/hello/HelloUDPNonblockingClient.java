package info.kgeorgiy.ja.rozhko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A client class that has a run method that allows you to send
 * udp requests to the server in several threads and display responses
 * along with the original messages
 *
 * @author paVlaDog
 */
public class HelloUDPNonblockingClient extends AbstractHelloUDPClient {
    /**
     * It accepts args and executes the run method if valid data was passed.
     *
     * @param args - Runs the run method, accepting data in the format method run
     * @see HelloUDPClient#run(String, int, String, int, int)
     */
    public static void main(String[] args) {
        subMain(new HelloUDPNonblockingClient(), args);
    }

    /**
     * Runs Hello client.
     * This method should return when all requests are completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        List<DatagramChannel> channels = new ArrayList<>();
        try (Selector selector = Selector.open()) {
            SocketAddress address = new InetSocketAddress(host, port);
            for (int i = 1; i < threads + 1; i++) {
                DatagramChannel datagramChannel = (DatagramChannel) DatagramChannel.open().configureBlocking(false);
                datagramChannel.connect(address).register(selector, SelectionKey.OP_WRITE,
                        new Attachment(datagramChannel.socket().getReceiveBufferSize(), i));
                channels.add(datagramChannel);
            }

            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                selector.select(TIMEOUT);
                final Set<SelectionKey> keys = selector.selectedKeys();
                if (keys.isEmpty()) {
                    selector.keys().forEach((key) -> key.interestOps(SelectionKey.OP_WRITE));
                }
                for (final Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
                    try {
                        SelectionKey key = iter.next();
                        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                        Attachment data = (Attachment) key.attachment();
                        String requestStroke = requestStrokeGenerate(prefix, data.threadNumber, data.requestNumber);
                        if (key.isReadable()) {
                            datagramChannel.receive(data.buffer.clear());
                            data.requestNumber += ansHandler(CHARSET.decode(data.buffer.flip()).toString(),
                                    requestStroke, data.threadNumber, data.requestNumber) ? 1 : 0;
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
                            datagramChannel.send(ByteBuffer.wrap((requestStroke).getBytes(CHARSET)), address);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                        if (data.requestNumber > requests) datagramChannel.close();
                    } catch (IOException e) {
                        System.err.println("SelectorKey handler problem" + e);
                    } finally {
                        iter.remove();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Selector input/output error" + e);
        } finally {
            channels.forEach((this::datagramChannelClose));
        }
    }

    private void datagramChannelClose(DatagramChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class Attachment {
        private final ByteBuffer buffer;
        private final int threadNumber;
        private int requestNumber;
        public Attachment(int bufSize, int threadNumber) {
            this.buffer = ByteBuffer.allocate(bufSize);
            this.requestNumber = 1;
            this.threadNumber = threadNumber;
        }
    }

}
