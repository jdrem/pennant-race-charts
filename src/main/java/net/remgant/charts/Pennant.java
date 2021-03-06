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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

public class Pennant {

    private TeamDataDAO teamDataDAO;

    void init() {
        EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(HSQL)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .addScript("schema.sql")
                .addScripts("teams.sql", "team_colors.sql", "leagues.sql")
                .build();
        teamDataDAO = new TeamDataDAOImpl(new JdbcTemplate(db));
    }

    private String args[];

    public static void main(String args[]) throws Exception {
        Pennant pennant = new Pennant(args);
        pennant.init();
        pennant.run();
    }

    Pennant(String args[]) {
        this.args = args;
    }


    private void run() throws Exception {
        int currentYear = Integer.parseInt(args[0]);
        teamDataDAO.loadDataForYear(currentYear);
        List<String> divisionsToChart = new ArrayList<>();
        int idx = 1;
        if (args.length > 1 && !args[1].equals("ALL")) {
            while (Pattern.matches("[AN]L[ECW]", args[idx]) && idx < args.length) {
                divisionsToChart.add(args[idx++]);
            }
        }
        Map<String, DivisionData> divisions = teamDataDAO.getDivisionDataForYear(currentYear).stream()
                .filter(d -> divisionsToChart.isEmpty() || divisionsToChart.contains(d.getShortName()))
                .collect(Collectors.toMap(DivisionData::getShortName, d -> d));
        for (DivisionData divisionData : divisions.values()) {
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
