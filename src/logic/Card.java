package logic;

public class Card {
    public final Rank rank;
    public final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String toString() {
        if (this.suit == null) {
            return "Джокер";
        } else {
            String var10000 = String.valueOf(this.rank);
            return var10000 + " " + String.valueOf(this.suit);
        }
    }

    public boolean beats(Card other, Suit trump) {
        if (this.rank == Card.Rank.JOKER) {
            return true;
        } else if (other.rank == Card.Rank.JOKER) {
            return false;
        } else if (this.suit == trump && other.suit != trump) {
            return true;
        } else if (this.suit != trump && other.suit == trump) {
            return false;
        } else if (this.suit != other.suit) {
            return false;
        } else {
            return this.rank.ordinal() > other.rank.ordinal();
        }
    }

    public static enum Suit {
        ПИК,
        ЧЕРВЕЙ,
        БУБЕН,
        ТРЕФ;

        private Suit() {
        }
    }

    public static enum Rank {
        ДВОЙКА,
        ТРОЙКА,
        ЧЕТВЕРКА,
        ПЯТЕРКА,
        ШЕСТЕРКА,
        СЕМЕРКА,
        ВОСЬМЕРКА,
        ДЕВЯТКА,
        ДЕСЯТКА,
        ВАЛЕТ,
        ДАМА,
        КОРОЛЬ,
        ТУЗ,
        JOKER;

        private Rank() {
        }
    }
}