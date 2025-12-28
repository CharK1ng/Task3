package ui;

import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Устанавливаем системный стиль для графических окон
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Используем Scanner для работы с терминалом
        Scanner scanner = new Scanner(System.in, "UTF-8");

        System.out.println("========================================");
        System.out.println("   WELCOME TO THE GOMO GOMO CLUB      ");
        System.out.println("========================================");
        System.out.print("Введите команду: ");

        String input = scanner.nextLine();

        if (input.trim().equalsIgnoreCase("Хочу играть!")) {
            System.out.println("\n[СИСТЕМА]: Доступ разрешен.");
            System.out.println("----------------------------------------");
            System.out.println("Выберите размер колоды:");
            System.out.println("1. 36 карт (Классика)");
            System.out.println("2. 52 карты (Полная)");
            System.out.print("Ваш выбор (1 или 2): ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                choice = 1; // По умолчанию 36
            }

            int deckSize = (choice == 2) ? 52 : 36;

            System.out.println("\n[СИСТЕМА]: Раздаем колоду из " + deckSize + " карт...");
            System.out.println("[СИСТЕМА]: Удачи за столом!");

            // Запускаем графический интерфейс
            SwingUtilities.invokeLater(() -> {
                // Режим тестера (isTesterMode) всегда false
                new DurakGameUI(deckSize, false);
            });

        } else {
            System.out.println("\n[СИСТЕМА]: Извините, сегодня столы накрыты только для тех, кто действительно хочет играть. Прости малая *подмигнул*");
            System.exit(0);
        }
    }
}
