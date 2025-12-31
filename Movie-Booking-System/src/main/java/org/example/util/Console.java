package org.example.util;

import java.util.Scanner;

public class Console {
    private static final Scanner sc = new Scanner(System.in);

    public static void println(String s) { System.out.println(s); }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (Exception e) {
                System.out.println("請輸入整數");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (Exception e) {
                System.out.println("請輸入數字");
            }
        }
    }
}
