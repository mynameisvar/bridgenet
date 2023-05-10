package me.moonways.bridgenet.api;

import lombok.Getter;
import me.moonways.bridgenet.api.command.Command;
import me.moonways.bridgenet.api.command.CommandContainer;
import me.moonways.bridgenet.api.command.CommandRegistry;
import me.moonways.bridgenet.api.connection.player.PlayerController;
import me.moonways.bridgenet.api.connection.server.ServerController;
import me.moonways.bridgenet.service.event.EventService;
import me.moonways.bridgenet.api.module.ModuleContainer;
import me.moonways.bridgenet.api.scheduler.Scheduler;
import me.moonways.bridgenet.service.inject.Component;
import me.moonways.bridgenet.service.inject.InitMethod;
import me.moonways.bridgenet.service.inject.Inject;
import org.jetbrains.annotations.NotNull;

@Getter
@Component
public class BridgenetControl {

    @Inject
    private CommandRegistry commandRegistry;

    @Inject
    private EventService eventService;

    @Inject
    private Scheduler scheduler;

    @Inject
    private ModuleContainer moduleContainer;

    @Inject
    private ServerController serverController;

    @Inject
    private PlayerController playerController;

    @InitMethod
    private void init() {
        moduleContainer.loadModules();
    }

    private void validateCommand(Class<?> commandType) {
        if (commandType == null) {
            throw new NullPointerException("command");
        }
    }

    public void registerCommand(@NotNull Class<? extends Command> commandClass) {
        validateCommand(commandClass);
        commandRegistry.register(commandClass);
    }

    public Command getCommand(@NotNull String line) {
        CommandContainer commandContainer = commandRegistry.getCommandContainer();
        return commandContainer.getCommand(line);
    }

}
