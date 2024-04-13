package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.FriendService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import net.optifine.Log;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"friend"}, description = "Toggle a module", syntax = "<add/remove/list/clear> <player>")
public class FriendCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        FriendService service = Virago.getInstance().getServiceManager().getService(FriendService.class);
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);

        if (args.length < 2) {
            notificationService.notify(NotificationType.NO, "Command Manager", "Please provide an option! " + getSyntax());
            return;
        }

        if (args.length < 3 && !"list".equals(args[1]) && !args[1].equalsIgnoreCase("clear")) {
            if ("remove".equals(args[1]) || "add".equals(args[1])) {
                notificationService.notify(NotificationType.NO, "Command Manager", "Please provide a player!");
                return;
            }
            return;
        }

        String option = args[1].toLowerCase();

        switch(option) {
            case "remove": {
                String player = args[2];
                if(!service.isFriend(player)) {
                    notificationService.notify(NotificationType.NO, "Command Manager", "You do not have " + player + " as a friend.");
                    break;
                }

                service.removeFriend(player);
                notificationService.notify(NotificationType.YES, "Command Manager", "You have removed " + player + " from your friend list.");
                break;
            }

            case "add": {
                String player = args[2];
                if(service.isFriend(player)) {
                    notificationService.notify(NotificationType.NO, "Command Manager", "You already have " + player + " as a friend.");
                    break;
                }

                service.addFriend(player);
                notificationService.notify(NotificationType.YES, "Command Manager", "You have added " + player + " as a friend.");
                break;
            }

            case "list": {
                if (service.getFriends().isEmpty()) {
                    notificationService.notify(NotificationType.NO, "Command Manager", "You do not have any friends.");
                    break;
                }
                Logger.addChatMessage("Friends: " + service.getFriends().toString().replace("[", "").replace("]", ""));
                break;
            }

            case "clear": {
                service.getFriends().clear();
                notificationService.notify(NotificationType.YES, "Command Manager", "You have cleared your friends list.");
                break;
            }
        }
    }
}
