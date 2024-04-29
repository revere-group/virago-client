package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"bind"}, description = "Bind a module to a key", syntax = "<add/remove> <module> <key>")
public class BindCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);
        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);

        if (args.length < 3) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Please follow the syntax! " + getSyntax());
            return;
        }

        String action = args[1];
        String moduleName = args[2];

        if (moduleService.getModuleByName(moduleName) == null) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Module not found!");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            if (args.length < 4) {
                notificationService.notify(NotificationType.NO, "Command Manager", "Please provide a key!");
                return;
            }
            String key = args[3];
            moduleService.getModuleByName(moduleName).setKey(Keyboard.getKeyIndex(key.toUpperCase()));
            notificationService.notify(NotificationType.YES, "Command Manager", "Bound " + moduleName + " to " + key);
        } else if (action.equalsIgnoreCase("remove")) {
            moduleService.getModuleByName(moduleName).setKey(Keyboard.KEY_NONE);
            notificationService.notify(NotificationType.YES, "Command Manager", "Unbound " + moduleName);
        } else {
            notificationService.notify(NotificationType.NO, "Command Manager", "Invalid action! " + getSyntax());
        }
    }
}
