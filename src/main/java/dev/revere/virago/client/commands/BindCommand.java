package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"bind"}, description = "Bind a module to a key", syntax = "bind <add/remove> <module> <key>")
public class BindCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        if (args.length < 3) {
            Logger.addChatMessage("Usage: " + getSyntax());
            return;
        }

        String action = args[1];
        String moduleName = args[2];

        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);
        if (moduleService.getModuleByName(moduleName) == null) {
            Logger.addChatMessage("Module not found!");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            if (args.length < 4) {
                Logger.addChatMessage("Usage: bind add <module> <key>");
                return;
            }
            String key = args[3];
            moduleService.getModuleByName(moduleName).setKey(Keyboard.getKeyIndex(key.toUpperCase()));
            Logger.addChatMessage("Bound " + moduleName + " to " + key);
        } else if (action.equalsIgnoreCase("remove")) {
            moduleService.getModuleByName(moduleName).setKey(Keyboard.KEY_NONE);
            Logger.addChatMessage("Unbound " + moduleName);
        } else {
            Logger.addChatMessage("Usage: " + getSyntax());
        }
    }
}
