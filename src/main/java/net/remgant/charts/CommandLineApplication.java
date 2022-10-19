package net.remgant.charts;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class CommandLineApplication implements ApplicationContextAware {
    protected ApplicationContext applicationContext;
    abstract void run(String... args) throws Exception;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
