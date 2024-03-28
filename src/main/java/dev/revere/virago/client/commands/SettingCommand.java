package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"setting"}, description = "Change a setting", syntax = "setting <module> <setting> <value>")
public class SettingCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        if (args.length < 4) {
            Logger.addChatMessage("Usage: " + getSyntax());
            return;
        }

        String moduleName = args[1];
        String settingName = args[2];
        String value = args[3];

        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);
        if (moduleService.getModuleByName(moduleName) == null) {
            Logger.addChatMessage("Module not found!");
            return;
        }
    }
}
