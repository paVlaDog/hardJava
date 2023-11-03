package info.kgeorgiy.ja.rozhko.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

public class Walk {
    public static void main(String[] args) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            Printer.bytesCount = String.format("%s", HexFormat.of().formatHex(md.digest())).length();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Incorrect algorithm" + e.getMessage());
            return;
        }
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.out.println("You need input 2 arguments");
            return;
        }
        try (BufferedReader startReader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            Path outputPath = Path.of(args[1]);
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
                System.out.println("Output error:" + e.getMessage());
            } catch (InvalidPathException e) {
                System.out.println("Invalid path input file:" + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Input error:" + e.getMessage());
        } catch (InvalidPathException e) {
            System.out.println("Invalid path input file:" + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Access denied" + e.getMessage());
        }
    }
}
