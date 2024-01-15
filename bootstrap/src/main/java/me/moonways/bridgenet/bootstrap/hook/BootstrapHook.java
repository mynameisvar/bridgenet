package me.moonways.bridgenet.bootstrap.hook;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.bootstrap.AppBootstrap;
import org.jetbrains.annotations.NotNull;

@Log4j2
@RequiredArgsConstructor
public abstract class BootstrapHook {

    public void prepareExecution() {
        // override me.
    }

    protected abstract void postExecute(@NotNull AppBootstrap bootstrap) throws Exception;

    public final void execute(@NotNull AppBootstrap bootstrap,
                              @NotNull String namespace) {

        log.info("§7************************* BOOSTRAP HOOK EXECUTION BEGIN ({}) *************************", namespace);
        executeUnchecked(bootstrap, namespace);

        log.info("§eBootstrap hook '{}' was processed", namespace);
    }

    private void executeUnchecked(AppBootstrap bootstrap, String namespace) {
        try {
            postExecute(bootstrap);
        }
        catch (Exception exception) {
            log.error("§4Bootstrap hook '{}' execution aborted:", namespace, exception);
        }
    }
}
