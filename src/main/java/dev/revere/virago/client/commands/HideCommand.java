package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/13/2024
 */
@CommandData(aliases = {"hide"}, description = "Hide a module", syntax = "<module>")
public class HideCommand extends AbstractCommand {
    @Override
    public void executeCommand(String line, String[] args) {
        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);

        if (args.length < 2) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Please provide a module name!");
            return;
        }

        String moduleName = args[1].replace("&", " ");
        if (moduleService.getModuleByName(moduleName) == null) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Module not found!");
            return;
        }

        if (moduleService.getModuleByName(moduleName).isHidden()) {
            moduleService.getModuleByName(moduleName).setHidden(false);
            notificationService.notify(NotificationType.YES, "Command Manager", "Module " + moduleName + " is now visible");
        } else {
            moduleService.getModuleByName(moduleName).setHidden(true);
            notificationService.notify(NotificationType.YES, "Command Manager", "Module " + moduleName + " is now hidden");
        }

    }
}
