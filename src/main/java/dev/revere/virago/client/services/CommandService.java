package dev.revere.virago.client.services;

import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import org.reflections.Reflections;

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
        String[] args = line.split(" ");
        String alias = args[0].substring(1);

        AbstractCommand command = commands.values().stream().filter(cmd -> cmd.getAliases()[0].equalsIgnoreCase(alias)).findFirst().orElse(null);
        if (command == null) {
            Logger.addChatMessage("Unknown command! Type .help for a list of commands.");
            return;
        }

        try {
            command.executeCommand(line, args);
        } catch (Exception e) {
            Logger.addChatMessage("Failed to execute command " + command.getClass().getSimpleName());
        }
    }
}
