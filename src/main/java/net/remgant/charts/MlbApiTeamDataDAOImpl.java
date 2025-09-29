package net.remgant.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MlbApiTeamDataDAOImpl extends TeamDataDAOImpl {
    final private static Map<String,String> teamMap;
    static {
        Map<String,String> m = new HashMap<>();
        m.put("Philadelphia Phillies","PHI");
        m.put("Atlanta Braves","ATL");
        m.put("Cleveland Guardians","CLE");
        m.put("Chicago Cubs","CHN");
        m.put("Kansas City Royals","KCA");
        m.put("Miami Marlins","MIA");
        m.put("Minnesota Twins","MIN");
        m.put("San Diego Padres","SDN");
        m.put("Pittsburgh Pirates","PIT");
        m.put("New York Yankees","NYA");
        m.put("Toronto Blue Jays","TOR");
        m.put("Arizona Diamondbacks","ARI");
        m.put("New York Mets","NYN");
        m.put("Cincinnati Reds","CIN");
        m.put("Seattle Mariners","SEA");
        m.put("Los Angeles Angels","ANA");
        m.put("Milwaukee Brewers","MIL");
        m.put("Colorado Rockies","COL");
        m.put("Athletics","OAK");
        m.put("Boston Red Sox","BOS");
        m.put("Chicago White Sox","CHA");
        m.put("Detroit Tigers","DET");
        m.put("Houston Astros","HOU");
        m.put("St. Louis Cardinals","SLN");
        m.put("San Francisco Giants","SFN");
        m.put("Tampa Bay Rays","TBA");
        m.put("Texas Rangers","TEX");
        m.put("Washington Nationals","WAS");
        m.put("Los Angeles Dodgers","LAN");
        m.put("Baltimore Orioles","BAL");
        teamMap = Collections.unmodifiableMap(m);
    }

    public MlbApiTeamDataDAOImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadDataForYear(int year) {
        String dataFileLocation = System.getProperty("data.file.location", "./gamelogs");
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(dataFileLocation, String.format("mlb-%d.json", year));
        byte[] b;
        try {
            b = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String json = new String(b, Charset.defaultCharset());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<Map<String,Object>> dates = (List<Map<String, Object>>) map.get("dates");
        for (Map<String,Object> d : dates) {
            LocalDate localDate = LocalDate.parse(d.get("date").toString(), DateTimeFormatter.ISO_LOCAL_DATE);
            List<Map<String, Object>> games = (List<Map<String, Object>>) d.get("games");
            for (Map<String, Object> game : games) {
                String gameType = game.get("gameType").toString();
                if (!gameType.equals("R"))
                    continue;
                Map<String, Object> status = (Map<String, Object>) game.get("status");
                String codedGameStatus = status.get("codedGameState").toString();
                if (!codedGameStatus.equals("F"))
                    continue;
                Map<String, Object> away = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("away");
                Map<String, Object> home = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("home");
                Map<String, Object> awayTeam = (Map<String, Object>) away.get("team");
                String awayName = awayTeam.get("name").toString();
                String awayAbbrev = teamMap.getOrDefault(awayName, "XXX");
                Map<String,Object> awayLeagueRecord = (Map<String, Object>) away.get("leagueRecord");
                int awayWins = (int)awayLeagueRecord.get("wins");
                int awayLosses = (int)awayLeagueRecord.get("losses");
                Map<String, Object> homeTeam = (Map<String, Object>) home.get("team");
                String homeName = homeTeam.get("name").toString();
                String homeAbbrev = teamMap.getOrDefault(homeName, "XXX");
                Map<String,Object> homeLeagueRecord = (Map<String, Object>) home.get("leagueRecord");
                int homeWins = (int)homeLeagueRecord.get("wins");
                int homeLosses = (int)homeLeagueRecord.get("losses");

                jdbcTemplate.update("insert into game_results (game_date,team,wins,losses) values (?,?,?,?)",
                        Date.valueOf(localDate), awayAbbrev, awayWins, awayLosses);
                jdbcTemplate.update("insert into game_results (game_date,team,wins,losses) values (?,?,?,?)",
                        Date.valueOf(localDate), homeAbbrev, homeWins, homeLosses);
            }
        }
    }
}
