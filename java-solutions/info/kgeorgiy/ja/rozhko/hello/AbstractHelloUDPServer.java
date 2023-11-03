package info.kgeorgiy.ja.rozhko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for rendering common server methods
 *
 * @author paVlaDog
 */
public abstract class AbstractHelloUDPServer extends AbstractHelloUPD implements HelloServer {
    protected ExecutorService requestHandler;
    protected ExecutorService oneHandler;

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        try {
            setup(port);
            requestHandler = Executors.newFixedThreadPool(threads);
            oneHandler = Executors.newSingleThreadExecutor();
            oneHandler.submit(run(port));
        } catch (IOException | UncheckedIOException e) {
            e.printStackTrace();
        }
    }

    protected abstract Runnable run(int port);
    protected abstract void setup(int port) throws IOException;

    protected void closeExecutorService(ExecutorService executorService) {
        boolean terminated = executorService.isTerminated();
        if (!terminated) {
            executorService.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = executorService.awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        executorService.shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected String responseGenerate(String request) {
        return "Hello, " + request;
    }
}
