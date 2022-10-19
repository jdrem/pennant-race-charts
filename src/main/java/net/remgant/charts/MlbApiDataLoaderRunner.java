package net.remgant.charts;

import org.springframework.stereotype.Component;

@Component("MlbApiDataLoaderRunner")
public class MlbApiDataLoaderRunner extends CommandLineApplication {

    @SuppressWarnings("RedundantThrows")
    @Override
    public void run(String... args) throws Exception {
        DataLoader dataLoader = applicationContext.getBean("mlbApiDataLoader", DataLoader.class);
        dataLoader.loadData(2022);
    }
}
