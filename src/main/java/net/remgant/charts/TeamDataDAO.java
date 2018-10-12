package net.remgant.charts;

import java.util.List;

public interface TeamDataDAO {

    List<DivisionData> getDivisionDataForYear(int year);
    TeamData getTeamData(String abbrev, int year);
    List<Standings> getStandingsForTeamAndYear(String abbrev, int year);
    void loadDataForYear(int year);
}
