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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

@Component("Pennant")
public class Pennant  extends CommandLineApplication {

    private final TeamDataDAO teamDataDAO;

    public Pennant(DataSource dataSource) {
        teamDataDAO = new TeamDataDAOImpl(new JdbcTemplate(dataSource));
    }

    @Value("${file.name:}")
    private String fileName;
    @Value("${title:}")
    private String chartTitle;

    @Override
    public void run(String... args) throws Exception {
        int currentYear = Integer.parseInt(args[0]);
        // Args can be:
        // nothing (implies ALL)
        // ALL
        // a lit of divisions/leagues. E.g. ALE,NLE,NLC
        // a list of team names: BOS,NYY,TMP
        List<DivisionData> divisions;
        if (args.length <= 1 || args[1].equals("ALL"))
            divisions = teamDataDAO.getDivisionDataForYear(currentYear, Collections.emptySet());
        else if (Pattern.matches("([AN]L[ECW]?(,|$))+",args[1])) {
            divisions = teamDataDAO.getDivisionDataForYear(currentYear, new HashSet<>(Arrays.asList(args[1].split(","))));
        } else {
            divisions = Collections.singletonList(new DivisionData(chartTitle, fileName, Arrays.asList(args[1].split(","))));
        }
        for (DivisionData divisionData : divisions) {
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            for (String teamName : divisionData.getMembers()) {
                TeamData teamData = teamDataDAO.getTeamData(teamName, currentYear);
                dataset.addSeries(readTeamFile(teamData, currentYear));
            }
            String chartName = divisionData.getLongName() + " " + currentYear;
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    chartName,                     // title
                    "Date",                     // x-axis label
                    "Games over/under .500",    // y-axis label
                    dataset,                    // data
                    true,                          // create legend?
                    false,                         // generate tooltips?
                    false                          // generate URLs?
            );
            XYPlot plot = (XYPlot) chart.getPlot();

            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            int j = 0;
            for (String teamName : divisionData.getMembers()) {
                TeamData teamData = teamDataDAO.getTeamData(teamName, currentYear);
                renderer.setSeriesPaint(j++, teamData.getColor());
            }
            Rectangle2D.Double shape = new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0);
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                renderer.setSeriesShape(i, shape);
                renderer.setSeriesShapesVisible(i, true);
                renderer.setSeriesShapesFilled(i, true);
            }
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("MMM d"));
            axis.setVerticalTickLabels(true);

            BufferedImage image = chart.createBufferedImage(800, 600);
            String outFileName = String.format("%s%d.png", divisionData.getFileName(), currentYear);
            File imageFile = new File(outFileName);
            ImageIO.write(image, "png", imageFile);
        }
    }

    private TimeSeries readTeamFile(TeamData teamData, int currentYear) {
        TimeSeries timeSeries = new TimeSeries(teamData.getName());
        List<Standings> standings = teamDataDAO.getStandingsForTeamAndYear(teamData.getAbbrev2(), currentYear);
        for (Standings s : standings) {
            timeSeries.addOrUpdate(new Day(s.getDate().getDayOfMonth(), s.getDate().getMonthValue(), s.getDate().getYear()), s.getWins() - s.getLosses());
        }
        return timeSeries;
    }
}
