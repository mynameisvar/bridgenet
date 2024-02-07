package me.moonways.bridgenet.api.util.autorun;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.Autobind;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.api.inject.PostConstruct;
import me.moonways.bridgenet.api.inject.bean.service.BeansService;
import me.moonways.bridgenet.api.inject.processor.TypeAnnotationProcessorResult;
import me.moonways.bridgenet.api.inject.processor.persistance.GetTypeAnnotationProcessor;
import me.moonways.bridgenet.api.inject.processor.persistance.WaitTypeAnnotationProcessor;
import me.moonways.bridgenet.api.scheduler.ScheduledTime;
import me.moonways.bridgenet.api.scheduler.Scheduler;
import me.moonways.bridgenet.api.util.autorun.persistance.AutoRunner;
import me.moonways.bridgenet.api.util.autorun.persistance.Runnable;
import me.moonways.bridgenet.api.util.autorun.persistance.RunningPeriod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Autobind
@WaitTypeAnnotationProcessor(AutoRunner.class)
public class ScheduledRunnersService {

    private static final ScheduledTime DEFAULT_PERIOD = ScheduledTime.ofMillis(100);

    @Inject
    private BeansService beansService;
    @Inject
    private Scheduler scheduler;

    @GetTypeAnnotationProcessor
    private TypeAnnotationProcessorResult<Object> runnersResult;

    @PostConstruct
    private void init() {
        startGlobalTimer(runnersResult.toList());
    }

    private void startGlobalTimer(List<Object> runners) {
        List<RunnableUnit> units = toUnits(runners);

        Map<RunnableUnit, Long> unitsMap = units.stream()
                .collect(Collectors.toMap(unit -> unit, unit -> 0L));

        scheduler.schedule(ScheduledTime.ofSeconds(10), DEFAULT_PERIOD)
                .follow(new ScheduledRunnersInvocationTask(unitsMap));
    }

    private static List<RunnableUnit> toUnits(List<Object> runners) {
        List<RunnableUnit> result = new ArrayList<>();

        for (Object runner : runners) {

            Class<?> runnerClass = runner.getClass();
            log.info("Automatically runner §a'{}' §rwas found", runnerClass.getSimpleName());

            RunningPeriod periodAnnotation = runnerClass.getDeclaredAnnotation(RunningPeriod.class);

            for (Method method : runnerClass.getDeclaredMethods()) {

                if (method.isAnnotationPresent(Runnable.class)) {
                    RunningPeriod periodUnitAnnotation = method.getDeclaredAnnotation(RunningPeriod.class);
                    ScheduledTime period = DEFAULT_PERIOD;

                    if (periodUnitAnnotation != null) {
                        period = ScheduledTime.of(periodUnitAnnotation.period(), periodUnitAnnotation.unit());
                    }
                    else if (periodAnnotation != null) {
                        period = ScheduledTime.of(periodAnnotation.period(), periodAnnotation.unit());
                    }

                    method.setAccessible(true);
                    result.add(new RunnableUnit(period, () -> method.invoke(runner)));
                }
            }
        }

        return result;
    }
}
