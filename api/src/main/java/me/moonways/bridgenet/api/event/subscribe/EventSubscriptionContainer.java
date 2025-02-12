package me.moonways.bridgenet.api.event.subscribe;

import me.moonways.bridgenet.api.event.Event;

import java.util.*;

public class EventSubscriptionContainer {

    private final Map<Class<? extends Event>, Set<EventSubscription<?>>> subscriptionMap = Collections.synchronizedMap(new HashMap<>());

    public void addSubscription(EventSubscription<?> subscription) {
        registerInternal(subscription);
    }

    public void removeSubscription(EventSubscription<?> subscription) {
        subscriptionMap.remove(subscription.getEventType());
    }

    public Set<EventSubscription<?>> getSubscriptions(Class<? extends Event> eventType) {
        return subscriptionMap.get(eventType);
    }

    private void registerInternal(EventSubscription<?> subscription) {
        Class<? extends Event> eventType = subscription.getEventType();
        Set<EventSubscription<?>> eventSubscriptionImpls = subscriptionMap.get(eventType);

        if (eventSubscriptionImpls == null)
            eventSubscriptionImpls = new HashSet<>();

        eventSubscriptionImpls.add(subscription);

        subscriptionMap.put(eventType, eventSubscriptionImpls);
    }
}
