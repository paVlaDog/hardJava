package info.kgeorgiy.ja.rozhko.i18n;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A class that displays statistics for the given text.
 *
 * Accepts input locale and output locale in the format country_region_variant
 * a file with a test for processing and for output
 *
 * @author paVlaDog
 */



public class TextStatistics {
    /**
     * Receives data and passes it to getStatistics
     *
     * @param args - Accepts input locale and output locale in the format country_region_variant
     * a file with a test for processing and for output
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Args is null");
        } else if (IntStream.range(0, 4).anyMatch(i -> Objects.isNull(args[i]))) {
            System.err.println("Args[i] is null");
        } else if (args.length < 4) {
            System.err.println("Length of args < 4");
        } else {
            Locale inputLocale = parseLocale(args[0]);
            Locale outputLocale = parseLocale(args[1]);
            String inputFilepath = args[2];
            String outputFilepath = args[3];
            try {
                String inputString = Files.readString(Path.of(inputFilepath), StandardCharsets.UTF_8);
                TextStatistics textStatistics = new TextStatistics();
                textStatistics.getStatistics(inputFilepath, inputString, Path.of(outputFilepath), inputLocale, outputLocale);
            } catch (IOException e) {
                System.err.println("Input error" + e.getMessage());
            } catch (InvalidPathException e) {
                System.err.println("Invalid path of input file" + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid argument: " + e.getMessage());
            }
        }
    }

    /**
     *
     * @param inputFilepath - input file name
     * @param str - input line
     * @param outputPath - output path
     * @param strLocale - Input locale in format country_region_varian
     * @param outputLocale - Output locale in format country_region_varian
     */
    public void getStatistics(String inputFilepath, String str, Path outputPath, Locale strLocale, Locale outputLocale) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("info/kgeorgiy/ja/rozhko/i18n/MessageBundle",
                outputLocale);
        ReinforcedStats<String> sentenceStats = new ReinforcedStats<>(messageBundle.getString("sentenceStats"),
                Collator.getInstance(strLocale)::compare, (el) -> (double)el.length(), outputLocale);
        ReinforcedStats<String> wordStats = new ReinforcedStats<>(messageBundle.getString("wordStats"),
                Collator.getInstance(strLocale)::compare, (el) -> (double)el.length(), outputLocale);
        StandardStats<Double> numberStats = new StandardStats<>(messageBundle.getString("numberStats"),
                Double::compare, Function.identity(), outputLocale);
        StandardStats<Long> dateStats = new StandardStats<>(messageBundle.getString("dateStats"),
                Long::compare, Long::doubleValue, outputLocale);
        StandardStats<Double> currencyStats = new StandardStats<>(messageBundle.getString("currencyStats"),
                Double::compare, Function.identity(), outputLocale);

        DateFormat dateShortFormat = DateFormat.getDateInstance(DateFormat.SHORT, strLocale);
        DateFormat dateMediumFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, strLocale);
        DateFormat dateLongFormat = DateFormat.getDateInstance(DateFormat.LONG, strLocale);
        DateFormat dateFullFormat = DateFormat.getDateInstance(DateFormat.FULL, strLocale);
        NumberFormat numberFormat = NumberFormat.getInstance(strLocale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(strLocale);

        BreakIterator it = BreakIterator.getSentenceInstance(strLocale);
        it.setText(str);
        for (int prevInd = it.first(), ind = it.next(); ind != BreakIterator.DONE; prevInd = ind, ind = it.next()) {
            sentenceStats.refreshStats(str.substring(prevInd, ind).trim().replaceAll("[\"\r\n]", ""));
        }
        it = BreakIterator.getWordInstance(strLocale);
        it.setText(str);
        for (int prevInd = it.first(), ind = it.next(); ind != BreakIterator.DONE; prevInd = ind, ind = it.next()) {
            String word = str.substring(prevInd, ind).trim();
            if (isWord(word)) wordStats.refreshStats(word);
            refreshData(numberFormat, str, ind, numberStats);
            refreshData(dateShortFormat, str, ind, dateStats);
            refreshData(dateMediumFormat, str, ind, dateStats);
            refreshData(dateLongFormat, str, ind, dateStats);
            refreshData(dateFullFormat, str, ind, dateStats);
            refreshData(currencyFormat, str, ind, currencyStats);
        }
        try {
            Files.writeString(outputPath, MessageFormat.format(messageBundle.getString("allStats"),
                    System.lineSeparator(), inputFilepath, sentenceStats.getCount(), wordStats.getCount(),
                    numberStats.getCount(), currencyStats.getCount(), dateStats.getCount()) + sentenceStats.getStats()
                            + wordStats.getStats() + numberStats.getStats() + currencyStats.getStats()
                            + dateStats.getStats(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Output error: " + e.getMessage());
        }
    }

    private void refreshData(NumberFormat numberFormat, String str, int ind, StandardStats<Double> numberStats) {
        Number number = numberFormat.parse(str, new ParsePosition(ind));
        if (number != null) numberStats.refreshStats(number.doubleValue());
    }

    private void refreshData(DateFormat dateShortFormat, String str, int ind, StandardStats<Long> dateStats) {
        Date date = dateShortFormat.parse(str, new ParsePosition(ind));
        if (date != null) dateStats.refreshStats(date.getTime());
    }

    private static Locale parseLocale(String localeStr) {
        String[] localeArgs = localeStr.split("_");
        if (localeArgs.length == 3) return (new Locale.Builder()).setLanguage(localeArgs[0]).setRegion(localeArgs[1])
                .setVariant(localeArgs[2]).build();
        else if (localeArgs.length == 2) return (new Locale.Builder()).setLanguage(localeArgs[0]).setRegion(localeArgs[1])
                .build();
        else if (localeArgs.length == 1) return (new Locale.Builder()).setLanguage(localeArgs[0]).build();
        else throw new IllegalArgumentException("Incorrect args for locale");
    }

    private boolean isWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Character.isLetter(word.charAt(i))) return true;
        }
        return false;
    }
}
