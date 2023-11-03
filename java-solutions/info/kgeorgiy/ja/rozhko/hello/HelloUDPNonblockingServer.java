package info.kgeorgiy.ja.rozhko.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * A server that accepts requests on the port specified in the start method
 * and returns hello + the original string in response to any request
 *
 * @author paVlaDog
 */
public class HelloUDPNonblockingServer extends AbstractHelloUDPServer {
    private DatagramChannel datagramChannel;
    private Selector selector;
    private Queue<TaskData> responseQueue;

    protected void setup(int port) throws IOException {
        responseQueue = new LinkedBlockingQueue<>();
        selector = Selector.open();
        datagramChannel = (DatagramChannel) DatagramChannel.open().configureBlocking(false);
        datagramChannel.register(selector, SelectionKey.OP_READ);
        datagramChannel.bind(new InetSocketAddress(port));
    }

    protected Runnable run(int port) {
        return () -> {
            while (!Thread.interrupted() && !datagramChannel.socket().isClosed()) {
                try {
                    selector.select();
                    for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext();) {
                        try {
                            SelectionKey key = iter.next();
                            if (key.isReadable()) {
                                ByteBuffer buffer = ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize());
                                SocketAddress address = datagramChannel.receive(buffer);
                                final TaskData taskData = new TaskData(buffer, address);
                                requestHandler.submit(() -> {
                                    taskData.buffer = ByteBuffer.wrap(responseGenerate(
                                            CHARSET.decode(taskData.buffer.flip()).toString()).getBytes(CHARSET));
                                    responseQueue.add(taskData);
                                    key.interestOpsOr(SelectionKey.OP_WRITE);
                                    selector.wakeup();
                                });
                            } else if (key.isWritable()) {
                                if (responseQueue.isEmpty()) {
                                    key.interestOpsAnd(~SelectionKey.OP_WRITE);
                                } else {
                                    TaskData responseData = responseQueue.poll();
                                    datagramChannel.send(responseData.buffer, responseData.address);
                                }
                            }
                        } finally {
                            iter.remove();
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        try {
            if (Objects.nonNull(datagramChannel)) datagramChannel.close();
            if (Objects.nonNull(selector)) selector.close();
            if (Objects.nonNull(requestHandler)) closeExecutorService(requestHandler);
            if (Objects.nonNull(oneHandler)) closeExecutorService(oneHandler);
        } catch (IOException e) {
            System.out.println("Closing error" + e);
        }
    }

    private static class TaskData {
        private ByteBuffer buffer;
        private final SocketAddress address;

        public TaskData (final ByteBuffer buffer, final SocketAddress address) {
            this.buffer = buffer;
            this.address = address;
        }
    }
}
