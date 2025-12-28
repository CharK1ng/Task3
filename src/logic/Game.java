package logic;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Deck deck;
    private final Card.Suit trump;
    private final List<Player> players;
    private int attackerIndex = 0;
    private int defenderIndex = 1;
    private int currentThrowerIndex;
    private boolean isInitialAttack = true;
    private boolean isDefending = false;
    private boolean isThrowing = false;
    private final List<Card> table = new ArrayList<>();
    private final boolean isTesterMode;
    private final StringBuilder gameHistory = new StringBuilder();

    public Game(int deckSize, boolean isTesterMode) {
        this.isTesterMode = isTesterMode;
        deck = new Deck(deckSize);
        Card trumpCard = deck.deal();
        trump = (trumpCard.suit == null) ? deck.deal().suit : trumpCard.suit;

        players = new ArrayList<>();
        if (isTesterMode) {
            players.add(new Player("Бот1", false));
            players.add(new Player("Бот2", false));
        } else {
            players.add(new Player("Бот", false));
            players.add(new Player("Игрок", true));
        }

        // Раздача 6 случайных карт каждому игроку
        for (int i = 0; i < 6; i++) {
            for (Player player : players) {
                if (!deck.isEmpty()) {
                    player.addCard(deck.deal());
                }
            }
        }

        // Сортировка рук
        for (Player player : players) {
            player.sortHand(trump);
        }

        gameHistory.append("Игра началась. Козырь: ").append(trump).append("\n");
        startNewRound();
    }

    public void startNewRound() {
        table.clear();
        isInitialAttack = true;
        isDefending = false;
        isThrowing = false;
        currentThrowerIndex = attackerIndex;
        gameHistory.append("Новый раунд. Атакующий: ").append(players.get(attackerIndex).name)
                .append(", Защитник: ").append(players.get(defenderIndex).name).append("\n");
    }

    public boolean canThrowCard(Card card) {
        return table.stream().anyMatch(c -> c.rank == card.rank);
    }

    public boolean canThrowAnyCard(Player player) {
        return player.hand.stream().anyMatch(this::canThrowCard);
    }

    public boolean playCard(int cardIndex) {

        Player currentPlayer = (Player) players.get(currentThrowerIndex);
        if (!currentPlayer.isHuman || cardIndex >= currentPlayer.hand.size()) return false;

        Card playedCard = currentPlayer.hand.get(cardIndex);
        if (isInitialAttack && currentPlayer == players.get(attackerIndex)) {
            currentPlayer.hand.remove(cardIndex);
            table.add(playedCard);
            gameHistory.append(currentPlayer.name).append(" атаковал: ").append(playedCard).append("\n");
            isInitialAttack = false;
            isDefending = true;
            currentThrowerIndex = defenderIndex;
        } else if (isDefending && currentPlayer == players.get(defenderIndex)) {
            Card lastOnTable = table.get(table.size() - 1);
            if (playedCard.beats(lastOnTable, trump)) {
                currentPlayer.hand.remove(cardIndex);
                table.add(playedCard);
                gameHistory.append(currentPlayer.name).append(" побил: ").append(playedCard).append("\n");
                isDefending = false;
                isThrowing = true;
                currentThrowerIndex = attackerIndex;
            } else {
                return false;
            }
        } else if (isThrowing && currentPlayer != players.get(defenderIndex)) {
            if (canThrowCard(playedCard)) {
                currentPlayer.hand.remove(cardIndex);
                table.add(playedCard);
                gameHistory.append(currentPlayer.name).append(" подкинул: ").append(playedCard).append("\n");
                isThrowing = false;
                isDefending = true;
                currentThrowerIndex = defenderIndex;
            } else {
                return false;
            }
        } else {
            return false;
        }

        // Убрал drawCards(currentPlayer); - добор теперь только в конце раунда
        return true;
    }

    public void takeCards() {
        Player currentPlayer = (Player) players.get(currentThrowerIndex);
        if (!isDefending || currentPlayer != players.get(defenderIndex)) return;
        currentPlayer.hand.addAll(table);
        table.clear();
        gameHistory.append(currentPlayer.name).append(" взял карты\n");
        // После взятия карт атакующий ходит снова (роли не меняются)
        startNewRound();
        // Добавил добор карт после взятия (в конце раунда)
        for (Player player : players) {
            drawCards(player);
        }
    }

    public void nextThrower() {
        currentThrowerIndex = (currentThrowerIndex + 1) % players.size();
        if (currentThrowerIndex == defenderIndex) {
            table.clear();
            nextRound();
            return;
        }
        if (isThrowing && players.get(currentThrowerIndex).isHuman) {
            return;
        }
    }

    private void nextRound() {
        attackerIndex = (attackerIndex + 1) % players.size();
        defenderIndex = (attackerIndex + 1) % players.size();
        // Добор карт после конца раунда
        for (Player player : players) {
            drawCards(player);
        }
        startNewRound();
    }

    public void botTurn() {
        while (!isGameOver() && !((Player) players.get(currentThrowerIndex)).isHuman && !((Player) players.get(currentThrowerIndex)).hand.isEmpty()) {
            Player bot = (Player) players.get(currentThrowerIndex);
            if (isInitialAttack && bot == players.get(attackerIndex)) {
                if (bot.hand.isEmpty()) return;
                Card cardToPlay = bot.hand.get(0);
                bot.hand.remove(0);
                table.add(cardToPlay);
                gameHistory.append(bot.name).append(" атаковал: ").append(cardToPlay).append("\n");
                isInitialAttack = false;
                isDefending = true;
                currentThrowerIndex = defenderIndex;
            } else if (isDefending && bot == players.get(defenderIndex)) {
                Card lastOnTable = table.get(table.size() - 1);
                boolean beat = false;
                for (int i = 0; i < bot.hand.size(); i++) {
                    if (bot.hand.get(i).beats(lastOnTable, trump)) {
                        Card beatingCard = bot.hand.remove(i);
                        table.add(beatingCard);
                        gameHistory.append(bot.name).append(" побил: ").append(beatingCard).append("\n");
                        beat = true;
                        isDefending = false;
                        isThrowing = true;
                        currentThrowerIndex = attackerIndex;
                        break;
                    }
                }
                if (!beat) {
                    bot.hand.addAll(table);
                    table.clear();
                    gameHistory.append(bot.name).append(" взял карты\n");
                    startNewRound();
                    // Добавил добор карт после взятия (в конце раунда)
                    for (Player player : players) {
                        drawCards(player);
                    }
                    return;
                }
            } else if (isThrowing && bot != players.get(defenderIndex)) {
                Card cardToThrow = null;
                for (Card card : bot.hand) {
                    if (canThrowCard(card)) {
                        cardToThrow = card;
                        break;
                    }
                }
                if (cardToThrow != null) {
                    bot.hand.remove(cardToThrow);
                    table.add(cardToThrow);
                    gameHistory.append(bot.name).append(" подкинул: ").append(cardToThrow).append("\n");
                    isThrowing = false;
                    isDefending = true;
                    currentThrowerIndex = defenderIndex;
                } else {
                    nextThrower();
                    return;
                }
            }
            // Убрал drawCards(bot); - добор теперь только в конце раунда
        }
    }

    private void drawCards(Player player) {
        while (player.hand.size() < 6 && !deck.isEmpty()) {
            player.addCard(deck.deal());
        }
        player.sortHand(trump);
    }

    public void runTesterMode() {
        while (!isGameOver()) {
            botTurn();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gameHistory.append(getWinner() + " победил!\n");
    }

    public boolean isGameOver() {
        for (Player player : players) {
            if (player.hand.isEmpty()) return true;
        }
        return false;
    }

    public String getWinner() {
        for (Player player : players) {
            if (player.hand.isEmpty()) return player.name;
        }
        return null;
    }

    // Геттеры для UI
    public Card.Suit getTrump() { return trump; }
    public List<Player> getPlayers() { return players; }
    public int getAttackerIndex() { return attackerIndex; }
    public int getDefenderIndex() { return defenderIndex; }
    public int getCurrentThrowerIndex() { return currentThrowerIndex; }
    public List<Card> getTable() { return table; }
    public String getGameHistory() { return gameHistory.toString(); }
    public boolean isInitialAttack() { return isInitialAttack; }
    public boolean isDefending() { return isDefending; }
    public boolean isThrowing() { return isThrowing; }
    public boolean isTesterMode() { return isTesterMode; }
    public Player getHumanPlayer() {
        return (Player) players.stream().filter(p -> p.isHuman).findFirst().orElse(null);
    }
}