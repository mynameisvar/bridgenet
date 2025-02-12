package me.moonways.bridgenet.test.data;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.jdbc.core.observer.DatabaseObserver;
import me.moonways.bridgenet.jdbc.core.observer.Observable;

@Log4j2
public class ExampleDatabaseObserver implements DatabaseObserver {

    @Override
    public void observe(Observable event) {
        log.debug(event);
    }
}
