package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.FriendService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;
import net.optifine.Log;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"friend"}, description = "Toggle a module", syntax = "friend <player>")
public class FriendCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        FriendService service = Virago.getInstance().getServiceManager().getService(FriendService.class);

        String option = args[1].toLowerCase();

        switch(option) {
            case "remove": {
                String player = args[2];

                if(!service.isFriend(player)) {
                    Logger.addChatMessage("You do not have " + player + " as a friend.");
                    break;
                }

                service.removeFriend(player);
                Logger.addChatMessage("You have removed " + player + " as a friend.");
                break;
            }

            case "add": {
                String player = args[2];

                if(service.isFriend(player)) {
                    Logger.addChatMessage("You already have " + player + " as a friend.");
                    break;
                }

                service.addFriend(player);
                Logger.addChatMessage("You have added " + player + " as a friend.");
                break;
            }

            case "list": {
                Logger.addChatMessage("Friends List: " + service.getFriends().toString().replace("[", "").replace("]", ""));
                break;
            }
        }
    }
}
