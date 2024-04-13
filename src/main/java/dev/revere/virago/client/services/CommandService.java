package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public class CommandService implements IService {

    private final LinkedHashMap<Class<?>, AbstractCommand> commands = new LinkedHashMap<>();

    @Override
    public void initService() {
        Logger.info("Command service initialized!", getClass());
    }

    @Override
    public void startService() {
        Reflections reflections = new Reflections("dev.revere.virago.client.commands");
        Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);

        for (Class<?> clazz : classes) {
            try {
                AbstractCommand command = (AbstractCommand) clazz.newInstance();
                commands.put(command.getClass(), command);
            } catch (InstantiationException | IllegalAccessException e) {
                Logger.err("Failed to instantiate command " + clazz.getSimpleName(), getClass());
            }
        }
    }

    public void executeCommand(String line) {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);

        String[] args = line.split(" ");
        String alias = args[0].substring(1);

        AbstractCommand command = commands.values().stream()
                .filter(cmd -> Arrays.stream(cmd.getAliases()).anyMatch(alias::equalsIgnoreCase))
                .findFirst().orElse(null);

        if (command == null) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Command not found!");
            return;
        }

        try {
            command.executeCommand(line, args);
        } catch (Exception e) {
            notificationService.notify(NotificationType.ERROR, "Command Manager", "An error occurred while executing the command!", 3000L);
        }
    }
}
