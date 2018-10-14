/*
  Copyright (C) 2018 Jeffrey D. Remillard <jdr@remgant.net>

  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  later version.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program. If not,
  see <https://www.gnu.org/licenses/>.
 */

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
