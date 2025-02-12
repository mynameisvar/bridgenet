package me.moonways.bridgenet.api.scheduler.task;

import me.moonways.bridgenet.api.scheduler.ScheduledTime;
import me.moonways.bridgenet.api.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractScheduledTask implements TaskProcess {

    private ScheduledTask scheduledTask;
    private TaskFuture taskFuture;

    public synchronized void shutdown() {
        if (scheduledTask != null) {
            scheduledTask.shutdown();
        }
    }

    public synchronized void forceShutdown() {
        if (scheduledTask != null) {
            scheduledTask.forceShutdown();
        }
    }

    public long getProcessedLoopsCount() {
        if (taskFuture != null) {
            return taskFuture.getProcessedLoopsCount();
        }

        return 0;
    }

    @NotNull
    public synchronized TaskFuture scheduleSynchronized(@NotNull Scheduler scheduler, @NotNull ScheduledTime delay) {
        taskFuture = scheduler.scheduleSynchronized(delay);
        taskFuture.follow(this);

        scheduledTask = taskFuture.getScheduledTask();

        return taskFuture;
    }

    @NotNull
    public synchronized TaskFuture scheduleSynchronized(@NotNull Scheduler scheduler, @NotNull ScheduledTime delay, @NotNull ScheduledTime period) {
        taskFuture = scheduler.scheduleSynchronized(delay, period);
        taskFuture.follow(this);

        scheduledTask = taskFuture.getScheduledTask();

        return taskFuture;
    }

    @NotNull
    public synchronized TaskFuture schedule(@NotNull Scheduler scheduler, @NotNull ScheduledTime delay) {
        taskFuture = scheduler.schedule(delay);
        taskFuture.follow(this);

        scheduledTask = taskFuture.getScheduledTask();

        return taskFuture;
    }

    @NotNull
    public synchronized TaskFuture schedule(@NotNull Scheduler scheduler, @NotNull ScheduledTime delay, @NotNull ScheduledTime period) {
        taskFuture = scheduler.schedule(delay, period);
        taskFuture.follow(this);

        scheduledTask = taskFuture.getScheduledTask();

        return taskFuture;
    }
}
