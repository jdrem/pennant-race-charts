package net.remgant.charts;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Component
public class RetroSheetDataLoader extends DataLoader {
    final private JdbcTemplate jdbcTemplate;

    public RetroSheetDataLoader(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
    public void loadData(int year) {
        String dataFileLocation = System.getProperty("data.file.location", "./gamelogs");
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(dataFileLocation, String.format("GL%d.TXT", year));
        try (Stream<String> lines = Files.lines(path)) {
           lines.forEach(s -> {
                String[] t = s.split(",");
                String gameCode = trimQuotes(t[6]) + trimQuotes(t[0]) + trimQuotes(t[1]);
                LocalDate date = LocalDate.parse(trimQuotes(t[0]), DateTimeFormatter.BASIC_ISO_DATE);
                String gameNumber = trimQuotes(t[1]);
                String hTeam = trimQuotes(t[6]);
                String aTeam = trimQuotes(t[3]);
                int vruns = Integer.parseInt(t[9]);
                int hruns = Integer.parseInt(t[10]);

                try {
                    jdbcTemplate.update("insert into games (game_id,game_date,game_number,home_team,home_runs,away_team,away_runs) values (?,?,?,?,?,?,?)",
                            gameCode, Date.valueOf(date), gameNumber, hTeam, hruns, aTeam, vruns);
                } catch (DuplicateKeyException ignore) {
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
