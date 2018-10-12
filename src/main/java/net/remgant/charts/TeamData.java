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
