package info.kgeorgiy.ja.rozhko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class with useful functions for interacting with arrays, each of which takes as an argument threads
 * - the number of threads of parallel code execution.
 *
 * @author paVlaDog
 */
public class IterativeParallelism implements ListIP {

    /**
     * ParallelMapper used for processing.
     */
    private final ParallelMapper pm;

    /**
     * Standard constructor for IterativeParallelism.
     * Uses a manual version of List processing
     */
    public IterativeParallelism() {
        this.pm = null;
    }

    /**
     * Standard constructor for IterativeParallelism.
     * Uses ParallelMapper for sheet processing
     *
     * @param pm ParallelMapper used for processing.
     */
    public IterativeParallelism(final ParallelMapper pm) {
        this.pm = pm;
    }

    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return list of joined results of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallelProcess(threads, values,
                (list) -> list.map(Objects::toString).collect(Collectors.joining()),
                (list) -> list.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicate. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: copy-paste
        return parallelProcess(threads, values, (list) -> list.filter(predicate).collect(Collectors.toList()), extractAnswer());
    }


    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values  values to map.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelProcess(threads, values, (list) -> list.map(f).collect(Collectors.toList()), extractAnswer());
    }

    private <T> Function<Stream<List<T>>, List<T>> extractAnswer() {
        return (list) -> list.flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: Stream i
        // :NOTE: copy-paste
        return parallelProcess(threads, values, i -> maxSubFunction(i, comparator), (i) -> maxSubFunction(i, comparator));
    }


    private static <T> T maxSubFunction(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.max(comparator).orElseThrow();
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelProcess(threads, values,
                list -> list.anyMatch(predicate),
                list -> list.anyMatch(Boolean::booleanValue));
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelProcess(threads, values,
                (list) -> (int) list.filter(predicate).count(),
                (list) -> list.reduce(0, Integer::sum)
        );
    }


    private <T, E> E parallelProcess(
            final int threads, final List<? extends T> values, final Function<Stream<? extends T>, E> func,
            final Function<Stream<E>, E> reducer
    ) throws InterruptedException {
        final int iterCount = Math.min(threads, values.size());
        // :NOTE: ArrayList
        final ArrayList<Stream<? extends T>> startArr = new ArrayList<>(Collections.nCopies(iterCount, null));
        final int countSmallArrays = iterCount - values.size() % iterCount;
        for (int i = 0; i < iterCount; i++) { // :NOTE: IntSteam
            // :NOTE: common calc
            startArr.set(i,
                    values.subList(getIdealIndex(values.size() / iterCount, countSmallArrays, i),
                            getIdealIndex(values.size() / iterCount, countSmallArrays, i + 1)
                    ).stream());
        }
        final List<E> result = pm != null ? pm.map(func, startArr) : handParall(func, startArr);
        return reducer.apply(result.stream());
    }

    private <T> int getIdealIndex(int valuesSize, int countSmallArrays, int i) {
        return i < countSmallArrays
                ? valuesSize * i
                : valuesSize * countSmallArrays + (valuesSize + 1) * (i - countSmallArrays);
    }

    private <T, E> List<E> handParall(
            final Function<Stream<? extends T>, E> func,
            final List<Stream<? extends T>> startArr
    ) throws InterruptedException {
        final List<E> answer = new ArrayList<>(Collections.nCopies(startArr.size(), null));
        final List<Thread> threads = IntStream.range(0, startArr.size())
                .mapToObj(i -> new Thread(() -> answer.set(i, func.apply(startArr.get(i)))))
                .toList();

        threads.forEach(Thread::start);

        InterruptedException exception = null;
        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                // :NOTE: simplify
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }

                for (final Thread thr : threads) {
                    thr.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException e1) {
                        // :NOTE: not joined
                        exception.addSuppressed(e1);
                    }
                }
                throw exception;
            }
        }
        return answer;
    }
}

//    public static void main(String[] args) throws InterruptedException {
//        ParallelMapper pm = new ParallelMapperImpl(3);
//        IterativeParallelism ip = new IterativeParallelism(pm);
//        try {
//            List<Integer> ans = ip.map(2, List.of(1, 2, 3, 4, 5), (i) -> {
//                try {
//                    sleep(1000);
//                    if (i == 2) {
//                        throw new NoSuchElementException("eooo");
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return i + 1;
//            });
//        } catch (NoSuchElementException e) {
//            System.out.println("Tuc-tuc. I'm exception!");
//            e.printStackTrace();
//        }
//
//        List<Integer> ans2 = ip.map(2, List.of(1, 2, 3, 4, 5), (i) -> {
//            return i + 1;
//        });
//        for (int i = 0; i < ans2.size(); i++) {
//            System.out.println(ans2.get(i));
//        }
//    }


//    @Override
//    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
//        int iterCount = Math.min(threads, values.size());
//        ArrayList<Boolean> ans = new ArrayList<>(Collections.nCopies(iterCount, null));
//        Thread[] thrs = new Thread[iterCount];
//        for (int i = 0; i < iterCount; i++) {
//            int iter = i;
//            Thread thread = new Thread(
//                    () -> {ans.set(iter, values.subList((values.size() / iterCount) * iter,
//                                    iter != iterCount - 1 ? (values.size() / iterCount) * (iter + 1) : values.size()).stream()
//                            .anyMatch((a) -> predicate.test(a) || (ans.get((iter - 1 + ans.size()) % ans.size()) != null &&
//                                    ans.get((iter - 1 + ans.size()) % ans.size()))));});
//            thread.start();
//            thrs[iter] = thread;
//        }
//        for (final Thread thread : thrs) {
//            thread.join();
//        }
//        return ans.stream().reduce((a, b) -> a || b).orElse(Boolean.FALSE);
//    }
