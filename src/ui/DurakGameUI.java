package ui;

import logic.Card;
import logic.Game;
import logic.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class DurakGameUI extends JFrame {
    private final Game game;
    private GameTimer gameTimer; // Новый класс для таймера
    private Timer uiUpdateTimer; // Таймер для обновления UI
    private JLabel timerLabel; // Лейбл для отображения времени

    private JLabel trumpLabel;
    private JLabel currentPlayerLabel;
    private JPanel tablePanel;
    private JPanel handPanel;
    private JScrollPane handScrollPane;
    private JButton takeButton;
    private JButton passButton;  // Кнопка "Пас" для подкидывания
    private JLabel statusLabel;
    private JButton[] cardButtons;
    private JTextArea historyArea;

    public DurakGameUI(int deckSize, boolean isTesterMode) {
        game = new Game(deckSize, isTesterMode);

        setTitle("Подкидной дурак");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            // Игнорируем, если нет иконки
        }

        initializeUI();
        updateGUI();

        if (!game.getPlayers().get(game.getCurrentThrowerIndex()).isHuman) {
            game.botTurn();
            updateGUI();
        }

        setSize(1200, 800);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        // Запуск таймера только в игровом режиме (не в testerMode)
        if (!isTesterMode) {
            gameTimer = new GameTimer(300000, () -> { // 300000 мс = 5 минут
                JOptionPane.showMessageDialog(this, "Вы медленный *подмигнул*", "Время вышло!", JOptionPane.WARNING_MESSAGE);
                System.exit(0); // Завершаем игру
            });
            gameTimer.start();

            // Таймер для обновления UI каждую секунду
            uiUpdateTimer = new Timer(1000, e -> updateTimerLabel());
            uiUpdateTimer.start();
        }

        if (isTesterMode) {
            new Thread(game::runTesterMode).start();
        }
    }

    private void updateTimerLabel() {
        if (gameTimer != null) {
            timerLabel.setText("Оставшееся время: " + gameTimer.getRemainingTimeFormatted());
        }
    }

    private void initializeUI() {
        JPanel topPanel = new JPanel(new GridLayout(3, 1)); // Изменено на 3 строки для таймера
        topPanel.setBackground(new Color(34, 139, 34));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        trumpLabel = new JLabel("Козырная масть: " + game.getTrump(), SwingConstants.CENTER);
        trumpLabel.setFont(new Font("Arial", Font.BOLD, 18));
        trumpLabel.setForeground(Color.WHITE);

        currentPlayerLabel = new JLabel("Атакующий: " + game.getPlayers().get(game.getAttackerIndex()).name +
                ", Защитник: " + game.getPlayers().get(game.getDefenderIndex()).name, SwingConstants.CENTER);
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentPlayerLabel.setForeground(Color.WHITE);

        timerLabel = new JLabel("Оставшееся время: 05:00", SwingConstants.CENTER); // Начальное значение
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.YELLOW);

        topPanel.add(trumpLabel);
        topPanel.add(currentPlayerLabel);
        topPanel.add(timerLabel); // Добавлен лейбл таймера
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(50, 150, 50));

        tablePanel = new JPanel();
        tablePanel.setBackground(new Color(60, 179, 113));
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3), "Стол", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        historyArea = new JTextArea(20, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBackground(Color.LIGHT_GRAY);
        historyArea.setBorder(BorderFactory.createTitledBorder("История ходов"));
        JScrollPane historyScroll = new JScrollPane(historyArea);
        centerPanel.add(historyScroll, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(34, 139, 34));

        handPanel = new JPanel();
        handPanel.setBackground(new Color(60, 179, 113));
        handPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Ваша рука", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        handPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        handScrollPane = new JScrollPane(handPanel);
        handScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        handScrollPane.setBorder(BorderFactory.createEmptyBorder());
        bottomPanel.add(handScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(34, 139, 34));

        takeButton = new JButton("Взять карты");
        takeButton.setFont(new Font("Arial", Font.BOLD, 14));
        takeButton.setBackground(Color.RED);
        takeButton.setForeground(Color.WHITE);
        takeButton.addActionListener(e -> {
            game.takeCards();
            game.botTurn();
            updateGUI();
            checkWin();
        });
        buttonPanel.add(takeButton);

        passButton = new JButton("Пас");
        passButton.setFont(new Font("Arial", Font.BOLD, 14));
        passButton.setBackground(Color.BLUE);
        passButton.setForeground(Color.WHITE);
        passButton.addActionListener(e -> {
            game.nextThrower();
            game.botTurn();
            updateGUI();
            checkWin();
        });
        buttonPanel.add(passButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.YELLOW);
        buttonPanel.add(statusLabel);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateGUI() {
        currentPlayerLabel.setText("Атакующий: " + game.getPlayers().get(game.getAttackerIndex()).name +
                ", Защитник: " + game.getPlayers().get(game.getDefenderIndex()).name);
        tablePanel.removeAll();
        List<Card> table = game.getTable();
        for (int i = 0; i < table.size(); i++) {
            Card card = table.get(i);
            JButton cardButton = createCardButton(card, false);
            tablePanel.add(cardButton);
        }
        tablePanel.revalidate();
        tablePanel.repaint();

        handPanel.removeAll();
        Player human = game.getHumanPlayer();
        if (human != null && !game.isTesterMode()) {
            cardButtons = new JButton[human.hand.size()];
            for (int i = 0; i < human.hand.size(); i++) {
                Card card = human.hand.get(i);
                JButton cardButton = createCardButton(card, true);
                if (game.isInitialAttack() && human == game.getPlayers().get(game.getAttackerIndex())) {
                    cardButton.setBackground(Color.YELLOW);
                } else if (game.isDefending() && human == game.getPlayers().get(game.getDefenderIndex())) {
                    Card lastOnTable = game.getTable().get(game.getTable().size() - 1);
                    if (card.beats(lastOnTable, game.getTrump())) {
                        cardButton.setBackground(Color.YELLOW);
                    } else {
                        cardButton.setBackground(Color.GRAY);
                    }
                } else if (game.isThrowing() && human != game.getPlayers().get(game.getDefenderIndex())) {
                    if (game.canThrowCard(card)) {
                        cardButton.setBackground(Color.YELLOW);
                    } else {
                        cardButton.setBackground(Color.WHITE);
                    }
                } else {
                    cardButton.setBackground(Color.WHITE);
                }
                int index = i;
                cardButton.addActionListener(e -> playCard(index));
                handPanel.add(cardButton);
                cardButtons[i] = cardButton;
            }
        }
        handPanel.revalidate();
        handPanel.repaint();

        historyArea.setText(game.getGameHistory());

        takeButton.setEnabled(game.isDefending() && game.getPlayers().get(game.getCurrentThrowerIndex()).isHuman &&
                game.getPlayers().get(game.getCurrentThrowerIndex()) == game.getPlayers().get(game.getDefenderIndex()));

        passButton.setEnabled(game.isThrowing() && game.getPlayers().get(game.getCurrentThrowerIndex()).isHuman);

        if (game.isThrowing() && game.getPlayers().get(game.getCurrentThrowerIndex()).isHuman &&
                !game.canThrowAnyCard(game.getPlayers().get(game.getCurrentThrowerIndex()))) {
            game.nextThrower();
            updateGUI();
            if (!game.getPlayers().get(game.getCurrentThrowerIndex()).isHuman) {
                game.botTurn();
                updateGUI();
            }
        }
    }

    private JButton createCardButton(Card card, boolean clickable) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(80, 120)); // Размер карты
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setFocusPainted(false);

        // Пытаемся загрузить изображение
        String imagePath = getCardImagePath(card);
        try {
            BufferedImage img = ImageIO.read(getClass().getResource(imagePath));
            ImageIcon icon = new ImageIcon(img.getScaledInstance(70, 100, Image.SCALE_SMOOTH));
            button.setIcon(icon);
            button.setText(""); // Убираем текст, если есть изображение
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            // Если изображение не найдено, используем текст с эмодзи и цветами
            String suitEmoji = getSuitEmoji(card.suit);
            String rankStr = getRankShort(card.rank);
            button.setText("<html><center><font size='5'>" + rankStr + "<br>" + suitEmoji + "</font></center></html>");
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setForeground(getSuitColor(card.suit));
            button.setBackground(Color.WHITE);
        }

        if (!clickable) {
            button.setEnabled(false); // Карты на столе не кликабельны
        }
        return button;
    }

    // Метод для получения эмодзи масти
    private String getSuitEmoji(Card.Suit suit) {
        if (suit == Card.Suit.ПИК) return "♠";
        if (suit == Card.Suit.ЧЕРВЕЙ) return "♥";
        if (suit == Card.Suit.БУБЕН) return "♦";
        if (suit == Card.Suit.ТРЕФ) return "♣";
        return ""; // Для джокера
    }

    // Метод для получения цвета масти
    private Color getSuitColor(Card.Suit suit) {
        if (suit == Card.Suit.ЧЕРВЕЙ || suit == Card.Suit.БУБЕН) return Color.RED;
        return Color.BLACK;
    }

    // Метод для короткого обозначения ранга
    private String getRankShort(Card.Rank rank) {
        switch (rank) {
            case ДВОЙКА: return "2";
            case ТРОЙКА: return "3";
            case ЧЕТВЕРКА: return "4";
            case ПЯТЕРКА: return "5";
            case ШЕСТЕРКА: return "6";
            case СЕМЕРКА: return "7";
            case ВОСЬМЕРКА: return "8";
            case ДЕВЯТКА: return "9";
            case ДЕСЯТКА: return "10";
            case ВАЛЕТ: return "В";
            case ДАМА: return "Д";
            case КОРОЛЬ: return "К";
            case ТУЗ: return "Т";
            case JOKER: return "J";
            default: return "";
        }
    }

    // Метод для получения пути к изображению карты
    private String getCardImagePath(Card card) {
        if (card.suit == null) return "/resources/cards/joker.png"; // Джокер
        String rankStr = card.rank.name().toLowerCase();
        String suitStr = card.suit.name().toLowerCase();
        // Преобразование русских мастей в английские (если нужно)
        if (suitStr.equals("пики")) suitStr = "spades";
        else if (suitStr.equals("черви")) suitStr = "hearts";
        else if (suitStr.equals("бубны")) suitStr = "diamonds";
        else if (suitStr.equals("трефы")) suitStr = "clubs";
        // Преобразование рангов
        if (rankStr.equals("валет")) rankStr = "jack";
        else if (rankStr.equals("дама")) rankStr = "queen";
        else if (rankStr.equals("король")) rankStr = "king";
        else if (rankStr.equals("туз")) rankStr = "ace";
        return "/resources/cards/" + rankStr + "_of_" + suitStr + ".png";
    }

    private void playCard(int index) {
        if (game.playCard(index)) {
            game.botTurn();
            updateGUI();
            checkWin();
        } else {
            statusLabel.setText("Неверный ход!");
        }
    }

    private void checkWin() {
        if (game.isGameOver()) {
            String winner = game.getWinner();
            statusLabel.setText(winner + " победил!");
            handPanel.removeAll();
            takeButton.setEnabled(false);
            passButton.setEnabled(false);
            if (gameTimer != null) gameTimer.stop();
            if (uiUpdateTimer != null) uiUpdateTimer.stop();
        }
    }
}