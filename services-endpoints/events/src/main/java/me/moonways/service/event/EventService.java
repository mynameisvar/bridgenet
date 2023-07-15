package me.moonways.service.event;

import me.moonways.bridgenet.service.inject.Component;
import me.moonways.service.event.subscribe.EventSubscriptionImpl;
import me.moonways.service.event.subscribe.EventSubscriptionApplier;
import me.moonways.bridgenet.service.inject.DependencyInjection;
import me.moonways.bridgenet.service.inject.Inject;
import me.moonways.services.api.events.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class EventService {

    private final ExecutorService threadsExecutorService = Executors.newCachedThreadPool();

// ----------------------------------------------------------------------------------------------------- //

    private final EventRegistry eventRegistry = new EventRegistry();

    private final EventExecutor eventExecutor = new EventExecutor(threadsExecutorService, eventRegistry);

    private final EventSubscriptionApplier eventSubscriptionApplier = new EventSubscriptionApplier(this);

// ----------------------------------------------------------------------------------------------------- //

    @Inject
    private DependencyInjection dependencyInjection;

    @NotNull
    public <E extends Event> EventFutureImpl<E> fireEvent(@NotNull E event) {
        dependencyInjection.injectDependencies(event);

        EventFutureImpl<E> eventFutureImpl = eventExecutor.fireEvent(event);
        eventSubscriptionApplier.followSubscription(eventFutureImpl);

        return eventFutureImpl;
    }

    public void registerHandler(@NotNull Object handler) {
        dependencyInjection.injectDependencies(handler);
        eventRegistry.register(handler);
    }

    public void unregisterHandler(@NotNull Object handler) {
        eventRegistry.unregister(handler.getClass());
    }

    public void unregisterHandler(@NotNull Class<?> handlerType) {
        eventRegistry.unregister(handlerType);
    }

    public void subscribe(@NotNull EventSubscriptionImpl<?> subscription) {
        eventSubscriptionApplier.subscribe(subscription);
    }

    public void unsubscribe(@NotNull EventSubscriptionImpl<?> subscription) {
        eventSubscriptionApplier.unsubscribe(subscription);
    }
}
