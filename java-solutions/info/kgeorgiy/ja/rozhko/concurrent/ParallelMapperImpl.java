package info.kgeorgiy.ja.rozhko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A utility class that creates threads of threads, 
 * each of which passively waits for new tasks to appear. 
 * When you use the map function, you add calls to the function 
 * f on all the elements in the args argument to the list of tasks 
 * that the threads of the threads are executing in parallel.
 * 
 * @see ParallelMapperImpl#map(Function, List) 
 * @see ParallelMapperImpl#ParallelMapperImpl(int) 
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final ArrayDeque<Runnable> list;
    private final List<Thread> threads;
    private final Integer MAX_QUEUE_LIST = 2;

    /**
     * A constructor that creates an instance of Parallel Mapper Impl and starts threads of threads,
     * each of which is passively waiting for a new task to appear.
     *
     * @param threads - number of threads to create.
     */
    public ParallelMapperImpl(int threads) {
        this.list = new ArrayDeque<>();
        this.threads = new ArrayList<>(Collections.nCopies(threads, null));
        threadCreate(threads);
    }

    private void threadCreate(int threads) {
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable el;
                        synchronized (list) {
                            while (list.size() == 0) {list.wait();}
                            el = list.poll();
                            list.notifyAll();
                        }
                        el.run();
                    }
                } catch (InterruptedException e) {
                    //
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
            this.threads.set(i, thread);
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @param f
     * @param args
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Resulter <R> resulter = new Resulter<>(new ArrayList<>(Collections.nCopies(args.size(), null)));
        synchronized (list) {
            for (int i = 0; i < args.size(); i++) {
                if (list.size() > MAX_QUEUE_LIST) {
                    list.wait();
                }
                int finalI = i;
                list.add(() -> {
                    try {
                        resulter.writeResult(finalI, f.apply(args.get(finalI)));
                    } catch (RuntimeException e) {
                        resulter.writeError(e);
                    }
                });
                list.notify();
            }
        }
        return resulter.getAns();
    }

    /**
     * Inner class that counts the number of remaining tasks
     *
     * @param <R> - result array type
     */
    private class Resulter<R> {
        private volatile Integer taskCount;
        private final List<R> result;
        private RuntimeException error = null;

        /**
         * Standard constructor.
         *
         * @param result - Array in which responses are returned
         */
        private Resulter(List<R> result) {
            this.taskCount = result.size();
            this.result = result;
        }

        /**
         * Writes the result to the result array
         *
         * @param ind - response index
         * @param ans - the answer itself
         */
        private synchronized void writeResult(int ind, R ans) {
            result.set(ind, ans);
            taskCount--;
            if (taskCount <= 0) {
                this.notify();
            }
        }

        private synchronized void writeError(RuntimeException e) {
            if (this.error == null) {
                this.error = e;
            } else {
                this.error.addSuppressed(e);
            }
            taskCount--;
            if (taskCount <= 0) {
                this.notify();
            }
        }

        /**
         * Waits for all tasks to complete, and then returns the response
         *
         * @return the response to the execution of all tasks, written to the result array
         * @throws InterruptedException - if the wait is interrupted
         */
        private synchronized List<R> getAns() throws InterruptedException{
            while (taskCount != 0) {this.wait();}
            if (this.error != null) {
                throw this.error;
            }
            return result;
        }
    }

    /**
     * Stops all threads. All unfinished mappings are left in undefined state.
     */
    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                //
            }
        }
    }
}


//        private synchronized void writeErrorVersion2(RuntimeException e) {
//            if (this.error == null) {
//                this.error = e;
//            } else {
//                this.error.addSuppressed(e);
//            }
//
//            close();
//            threadCreate(threads.size());
//            list.clear();
//            taskCount = 0;
//            this.notify();
//        }