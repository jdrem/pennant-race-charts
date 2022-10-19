package net.remgant.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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

@Component
public class MlbApiDataLoader extends DataLoader {
    final public static Map<String,String> teamMap;
    static {
        Map<String,String> m = new HashMap<>();
        m.put("Philadelphia Phillies","PHI");
        m.put("Atlanta Braves","ATL");
        m.put("Cleveland Guardians","CLE");
        m.put("Chicago Cubs","CNH");
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
        m.put("Oakland Athletics","OAK");
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

    final private JdbcTemplate jdbcTemplate;
    protected MlbApiDataLoader(DataSource dataSource) {
        super(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadData(int year) {
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
        Map<String,Object> map;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<Map<String,Object>> dates = (List<Map<String, Object>>) map.get("dates");
        for (Map<String,Object> d : dates) {
            LocalDate localDate = LocalDate.parse(d.get("date").toString(), DateTimeFormatter.ISO_LOCAL_DATE);
            List<Map<String,Object>> games = (List<Map<String, Object>>) d.get("games");
            for (Map<String,Object> game : games) {
                String gameType = game.get("gameType").toString();
                if (!gameType.equals("R"))
                    continue;
                Map<String,Object> status = (Map<String, Object>) game.get("status");
                String codedGameStatus = status.get("codedGameState").toString();
                if (!codedGameStatus.equals("F"))
                    continue;
                boolean doubleHeader = game.getOrDefault("doubleHeader","N").equals("Y");
                int gameNumber = doubleHeader ? (int)game.get("gameNumber") : 0;
                Map<String,Object> away = (Map<String, Object>) ((Map<String,Object>)game.get("teams")).get("away");
                Map<String,Object> home = (Map<String, Object>) ((Map<String,Object>)game.get("teams")).get("home");
                Map<String,Object> awayTeam = (Map<String, Object>) away.get("team");
                String awayName = awayTeam.get("name").toString();
                String awayAbbrev = teamMap.getOrDefault(awayName, "XXX");
                int awayRuns = (int)away.get("score");
                Map<String,Object> homeTeam = (Map<String, Object>) home.get("team");
                String homeName = homeTeam.get("name").toString();
                String homeAbbrev = teamMap.getOrDefault(homeName, "XXX");
                int homeRuns = (int)home.get("score");
                String gameCode = homeAbbrev + DateTimeFormatter.BASIC_ISO_DATE.format(localDate) + gameNumber;
                try {
                    jdbcTemplate.update("insert into games (game_id,game_date,game_number,home_team,home_runs,away_team,away_runs) values (?,?,?,?,?,?,?)",
                            gameCode, Date.valueOf(localDate), gameNumber, homeAbbrev, homeRuns, awayAbbrev, awayRuns);
                } catch (DuplicateKeyException ignore) {
                }
            }
        }
    }
}
