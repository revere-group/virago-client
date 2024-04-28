package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ConfigService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;

import java.io.File;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
@CommandData(aliases = {"config", "c"}, description = "Manage client configuration", syntax = "<load/save/delete/list>")
public class ConfigCommand extends AbstractCommand {
    @Override
    public void executeCommand(String line, String[] args) {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);
        ConfigService service = Virago.getInstance().getServiceManager().getService(ConfigService.class);

        if (args.length < 2) {
            notificationService.notify(NotificationType.NO, "Config Manager", "Please provide an option! " + getSyntax());
            return;
        }

        if (args.length < 3 && !"list".equals(args[1])) {
            if ("load".equals(args[1]) || "delete".equals(args[1]) || "save".equals(args[1])) {
                notificationService.notify(NotificationType.NO, "Config Manager", "Please provide a config name!");
                return;
            }
            return;
        }

        String action = args[1];

        if ("list".equals(action)) {
            listConfigs();
            return;
        }

        String name = args[2];

        switch (action) {
            case "load":
                if (service.configExists(name)) {
                    service.loadConfig(name);
                    notificationService.notify(NotificationType.YES, "Config Manager", "Successfully loaded config " + name);
                } else {
                    notificationService.notify(NotificationType.NO, "Config Manager", "Config " + name + " does not exist");
                }
                break;
            case "save":
                service.saveConfig(name);
                notificationService.notify(NotificationType.YES, "Config Manager", "Successfully saved config " + name);
                break;
            case "delete":
                if (service.configExists(name)) {
                    service.deleteConfig(name);
                    notificationService.notify(NotificationType.YES, "Config Manager", "Successfully deleted config " + name);
                } else {
                    notificationService.notify(NotificationType.NO, "Config Manager", "Config " + name + " does not exist");
                }
                break;
        }
    }

    private void listConfigs() {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);
        ConfigService service = Virago.getInstance().getServiceManager().getService(ConfigService.class);
        File[] configs = service.listConfigs();
        if (configs != null && configs.length > 0) {
            StringBuilder message = new StringBuilder("Available configs: ");
            for (File config : configs) {
                message.append(config.getName()).append(", ");
            }

            message.delete(message.length() - 2, message.length());
            Logger.addChatMessage(message.toString().replace(".json", ""));
        } else {
            notificationService.notify(NotificationType.NO, "Config Manager", "No configs found");
        }
    }
}
