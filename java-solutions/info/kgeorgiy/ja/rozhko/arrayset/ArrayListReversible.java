package info.kgeorgiy.ja.rozhko.arrayset;

import java.util.*;

public class ArrayListReversible<E> extends AbstractList<E> implements List<E>, RandomAccess{
    private final boolean reversed;
    private final List<E> list;

    public ArrayListReversible() {
        this.reversed = false;
        this.list = new ArrayList<E>();
    }

    @Override
    public int size() {
        return list.size();
    }

    public ArrayListReversible(Collection<? extends E> col) {
        this.reversed = false;
        this.list = new ArrayList<E>(col);
    }

    public ArrayListReversible(Collection<? extends E> col, boolean reversed) {
        this.reversed = reversed;
        this.list = new ArrayList<>(col);
    }

    @Override
    public E get(int index){
        return list.get(reversed ? size() - index - 1 : index);
    }
}
