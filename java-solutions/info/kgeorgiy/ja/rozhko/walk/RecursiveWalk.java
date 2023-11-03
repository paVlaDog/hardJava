package info.kgeorgiy.ja.rozhko.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class RecursiveWalk {
    public static void main(String[] args) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            Printer.bytesCount = md.digest().length;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Incorrect algorithm" + e.getMessage());
            return;
        }
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.out.println("You need input 2 arguments");
            return;
        }

        // Path input check
        Path inputPath;
        try {
            inputPath = Path.of(args[0]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path input file:" + e.getMessage());
            return;
        }

        // Path output check

        Path outputPath;
        try {
            outputPath = Path.of(args[1]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path output file:" + e.getMessage());
            return;
        }

        try {

            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
        } catch (InvalidPathException e) {
            System.out.println("Invalid path output file:" + e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("Problems with directories output files:" + e.getMessage());
            return;
        }


        try (BufferedReader startReader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            try (BufferedWriter startWritter = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                String fileOrDirName;
                HashFileVisitor hashFIleVisitor = new HashFileVisitor(startWritter, md);
                while ((fileOrDirName = startReader.readLine()) != null) {
                    try {
                        Files.walkFileTree(Path.of(fileOrDirName), hashFIleVisitor);
                    } catch (IOException | IllegalArgumentException e) {
                        Printer.printOnError(fileOrDirName, startWritter);
                    }
                }
            } catch (IOException e) {
                // :NOTE: здесь все еще startReader.readLine() ловится
                System.out.println("Output error:" + e.getMessage());
            }
        } catch (IOException e) {
            // :NOTE: здесь ловится и открытие входного файла, и создание родительских папок для выходного
            // всякие SecurityException пропустил
            System.out.println("Input error:" + e.getMessage());
        }
    }
}