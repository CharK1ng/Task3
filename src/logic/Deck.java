package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logic.Card.Rank;
import logic.Card.Suit;

public class Deck {
    private final List<Card> cards = new ArrayList();

    public Deck(int size) {
        if (size == 36) {
            for(Card.Suit suit : Suit.values()) {
                for(Card.Rank rank : Rank.values()) {
                    if (rank.ordinal() >= Rank.ШЕСТЕРКА.ordinal() && rank != Rank.JOKER) {
                        this.cards.add(new Card(rank, suit));
                    }
                }
            }
        } else if (size == 52) {
            for(Card.Suit suit : Suit.values()) {
                for(Card.Rank rank : Rank.values()) {
                    if (rank != Rank.JOKER) {
                        this.cards.add(new Card(rank, suit));
                    }
                }
            }
        }

        this.shuffle();
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
    }

    public Card deal() {
        return (Card)this.cards.remove(this.cards.size() - 1);
    }

    public boolean isEmpty() {
        return this.cards.isEmpty();
    }
}
