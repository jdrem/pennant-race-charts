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
import java.util.stream.Collectors;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public abstract class TeamDataDAOImpl implements TeamDataDAO {
    protected final JdbcTemplate jdbcTemplate;

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
        List<Map<String, Object>> l = jdbcTemplate.queryForList("select game_date,wins,losses from game_results where team = ? and game_date >= ? and game_date <= ? order by game_date",
                abbrev, Date.valueOf(LocalDate.of(year, 1, 1)), Date.valueOf(LocalDate.of(year, 12, 31)));
        return l.stream().map(g -> new Standings(((Date) g.get("GAME_DATE")).toLocalDate(), (Integer) g.get("WINS"), (Integer) g.get("LOSSES")))
                .collect(Collectors.toList());
    }

    protected String trimQuotes(String s) {
        if (s.length() <= 1)
            return s;
        if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"')
            return s.substring(1, s.length() - 1);
        if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')
            return s.substring(1, s.length() - 1);
        return s;
    }
}
