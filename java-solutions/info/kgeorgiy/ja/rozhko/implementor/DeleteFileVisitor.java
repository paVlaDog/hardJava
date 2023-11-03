package info.kgeorgiy.ja.rozhko.implementor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A fileVisitor who deletes files.
 *
 * @see SimpleFileVisitor
 */

// :NOTE: задокументирован не весь класс -- конструкторы и throws
public class DeleteFileVisitor extends SimpleFileVisitor<Path> {
    /**
     * The overridden command visitFile, removes the element and moves on, analogous
     * {@link SimpleFileVisitor#visitFile(Object, BasicFileAttributes)}
     *
     * @see SimpleFileVisitor#visitFile(Object, BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    /**
     * The overridden command postVisitDirectory, removes the element at the end of the tour, analogous
     * {@link SimpleFileVisitor#postVisitDirectory(Object, IOException)}
     *
     * @see SimpleFileVisitor#postVisitDirectory(Object, IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException ioExec) throws IOException {
        if (ioExec == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        } else {
            throw ioExec;
        }
    }
}