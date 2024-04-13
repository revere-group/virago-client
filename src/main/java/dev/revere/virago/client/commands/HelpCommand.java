package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.CommandService;
import dev.revere.virago.util.Logger;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"help"}, description = "Display a list of commands", syntax = "help")
public class HelpCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        Logger.addChatMessage("\u00a77\u00a7m------------------------");
        for (AbstractCommand command : Virago.getInstance().getServiceManager().getService(CommandService.class).getCommands().values()) {
            String aliases = String.join(", ", command.getAliases());
            String description = command.getDescription();
            String syntax = command.getSyntax();

            Logger.addChatMessage("\u00a73" + aliases + " \u00a7f" + syntax + " \u00a77-\u00a7f " + description);
        }
        Logger.addChatMessage("\u00a77\u00a7m------------------------");
    }
}
