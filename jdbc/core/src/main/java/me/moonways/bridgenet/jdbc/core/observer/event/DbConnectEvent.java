package me.moonways.bridgenet.jdbc.core.observer.event;

import lombok.val;
import me.moonways.bridgenet.jdbc.core.ConnectionID;
import me.moonways.bridgenet.jdbc.core.observer.AbstractObservable;

public class DbConnectEvent extends AbstractObservable {

    private static final String TO_STRING_FORMAT = "DbConnectEvent(eventId=%s, connection=\"%s\")";

    public DbConnectEvent(long eventId, ConnectionID connectionID) {
        super(eventId, connectionID);
    }

    @Override
    public String toString() {
        val eventId = getEventId();
        val connectionID = getConnectionID().getUniqueId();

        return String.format(TO_STRING_FORMAT, eventId, connectionID);
    }
}
