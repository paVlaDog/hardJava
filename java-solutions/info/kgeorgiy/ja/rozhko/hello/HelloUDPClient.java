package info.kgeorgiy.ja.rozhko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * A client class that has a run method that allows you to send
 * udp requests to the server in several threads and display responses
 * along with the original messages
 *
 * @author paVlaDog
 */
public class HelloUDPClient extends AbstractHelloUDPClient {
    private ExecutorService requestSender;

    /**
     * It accepts args and executes the run method if valid data was passed.
     *
     * @param args - Runs the run method, accepting data in the format method run
     * @see HelloUDPClient#run(String, int, String, int, int)
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Args is null");
        } else if (IntStream.range(0, 5).anyMatch(i -> Objects.isNull(args[i]))) {
            System.err.println("Args[i] is null");
        } else if (args.length < 5) {
            System.err.println("Length of args < 5");
        } else {
            try {
                HelloClient client = new HelloUDPClient();
                client.run(args[0],
                        Integer.parseInt(args[1]),
                        args[2],
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4]));
            } catch (NumberFormatException e) {
                System.err.println("Args 2, 3 or 5 is not integer");
            }
        }
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
        SocketAddress address = new InetSocketAddress(host, port);
        requestSender = Executors.newFixedThreadPool(threads);
        try {
            IntStream.range(1, threads + 1).forEach((i) -> {
                requestSender.submit(() -> {
                    IntStream.range(1, requests + 1).forEach((j) -> {
                        try (DatagramSocket datagramSocket = new DatagramSocket()) {
                            DatagramPacket request = new DatagramPacket(new byte[0], 0, address);
                            DatagramPacket response = new DatagramPacket(new byte[0], 0);
                            int bufferSize = datagramSocket.getReceiveBufferSize();
                            datagramSocket.setSoTimeout(50);
                            String requestStroke = requestStrokeGenerate(prefix, i, j);
                            byte[] requestBytes = requestStroke.getBytes(StandardCharsets.UTF_8);
                            while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                                try {
                                    request.setData(requestBytes);
                                    request.setLength(requestBytes.length);
                                    response.setData(new byte[bufferSize]);
                                    response.setLength(bufferSize);
                                    if (getMessage(i, j, datagramSocket, request, response, requestStroke)) break;
                                } catch (final SocketTimeoutException | NumberFormatException e) {
                                    //
                                } catch (final IOException e) {
                                    System.err.println("Critical send/receive error: " + e.getMessage());
                                    break;
                                }
                            }
                        } catch (final SocketException e) {
                            System.err.println("Socket creation error: " + e.getMessage());
                        }
                    });
                });
            });
            requestSender.shutdown();
            requestSender.awaitTermination(1, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            requestSender.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private boolean getMessage(int i, int j, DatagramSocket datagramSocket, DatagramPacket request,
                               DatagramPacket response, String requestStroke) throws IOException {
        datagramSocket.send(request);
        datagramSocket.receive(response);
        String responseString = new String(response.getData(),
                        response.getOffset(),
                        response.getLength(),
                        StandardCharsets.UTF_8);
        return ansHandler(responseString, requestStroke, i, j);
    }

}