package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@CommandData(aliases = {"setting"}, description = "Change a setting", syntax = "<module> <setting> <value>")
public class SettingCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);
        notificationService.notify(NotificationType.ERROR, "Command Manager", "This command is not yet implemented.");
    }
}
