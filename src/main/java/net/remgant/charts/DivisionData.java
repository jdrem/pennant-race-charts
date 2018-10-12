package net.remgant.charts;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class DivisionData {
    private String longName;
    private String fileName;
    private String shortName;
    private List<String> members;

    public DivisionData(String longName,  String shortName, String fileName, List<String> members) {
        this.longName = longName;
        this.fileName = fileName;
        this.shortName = shortName;
        this.members = members;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getMembers() {
        return members;
    }
}
