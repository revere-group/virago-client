package dev.revere.virago.util;

import dev.revere.virago.Virago;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
public class Logger {
    /**
     * Send information to the console
     *
     * @param message message to send
     * @param type class of the message
     */
    public static void info(String message, Class<?> type) {
        String date = new SimpleDateFormat("hh:mm:ss").format(new Date());
        LogManager.getLogger(Virago.getInstance().getName()).info("[VIRAGO] [" + date + "] INFO " + type.getSimpleName() + ": " + message);
    }

    /**
     * Sends a warning to the console
     *
     * @param message message to send
     * @param type class of the message
     */
    public static void warn(String message, Class<?> type) {
        String date = new SimpleDateFormat("hh:mm:ss").format(new Date());
        LogManager.getLogger(Virago.getInstance().getName()).warn("[VIRAGO] [" + date + "] WARN " + type.getSimpleName() + ": " + message);
    }

    /**
     * Sends an error to the console
     *
     * @param message message to send
     * @param type class of the message
     */
    public static void err(String message, Class<?> type) {
        String date = new SimpleDateFormat("hh:mm:ss").format(new Date());
        LogManager.getLogger(Virago.getInstance().getName()).fatal("[VIRAGO] [" + date + "] ERROR " + type.getSimpleName() + ": " + message);
    }

    /**
     * Sends a chat message to the player
     *
     * @param message message to send
     */
    public static void addChatMessage(String message) {
        if (message != null && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_AQUA + Virago.getInstance().getName() + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RESET + message));
        }
    }

    /**
     * Sends a chat message to the player without the prefix
     *
     * @param message message to send
     */
    public static void addChatMessageNoPrefix(String message) {
        if (message != null && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }
}