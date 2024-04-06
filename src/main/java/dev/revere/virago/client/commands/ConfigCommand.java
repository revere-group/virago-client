package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.ConfigService;
import dev.revere.virago.util.Logger;

import java.io.File;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
@CommandData(aliases = {"config"}, description = "Manage client configuration", syntax = "config <load/save/delete>")
public class ConfigCommand extends AbstractCommand {
    @Override
    public void executeCommand(String line, String[] args) {
        if (args.length < 2) {
            return;
        }

        String action = args[1];

        if ("list".equals(action)) {
            listConfigs();
            return;
        }

        ConfigService service = Virago.getInstance().getServiceManager().getService(ConfigService.class);
        String name = args[2];

        switch (action) {
            case "load":
                if (service.configExists(name)) {
                    service.loadConfig(name);
                    Logger.addChatMessage("Loaded config " + name);
                } else {
                    Logger.addChatMessage("Config " + name + " does not exist");
                }
                break;
            case "save":
                service.saveConfig(name);
                Logger.addChatMessage("Saved config " + name);
                break;
            case "delete":
                if (service.configExists(name)) {
                    service.deleteConfig(name);
                    Logger.addChatMessage("Deleted config " + name);
                } else {
                    Logger.addChatMessage("Config " + name + " does not exist");
                }
                break;
        }
    }

    private void listConfigs() {
        ConfigService service = Virago.getInstance().getServiceManager().getService(ConfigService.class);
        File[] configs = service.listConfigs();
        if (configs != null && configs.length > 0) {
            StringBuilder message = new StringBuilder("Available configs: ");
            for (File config : configs) {
                message.append(config.getName()).append(", ");
            }

            // Remove the trailing comma and space
            message.delete(message.length() - 2, message.length());
            Logger.addChatMessage(message.toString().replace(".json", ""));
        } else {
            Logger.addChatMessage("No configs available");
        }
    }
}
