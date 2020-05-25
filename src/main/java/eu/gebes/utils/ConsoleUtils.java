package eu.gebes.utils;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleUtils {

    final private static String OS = System.getProperty("os.name");

    public static void clear() {

        try {
            if (OS.toLowerCase().contains("windows"))
                Runtime.getRuntime().exec("cls");
            else
                Runtime.getRuntime().exec("clear");
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static void printLine() {
        System.out.println("=".repeat(50));
    }

    public static int scanIntRange(final String message, int from, int to) {

        if (from > to) {
            int temp = to;
            to = from;
            from = temp;
        }

        int selection = from - 1;
        do {
            System.out.print(message);
            selection = new Scanner(System.in).nextInt();
        } while (!(selection >= from && selection <= to));
        return selection;
    }

}
