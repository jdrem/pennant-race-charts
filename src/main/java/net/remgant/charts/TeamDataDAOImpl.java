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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class TeamDataDAOImpl implements TeamDataDAO {
    private final JdbcTemplate jdbcTemplate;

    TeamDataDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final Map<String, String> longNameMap;
    private static final Map<String, String> fileNameMap;

    static {
        longNameMap = new HashMap<>();
        longNameMap.put("AL", "American League");
        longNameMap.put("NL", "National League");
        longNameMap.put("ALE", "AL East");
        longNameMap.put("ALC", "AL Central");
        longNameMap.put("ALW", "AL West");
        longNameMap.put("NLE", "NL East");
        longNameMap.put("NLC", "NL Central");
        longNameMap.put("NLW", "NL West");

        fileNameMap = new HashMap<>();
        fileNameMap.put("AL", "al_");
        fileNameMap.put("NL", "nl_");
        fileNameMap.put("ALE", "al_east_");
        fileNameMap.put("ALC", "al_central_");
        fileNameMap.put("ALW", "al_west_");
        fileNameMap.put("NLE", "nl_east_");
        fileNameMap.put("NLC", "nl_central_");
        fileNameMap.put("NLW", "nl_west_");
    }

    @Override
    public List<DivisionData> getDivisionDataForYear(int year, Set<String> divisionsToChart) {
        Date date = Date.valueOf(LocalDate.of(year, 7, 1));
        List<DivisionData> returnList = new ArrayList<>();
        List<Map<String, Object>> divisionList = jdbcTemplate.queryForList("select distinct league,division from teams where ? > start_date and (? < end_date or end_date is null)", date, date);

        for (Map<String, Object> div : divisionList) {
            String shortName = div.get("LEAGUE").toString() + (div.get("DIVISION") != null ? div.get("DIVISION").toString() : "");
            if (!divisionsToChart.isEmpty() && !divisionsToChart.contains(shortName))
                continue;
            String longName = longNameMap.get(shortName);
            String fileName = fileNameMap.get(shortName);
            List<String> members = new ArrayList<>();
            List<Map<String, Object>> teams;
            if (div.get("DIVISION") != null)
                teams = jdbcTemplate.queryForList("select abbrev from teams where league = ? and division = ? and ? > start_date and (? < end_date or end_date is null)", div.get("LEAGUE").toString(), div.get("DIVISION").toString(), date, date);
            else
                teams = jdbcTemplate.queryForList("select abbrev from teams where league = ? and division is null and ? > start_date and (? < end_date or end_date is null)", div.get("LEAGUE").toString(), date, date);
            for (Map<String,Object> t : teams) {
                members.add(t.get("ABBREV").toString());
            }
            returnList.add(new DivisionData(longName, fileName, members));
        }
        return returnList;
    }

    @Override
    public TeamData getTeamData(String abbrev, int year) {
        Date date = Date.valueOf(LocalDate.of(year, 7, 1));
        Map<String, Object> map = jdbcTemplate.queryForMap("select * from teams where ? > start_date and (? < end_date or end_date is null) and abbrev = ?", date, date, abbrev);
        String abbrev2 = map.get("ABBREV2").toString();
        String name = map.get("NICKNAME").toString();
        Map<String, Object> colorMap = jdbcTemplate.queryForMap("select r,g,b from team_colors where abbrev = ?", abbrev);
        Color color = new Color((int) colorMap.get("R"), (int) colorMap.get("G"), (int) colorMap.get("B"));
        return new TeamData(abbrev, abbrev2, name, color);
    }

    @Override
    public List<Standings> getStandingsForTeamAndYear(String abbrev, int year) {
        List<Map<String,Object>> m = jdbcTemplate.queryForList("select * from games where (home_team = ? or away_team = ?) and " +
                "game_date >= ? and game_date <= ?  order by game_date, game_number",
                abbrev, abbrev, Date.valueOf(LocalDate.of(year, 1, 1)), Date.valueOf(LocalDate.of(year, 12, 31)));
        AtomicInteger wins = new AtomicInteger(0);
        AtomicInteger losses = new AtomicInteger(0);
        return m.stream().map(g -> {
            String homeTeam = g.get("HOME_TEAM").toString();
            String awayTeam = g.get("AWAY_TEAM").toString();
            int hr = (int)g.get("HOME_RUNS");
            int vr = (int)g.get("AWAY_RUNS");
            if ((homeTeam.equals(abbrev) && hr > vr) || (awayTeam.equals(abbrev) && vr > hr))
                wins.incrementAndGet();
            else
                losses.incrementAndGet();
            return new Standings(((Date)g.get("GAME_DATE")).toLocalDate(), wins.get(), losses.get());
        })
                .collect(Collectors.toList());
    }

    /*
       "20170402","0","Sun","SFN","NL",1,"ARI","NL",1,5,6,53,"D","","","","PHO01",49016,203,"010011101","000003012",34,11,2,1,2,5,0,2,0,5,0,7,2,1,0,0,9,5,6,6,0,0,26,9,0,0,1,0,38,13,1,1,1,6,0,0,0,0,0,12,0,0,1,0,6,6,5,5,2,0,27,8,0,0,0,0,"gibsg901","Greg Gibson","timmt901","Tim Timmons","wolfj901","Jim Wolf","reybd901","D.J. Reyburn","","(none)","","(none)","bochb002","Bruce Bochy","lovut001","Tony Lovullo","rodnf001","Fernando Rodney","melam001","Mark Melancon","","(none)","owinc001","Chris Owings","bumgm001","Madison Bumgarner","greiz001","Zack Greinke","spand001","Denard Span",8,"beltb001","Brandon Belt",3,"pench001","Hunter Pence",9,"poseb001","Buster Posey",2,"crawb001","Brandon Crawford",6,"nunee002","Eduardo Nunez",5,"parkj002","Jarrett Parker",7,"panij002","Joe Panik",4,"bumgm001","Madison Bumgarner",1,"polla001","A.J. Pollock",8,"owinc001","Chris Owings",6,"goldp001","Paul Goldschmidt",3,"lambj001","Jake Lamb",5,"tomay001","Yasmany Tomas",7,"drurb001","Brandon Drury",4,"perad001","David Peralta",9,"mathj001","Jeff Mathis",2,"greiz001","Zack Greinke",1,"","Y"
       Field(s)  Meaning
           1     Date in the form "yyyymmdd"
           2     Number of game:
                    "0" -- a single game
                    "1" -- the first game of a double (or triple) header
                           including seperate admission doubleheaders
                    "2" -- the second game of a double (or triple) header
                           including seperate admission doubleheaders
                    "3" -- the third game of a triple-header
                    "A" -- the first game of a double-header involving 3 teams
                    "B" -- the second game of a double-header involving 3 teams
           3     Day of week  ("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
         4-5     Visiting team and league
           6     Visiting team game number
                 For this and the home team game number, ties are counted as
                 games and suspended games are counted from the starting
                 rather than the ending date.
         7-8     Home team and league
           9     Home team game number
       10-11     Visiting and home team score (unquoted)
          12     Length of game in outs (unquoted).  A full 9-inning game would
                 have a 54 in this field.  If the home team won without batting
                 in the bottom of the ninth, this field would contain a 51.
          13     Day/night indicator ("D" or "N")
          14     Completion information.

        */
    @Override
    public void loadDataForYear(int year) {
        String dataFileLocation = System.getProperty("data.file.location", "./gamelogs");

        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(dataFileLocation, String.format("GL%d.TXT", year));
        try {
            Files.lines(path).forEach(s -> {
                String[] t = s.split(",");
                LocalDate date = LocalDate.parse(trimQuotes(t[0]), DateTimeFormatter.BASIC_ISO_DATE);
                int vruns = Integer.parseInt(t[9]);
                int hruns = Integer.parseInt(t[10]);

                String[] teams = new String[]{trimQuotes(t[3]), trimQuotes(t[6])};
                boolean[] results = new boolean[]{vruns > hruns, hruns > vruns};
                for (int i = 0; i < 2; i++) {
                    Map<String, Object> m;
                    try {
                        m = jdbcTemplate.queryForMap("select wins,losses,game_date from game_results where game_date <= ? and team = ? " +
                                        "order by game_date desc limit 1",
                                Date.valueOf(date), teams[i]);
                    } catch (EmptyResultDataAccessException erdae) {
                        m = new HashMap<>();
                        m.put("WINS", 0);
                        m.put("LOSSES", 0);
                    }

                    int w = (Integer) m.get("WINS");
                    int l = (Integer) m.get("LOSSES");
                    if (results[i])
                        w++;
                    else
                        l++;
                    jdbcTemplate.update("insert into game_results (game_date,team,wins,losses) values(?,?,?,?)",
                            Date.valueOf(date), teams[i], w, l);
                }

            });
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private String trimQuotes(String s) {
        if (s.length() <= 1)
            return s;
        if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"')
            return s.substring(1, s.length() - 1);
        if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')
            return s.substring(1, s.length() - 1);
        return s;
    }
}
