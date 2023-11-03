package info.kgeorgiy.ja.rozhko.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import static java.nio.file.FileVisitResult.CONTINUE;

public class HashFileVisitor
        extends SimpleFileVisitor<Path> {
    private final BufferedWriter startFileWritter;
    private final MessageDigest md;

    HashFileVisitor(BufferedWriter startFileWritter, MessageDigest md) {
        this.startFileWritter = startFileWritter;
        this.md = md;
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) throws IOException {
        try (InputStream currentFile = Files.newInputStream(file)) {
            byte[] bytes = new byte[1024];
            int bytesSize;
            while ((bytesSize = currentFile.read(bytes)) >= 0) {
                md.update(bytes, 0, bytesSize);
            }
            Printer.printRightAns(md.digest(), file.toString(), startFileWritter);
        } catch (IOException | IllegalArgumentException e) {
            // :NOTE: если на середине какого-то файла вылетела ошибка, при чтении следующего в md уже будут учтены байты ошибочного и хеш в итоге будет неправильный
            Printer.printOnError(file.toString(), startFileWritter);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                     IOException exec) {
        Printer.printOnError(file.toString(), startFileWritter);
        return CONTINUE;
    }
}