package info.kgeorgiy.ja.rozhko.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HexFormat;

public class Printer {
    public static int bytesCount = 0;

    public static void printRightAns(byte[] ans, String fileName, BufferedWriter writer) {
        try {
            writer.write(HexFormat.of().formatHex(ans) + " " + fileName);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Problems with output file:" + e.getMessage());
        }
    }

    public static void printOnError(String fileName, BufferedWriter writer) {
        try {
            writer.write("0".repeat(bytesCount * 2) + " " + fileName);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Problems with output file:" + e.getMessage());
        }
    }
}
