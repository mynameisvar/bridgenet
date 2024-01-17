package me.moonways.bridgenet.api.event;

import me.moonways.bridgenet.api.event.subscribe.EventSubscription;
import me.moonways.bridgenet.api.event.subscribe.EventSubscriptionApplier;
import me.moonways.bridgenet.api.inject.Autobind;
import me.moonways.bridgenet.api.inject.DependencyInjection;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.api.util.thread.Threads;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

@Autobind
public final class EventService {

    private final ExecutorService threadsExecutorService = Threads.newCachedThreadPool();

    private final EventRegistry eventRegistry = new EventRegistry();
    private final EventExecutor eventExecutor = new EventExecutor(threadsExecutorService, eventRegistry);

    private final EventSubscriptionApplier eventSubscriptionApplier = new EventSubscriptionApplier(this);

    @Inject
    private DependencyInjection injector;

    @NotNull
    public <E extends Event> EventFuture<E> fireEvent(@NotNull E event) {
        injector.injectFields(event);

        EventFuture<E> eventFuture = eventExecutor.fireEvent(event);
        eventSubscriptionApplier.followSubscription(eventFuture);

        return eventFuture;
    }

    public void registerHandler(@NotNull Object handler) {
        injector.injectFields(handler);
        eventRegistry.register(handler);
    }

    public void unregisterHandler(@NotNull Object handler) {
        eventRegistry.unregister(handler.getClass());
    }

    public void unregisterHandler(@NotNull Class<?> handlerType) {
        eventRegistry.unregister(handlerType);
    }

    public void subscribe(@NotNull EventSubscription<?> subscription) {
        eventSubscriptionApplier.subscribe(subscription);
    }

    public void unsubscribe(@NotNull EventSubscription<?> subscription) {
        eventSubscriptionApplier.unsubscribe(subscription);
    }
}