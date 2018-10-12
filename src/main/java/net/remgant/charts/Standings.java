package net.remgant.charts;

import java.time.LocalDate;

@SuppressWarnings("WeakerAccess")
public class Standings {
    private LocalDate date;
    private int wins;
    private int losses;

    public Standings(LocalDate date, int wins, int losses) {
        this.date = date;
        this.wins = wins;
        this.losses = losses;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    @Override
    public String toString() {
        return "Standings{" +
                "date=" + date +
                ", wins=" + wins +
                ", losses=" + losses +
                '}';
    }
}
