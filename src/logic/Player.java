package logic;

import java.util.ArrayList;
import java.util.List;
import logic.Card.Rank;

public class Player {
    public final String name;
    public final List<Card> hand = new ArrayList();
    public final boolean isHuman;

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
    }

    public void addCard(Card card) {
        this.hand.add(card);
    }

    public void sortHand(Card.Suit trump) {
        this.hand.sort((a, b) -> {
            if (a.rank == Rank.JOKER && b.rank != Rank.JOKER) {
                return -1;
            } else if (a.rank != Rank.JOKER && b.rank == Rank.JOKER) {
                return 1;
            } else if (a.suit == trump && b.suit != trump) {
                return -1;
            } else if (a.suit != trump && b.suit == trump) {
                return 1;
            } else {
                return a.suit != b.suit ? a.suit.ordinal() - b.suit.ordinal() : a.rank.ordinal() - b.rank.ordinal();
            }
        });
    }
}
