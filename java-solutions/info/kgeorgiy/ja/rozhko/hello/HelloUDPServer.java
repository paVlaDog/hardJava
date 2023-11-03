package info.kgeorgiy.ja.rozhko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 * A server that accepts requests on the port specified in the start method
 * and returns hello + the original string in response to any request
 *
 * @author paVlaDog
 */
public class HelloUDPServer extends AbstractHelloUDPServer {
    private DatagramSocket datagramSocket;

    protected Runnable run(int port) {
        return () -> {
            try {
                datagramSocket = new DatagramSocket(port);
                int bufferSize = datagramSocket.getReceiveBufferSize();
                while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                        datagramSocket.receive(request);
                        requestHandler.submit(() -> {
                            try {
                                byte[] responseBytes = (responseGenerate(new String(
                                        request.getData(), request.getOffset(),
                                        request.getLength(), CHARSET))).getBytes(CHARSET);
                                DatagramPacket response = new DatagramPacket(responseBytes,
                                        responseBytes.length, request.getSocketAddress());
                                datagramSocket.send(response);
                            } catch (SocketTimeoutException e) {
                                //
                            } catch (IOException e) {
//                                System.err.println("Send message error" + e.getMessage());
                            }
                        });
                    } catch (SocketTimeoutException e) {
                        //
                    } catch (IOException e) {
//                        System.err.println("Receive message error" + e.getMessage());
                    }
                }
            } catch (SocketException e) {
                System.err.println("Socket creation error or UDP protocol problem" + e.getMessage());
            }
        };
    }

    protected void setup(int port) {
        //
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        if (Objects.nonNull(requestHandler)) datagramSocket.close();
        if (Objects.nonNull(requestHandler)) closeExecutorService(requestHandler);
        if (Objects.nonNull(requestHandler)) closeExecutorService(oneHandler);
    }


}
