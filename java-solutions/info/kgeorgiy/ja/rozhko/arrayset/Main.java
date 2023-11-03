package info.kgeorgiy.ja.rozhko.arrayset;

import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
//        NavigableSet<Integer> s1 = new ArraySet<>();
        NavigableSet<Integer> s = new ArraySet<>(new ArrayList<>(){{
            add(2);
            add(1);
            add(7);
            add(3);
            add(5);
        }});
        System.out.println("des:");
        NavigableSet<Integer> s1 = s.descendingSet();
        for (Integer el : s.subSet(1, 5)) {
            System.out.println(el   );
        }

        System.out.println("Working!");
    }
}
