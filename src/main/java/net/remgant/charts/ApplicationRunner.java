package net.remgant.charts;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.util.Arrays;

@SpringBootApplication(scanBasePackages = "net.remgant.charts")
public class ApplicationRunner implements CommandLineRunner, ApplicationContextAware {
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(ApplicationRunner.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            throw new IllegalAccessException("no args given");
        }
        CommandLineApplication commandLineApplication = applicationContext.getBean(args[0], CommandLineApplication.class);
        commandLineApplication.run(Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public DataSource dataSource() {
        return new SingleConnectionDataSource("jdbc:hsqldb:file:./data/retrosheet;shutdown=true","SA","",true);
    }
}
