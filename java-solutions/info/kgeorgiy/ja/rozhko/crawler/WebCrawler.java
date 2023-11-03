package info.kgeorgiy.ja.rozhko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A class that finds all links reachable from
 * the given url in no more than depth hops.
 *
 * @author paVlaDog
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloaderService;
    private final ExecutorService extractorService;

    /**
     * Creates an instance of the Web Crawler class
     * and calls the download method with
     * the given url and depth.
     *
     * @param args - Argument string in the format:
     *             [url, depth, max downloaders, max extractors, max perHost]
     * @see WebCrawler#download(String, int)
     */
    public static void main(String[] args) {
        if (args != null && Arrays.stream(args).allMatch(Objects::nonNull) && args.length >= 5) {
            try (WebCrawler crawler = new WebCrawler(new CachingDownloader(1.0),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4])
            )) {
                Result result = crawler.download(args[0], Integer.parseInt(args[1]));
                System.out.println("===Downloaded urls===");
                result.getDownloaded().forEach(System.out::println);
                System.out.println("=======Errors=======");
                result.getErrors().forEach((key, value) -> System.out.println(key + " | " + value));
            } catch (NumberFormatException e) {
                System.err.println("Incorrect args - second, third, forth and fifth arguments was be Integer");
            } catch (IOException e) {
                System.err.println("CashingDownloader Input/Output error");
            }
        } else {
            System.err.println("Args is null or args length < 5");
        }
    }

    /**
     * A constructor for the Web Crawler class that accepts
     * a limit on the maximum number of threads that download documents,
     * retrieve links, download from the same host, and the class used to download pages.
     *
     * @param downloader - the class used to load the page
     * @param downloaders - maximum number of threads loading the page
     * @param extractors - maximum number of threads fetching links
     * @param perHost - maximum number of streams downloaded from one host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloaderService = Executors.newFixedThreadPool(downloaders);
        extractorService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        return downloadSet(depth, Set.of(url), ConcurrentHashMap.newKeySet(), new ConcurrentHashMap<>());
    }

    private Result downloadSet(int depth, Set<String> queue, Set<String> downloaded, Map<String, IOException> errors) {
        final ConcurrentMap<String, AtomicInteger> counterOnHost = new ConcurrentHashMap<>();
        final Phaser phaser = new Phaser(1);
        final Set<String> newQueue = ConcurrentHashMap.newKeySet();
        final ExceptionCollector exceptionCollector = new ExceptionCollector();
        final TaskQueue taskQueue = new TaskQueue(counterOnHost);

        for (String element : queue) {
            try {
                String host = URLUtils.getHost(element);
                phaser.register();
                taskQueue.addTask(host, () -> {
                    try {
                        Document document = downloader.download(element);
                        downloaded.add(element);
                        if (depth > 1) {
                            phaser.register();
                            extractorService.submit(() -> {
                                try {
                                    newQueue.addAll(document.extractLinks());
                                } catch (IOException e) {
                                    errors.put(element, e);
                                } catch (RuntimeException e) {
                                    exceptionCollector.addException(e);
                                } finally {
                                    phaser.arriveAndDeregister();
                                }
                            });
                        }
                    } catch (IOException e) {
                        errors.put(element, e);
                    } catch (RuntimeException e) {
                        exceptionCollector.addException(e);
                    } finally {
                        phaser.arriveAndDeregister();
                        counterOnHost.get(host).getAndIncrement();
                    }
                });
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Illegal URL" + e.getMessage());
            }
            downloaderService.submit(() -> taskQueue.getTask().run());
        }

        phaser.arriveAndAwaitAdvance();
        exceptionCollector.throwExceptionIfExist();
        return depth > 1 && !newQueue.isEmpty() ? downloadSet(depth - 1, newQueue.stream()
                .filter((el) -> !downloaded.contains(el) && !errors.containsKey(el))
                .collect(Collectors.toSet()), downloaded, errors) : new Result(downloaded.stream().toList(), errors);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     *
     * @throws RuntimeException if java.lang.executorService does not respond to shutdownNow
     */
    @Override
    public void close() {
        closeExecutorService(extractorService);
        closeExecutorService(downloaderService);
    }

    private void closeExecutorService(ExecutorService downloaderService) {
        downloaderService.shutdown();
        try {
            if (!downloaderService.awaitTermination(2, TimeUnit.SECONDS)) {
                downloaderService.shutdownNow();
                if (!downloaderService.awaitTermination(2, TimeUnit.SECONDS)) {
                    throw new RuntimeException("java.lang.executorService does not respond to shutdownNow");
                }
            }
        } catch (final InterruptedException exception) {
            downloaderService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class ExceptionCollector {
        private RuntimeException exception = null;

        private synchronized void addException(RuntimeException exception) {
            this.exception = this.exception == null ? exception : this.exception;
            this.exception.addSuppressed(exception);
        }

        private void throwExceptionIfExist() {
            if (exception != null) {
                throw this.exception;
            }
        }
    }

    private class TaskQueue {
        private final BlockingQueue <Pair<String, Runnable>> runnableQueue = new LinkedBlockingQueue<>();
        private final Map<String, AtomicInteger> counterOnHost;
        public TaskQueue(Map<String, AtomicInteger> counterOnHost) {
            this.counterOnHost = counterOnHost;
        }

        private void addTask(String host, Runnable runnable) {
            runnableQueue.add(new Pair<>(host, runnable));
        }

        private synchronized Runnable getTask() {
            while (true) {
                Pair<String, Runnable> currentElement = runnableQueue.poll();
                if (currentElement == null) throw new NoSuchElementException("No tasks left");
                counterOnHost.putIfAbsent(currentElement.first, new AtomicInteger(perHost));
                if (counterOnHost.get(currentElement.first).get() > 0) {
                    counterOnHost.get(currentElement.first).getAndDecrement();
                    return currentElement.second;
                } else {
                    runnableQueue.add(currentElement);
                }
            }

        }
    }

    private record Pair<T, R>(T first, R second) {}
}
