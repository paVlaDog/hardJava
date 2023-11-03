package info.kgeorgiy.ja.rozhko.i18n;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;

public class ReinforcedStats<T> extends StandardStats<T>{
    private T minSizeElement = null;
    private T maxSizeElement = null;

    public ReinforcedStats(String bundleString, Comparator<T> comparator, Function<T, Double> lengthFunc, Locale strLocale) {
        super(bundleString, comparator, lengthFunc, strLocale);
    }

    @Override
    public String getStats() {
        return new MessageFormat(bundleString, strLocale).format(new Object[]{System.lineSeparator(), countElement,
                variousElement.size(), minElement, maxElement, lengthFunc.apply(minSizeElement), minSizeElement,
                lengthFunc.apply(maxSizeElement), maxSizeElement, lengthAllElement / countElement});
    }

    @Override
    public void refreshStats(T element) {
        if (element == null) return;
        super.refreshStats(element);
        minSizeElement = minSizeElement == null || lengthFunc.apply(element) < lengthFunc.apply(minSizeElement)
                ? element : minSizeElement;
        maxSizeElement = maxSizeElement == null || lengthFunc.apply(element) > lengthFunc.apply(maxSizeElement)
                ? element : maxSizeElement;
    }
}
