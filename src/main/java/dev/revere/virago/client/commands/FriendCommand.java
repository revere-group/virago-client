package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.FriendService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"friend"}, description = "Toggle a module", syntax = "friend <player>")
public class FriendCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        if (args.length < 2) {
            Logger.addChatMessage("Please provide a valid player name Usage: " + getSyntax());
            return;
        }

        String player = args[2];
        FriendService service = Virago.getInstance().getServiceManager().getService(FriendService.class);

        if (service.isFriend(player)) {
            service.removeFriend(player);
            Logger.addChatMessage("Removed " + player + " as a friend.");
        } else {
            service.addFriend(player);
            Logger.addChatMessage("Added " + player + " as a friend.");
        }
    }
}
