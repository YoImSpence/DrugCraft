package com.spence.drugcraft.casino;

public class Card {
    private final String suit;
    private final String rank;
    private final int value;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = calculateValue(rank);
    }

    private int calculateValue(String rank) {
        if (rank.equals("Ace")) return 11;
        if (rank.equals("King") || rank.equals("Queen") || rank.equals("Jack")) return 10;
        return Integer.parseInt(rank);
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}