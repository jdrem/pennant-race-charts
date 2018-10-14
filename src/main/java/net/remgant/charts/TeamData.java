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

import java.awt.*;

@SuppressWarnings("WeakerAccess")
public class TeamData {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String abbrev;
    private String abbrev2;
    private String name;
    private Color color;

    public TeamData(String abbrev, String abbrev2, String name, Color color) {
        this.abbrev = abbrev;
        this.abbrev2 = abbrev2;
        this.name = name;
        this.color = color;
    }

    public String getAbbrev2() {
        return abbrev2;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
