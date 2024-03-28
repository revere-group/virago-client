package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"toggle"}, description = "Toggle a module", syntax = "toggle <module>")
public class ToggleCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        if (args.length < 2) {
            Logger.addChatMessage("Please provide a module name! Usage: " + getSyntax());
            return;
        }

        String moduleName = args[1];

        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);
        if (moduleService.getModuleByName(moduleName) == null) {
            Logger.addChatMessage("Module not found!");
            return;
        }

        String toggled = moduleService.getModuleByName(moduleName).isEnabled() ? "disabled" : "enabled";
        moduleService.getModuleByName(moduleName).toggle();

        Logger.addChatMessage(moduleName + " has been " + toggled);
    }
}
