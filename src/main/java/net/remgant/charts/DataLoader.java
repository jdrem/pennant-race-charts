package net.remgant.charts;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public abstract class DataLoader implements InitializingBean {
    final protected DataSource dataSource;

    protected DataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void init() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int count = jdbcTemplate.queryForObject("select count(*) from teams", Integer.TYPE);
        if (count == 0) {
            populator = new ResourceDatabasePopulator(
                    new ClassPathResource("leagues.sql"),
                    new ClassPathResource("teams.sql"),
                    new ClassPathResource("team_colors.sql"));
            populator.execute(dataSource);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    abstract public void loadData(int year);
}
