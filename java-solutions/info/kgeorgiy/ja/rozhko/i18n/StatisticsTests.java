package info.kgeorgiy.ja.rozhko.i18n;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsTests {

    @Test
    public void test1() throws IOException {
        TextStatistics.main(new String[]{"en_US", "en_US",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\DearRustam.txt",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out.txt"});
        Assert.assertEquals(-1, Files.mismatch(
                Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out.txt"),
                Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\ansTest1.txt")));
    }

    @Test
    public void test2() throws IOException {
        TextStatistics.main(new String[]{"en_US", "ru_RU",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\DearRustam.txt",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out2.txt"});
        String currentAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out2.txt"));
        String testAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\ansTest2.txt"));
        Assert.assertEquals(currentAns, testAns);
    }

    @Test
    public void test3() throws IOException {
        TextStatistics.main(new String[]{"fr_FR", "ru_RU",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\franceText.txt",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out3.txt"});
        String currentAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out3.txt"));
        String testAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\ansTest3.txt"));
        Assert.assertEquals(currentAns, testAns);
    }

    @Test
    public void test4() throws IOException {
        TextStatistics.main(new String[]{"th", "ru_RU",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\thaiText.txt",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out4.txt"});
        String currentAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out4.txt"));
        String testAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\ansTest4.txt"));
        Assert.assertEquals(currentAns, testAns);
    }


    @Test
    public void test6() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(() -> {
            try {
                test1();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        executorService.submit(() -> {
            try {
                test2();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        executorService.submit(() -> {
            try {
                test3();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        executorService.submit(() -> {
            try {
                test4();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    public void test7() throws IOException {
        TextStatistics.main(new String[]{"ja_JP", "ru_RU",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\japanBibly.txt",
                "C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out6.txt"});
        String currentAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\out4.txt"));
        String testAns = Files.readString(Path.of("C:\\All\\Stu\\JavaAdvanced\\java-advanced\\java-solutions\\info\\kgeorgiy\\ja\\rozhko\\i18n\\ansTest4.txt"));
        Assert.assertEquals(currentAns, testAns);
    }
}

