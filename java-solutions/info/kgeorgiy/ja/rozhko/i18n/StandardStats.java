package info.kgeorgiy.ja.rozhko.i18n;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class StandardStats<T> {
    protected final String bundleString;
    protected final Set<T> variousElement;
    protected final Comparator<T> comparator;
    protected final Function<T, Double> lengthFunc;
    protected int countElement = 0;
    protected T minElement = null;
    protected T maxElement = null;
    protected double lengthAllElement = 0;
    protected Locale strLocale;

    public StandardStats(String bundleString, Comparator<T> comparator, Function<T, Double> lengthFunc, Locale strLocale) {
        this.bundleString = bundleString;
        this.variousElement = new HashSet<>();
        this.comparator = comparator;
        this.lengthFunc = lengthFunc;
        this.strLocale = strLocale;
    }

    public int getCount() {
        return countElement;
    }

    public String getStats() {
        return new MessageFormat(bundleString, strLocale).format(new Object[]{System.lineSeparator(), countElement,
                variousElement.size(), minElement, maxElement, lengthAllElement / countElement});
    }

    public void refreshStats(T element) {
        if (element == null) return;
        countElement++;
        variousElement.add(element);
        minElement = minElement == null || comparator.compare(minElement, element) > 0 ? element : minElement;
        maxElement = maxElement == null || comparator.compare(element, maxElement) > 0 ? element : maxElement;
        lengthAllElement += lengthFunc.apply(element);
    }
}
