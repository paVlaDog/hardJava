package info.kgeorgiy.ja.rozhko.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final ArrayListReversible<E> list;
    private final Comparator<? super E> comparator;
    private final boolean comparatorExist;

    public ArraySet() {
        this.list = new ArrayListReversible<>();
        this.comparator = null;
        this.comparatorExist = false;
    }

    public ArraySet(Collection<? extends E> col) {
        this.list = new ArrayListReversible<>(new TreeSet<>(col));
        this.comparator = null;
        this.comparatorExist = false;
    }

    public ArraySet(Collection<? extends E> col, Comparator<? super E> comp) {
        TreeSet<E> set = new TreeSet<>(comp);
        set.addAll(col);
        this.list = new ArrayListReversible<>(set);
        this.comparator = comp;
        this.comparatorExist = true;
    }

    public ArraySet(ArrayListReversible<E> list, Comparator<? super E> comp) {
        this.list = list;
        this.comparator = comp;
        this.comparatorExist = true;
    }

    private int findIndex(E e, boolean leftIndex, boolean ansInRight) {
        int index = Collections.binarySearch(list, e, comparator);
        return index < 0 ? -index - 1 - (leftIndex ? 1 : 0) : index - (ansInRight ? 1 : 0) + (leftIndex ? 0 : 1);
    }

    private E getElementOnIndex(int index) {
        return index == -1 || index == size() ? null : list.get(index);
    }

    @Override
    public E lower(E e) {
        return getElementOnIndex(findIndex(e, true, true));
    }

    @Override
    public E floor(E e) {
        return getElementOnIndex(findIndex(e, true, false));
    }

    @Override
    public E ceiling(E e) {
        return getElementOnIndex(findIndex(e, false, true));
    }

    @Override
    public E higher(E e) {
        return getElementOnIndex(findIndex(e, false, false));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ArrayListReversible<>(list, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("First element greater the second element");
        }
        int leftBound = findIndex(fromElement, false, fromInclusive);
        int rightBound = findIndex(toElement, true, !toInclusive);
        if (rightBound + 1 < leftBound) {
            return new ArraySet<>();
        }
        return new ArraySet<>(list.subList(leftBound, rightBound + 1), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty() || compare(toElement, first()) < 0) return new ArraySet<>();
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty() || compare(last(), fromElement) < 0) return new ArraySet<>();
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    private E get(int index) {
        if (index < 0 || index >= size()) {
            throw new NoSuchElementException("Array size = 0");
        }
        return list.get(index);
    }

    @Override
    public E first() {
        return get(0);
    }

    @Override
    public E last() {
        return get(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        int left = findIndex((E) o, true, false);
        return left != -1 && compare(o, list.get(left)) == 0;
    }


//    ToIntBiFunction<E, E> compare2(Comparator<? super E> comparator) {
//        return comparator==null ? new ToIntBiFunction<E, E>((k1, k2) -> ((Comparable<? super E>)k1).compareTo(k2))
//                : new ToIntBiFunction<E, E>((k1, k2) -> comparator.compare((E)k1, (E)k2));
//    }

    @SuppressWarnings("unchecked")
    final int compare(Object k1, Object k2) throws ClassCastException {
        // :NOTE: не хочется на каждый вызов compare сравнивать с нулом
        return !comparatorExist ? ((Comparable<? super E>) k1).compareTo((E) k2)
                : comparator.compare((E) k1, (E) k2);
    }

    @Override
    public int size() {
        return list.size();
    }
}
