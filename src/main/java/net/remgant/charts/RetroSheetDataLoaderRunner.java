package net.remgant.charts;

import org.springframework.stereotype.Component;

@Component("RetroSheetDataLoaderRunner")
public class RetroSheetDataLoaderRunner extends CommandLineApplication {

    @SuppressWarnings("RedundantThrows")
    @Override
    public void run(String... args) throws Exception {
        DataLoader dataLoader = applicationContext.getBean("retroSheetDataLoader", DataLoader.class);
        dataLoader.loadData(2021);
    }
}
