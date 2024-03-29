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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.AttributedCharacterIterator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.awt.font.TextAttribute.WEIGHT_BOLD;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;
@SpringBootApplication
public class Pennant  implements CommandLineRunner {

    private TeamDataDAO teamDataDAO;

    public static void main(String[] args) {
        SpringApplication.run(Pennant.class, args);
    }

    public void init() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(HSQL)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .addScript("schema.sql")
                .addScripts("teams.sql", "team_colors.sql", "leagues.sql")
                .build();
        if (dataSourceType.equals("retrosheet"))
            teamDataDAO = new RetroSheetTeamDataDAOImpl(new JdbcTemplate(dataSource));
        else if (dataSourceType.equals("mlbapi"))
            teamDataDAO = new MlbApiTeamDataDAOImpl(new JdbcTemplate(dataSource));
        else
            throw new RuntimeException("Unknown data source type: "+dataSourceType);
        chartWidth = Integer.parseInt(chartWidthStr);
        chartHeight = Integer.parseInt(chartHeightStr);
    }
    @Value("${file.name:}")
    private String fileName;
    @Value("${title:}")
    private String chartTitle;
    @Value("${data.source.type:retrosheet}")
    private String dataSourceType;
    @Value("${chart.width:800}")
    private String chartWidthStr;
    @Value("${chart.height:600}")
    private String chartHeightStr;
    private int chartWidth;
    private int chartHeight;

    @Override
    public void run(String... args) throws Exception {
        init();
        List<String> argList = Arrays.stream(args).filter(a -> !a.startsWith("--")).collect(Collectors.toList());

        int defaultChartWidth = 800;
        double xScale = (double)chartWidth / (double) defaultChartWidth;
        int defaultChartHeight = 600;
        double yScale = (double)chartHeight / (double) defaultChartHeight;
        double scale = Double.max(xScale, yScale);

        int currentYear = Integer.parseInt(argList.get(0));
        teamDataDAO.loadDataForYear(currentYear);

        // Args can be:
        // nothing (implies ALL)
        // ALL
        // a lit of divisions/leagues. E.g. ALE,NLE,NLC
        // a list of team names: BOS,NYY,TMP
        List<DivisionData> divisions;
        //noinspection ConstantValue
        if (argList.isEmpty() || argList.get(1).equals("ALL"))
            divisions = teamDataDAO.getDivisionDataForYear(currentYear, Collections.emptySet());
        else if (Pattern.matches("([AN]L[ECW]?(,|$))+",argList.get(1))) {
            divisions = teamDataDAO.getDivisionDataForYear(currentYear, new HashSet<>(Arrays.asList(argList.get(1).split(","))));
        } else {
            divisions = Collections.singletonList(new DivisionData(chartTitle, fileName, Arrays.asList(argList.get(1).split(","))));
        }
        for (DivisionData divisionData : divisions) {
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            for (String teamName : divisionData.getMembers()) {
                TeamData teamData = teamDataDAO.getTeamData(teamName, currentYear);
                dataset.addSeries(readTeamFile(teamData, currentYear));
            }
            String chartName = chartTitle.isEmpty() ? divisionData.getLongName() + " " + currentYear : chartTitle;
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
                renderer.setSeriesStroke(i, new BasicStroke((float)scale));
            }
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("MMM d"));
            axis.setVerticalTickLabels(true);

            double titleFontSize = 20.0 * scale;
            double labelFontSize = 14.0 * scale;
            double tickLabelFontSize = 12.0 * scale;
            String fontFamily = chart.getTitle().getFont().getFamily();
            Map<AttributedCharacterIterator.Attribute,Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.FAMILY, fontFamily);
            attributes.put(TextAttribute.WEIGHT, WEIGHT_BOLD);
            attributes.put(TextAttribute.SIZE, titleFontSize);
            Font titleFont = new Font(attributes);
            attributes = new HashMap<>();
            attributes.put(TextAttribute.FAMILY, fontFamily);
            attributes.put(TextAttribute.SIZE, labelFontSize);
            Font labelFont = new Font(attributes);
            attributes = new HashMap<>();
            attributes.put(TextAttribute.FAMILY, fontFamily);
            attributes.put(TextAttribute.SIZE, tickLabelFontSize);
            Font tickLabelFont = new Font(attributes);
            chart.getTitle().setFont(titleFont);
            plot.getDomainAxis().setLabelFont(labelFont);
            plot.getDomainAxis().setTickLabelFont(tickLabelFont);
            plot.getRangeAxis().setLabelFont(labelFont);
            plot.getRangeAxis().setTickLabelFont(tickLabelFont);

            BufferedImage image = chart.createBufferedImage(chartWidth, chartHeight);
            String outFileName = fileName.isEmpty() ? String.format("%s%d.png", divisionData.getFileName(), currentYear) : fileName;
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
