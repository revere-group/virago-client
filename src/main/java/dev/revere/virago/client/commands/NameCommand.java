package dev.revere.virago.client.commands;

import dev.revere.virago.Virago;
import dev.revere.virago.api.command.AbstractCommand;
import dev.revere.virago.api.command.CommandData;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.CommandService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author Zion
 * @project Virago-Client
 * @date 28/04/2024
 */
@CommandData(aliases = {"name"}, description = "Copy your name to clipboard.", syntax = "help")
public class NameCommand extends AbstractCommand {

    @Override
    public void executeCommand(String line, String[] args) {
        String sessionName = Minecraft.getMinecraft().getSession().getUsername();
        copyToClipboard(sessionName);

        Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.INFO, "Name Copied", "Copied " + sessionName + " to clipboard");
    }

    private void copyToClipboard(String toCopy) {
        StringSelection selection = new StringSelection(toCopy);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}
