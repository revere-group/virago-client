package dev.revere.virago.api.socket;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.packet.*;
import dev.revere.virago.api.protection.rank.Rank;
import dev.revere.virago.client.events.player.IRCEvent;
import dev.revere.virago.util.Logger;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static dev.revere.virago.api.socket.SocketHelper.socket;

public class SocketClient {
    public static String jwt;
    private static final String URL = "ws://89.168.45.104:7376";
    private static final String DEV = "ws://localhost:7376";
    public static String key = "";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy HH:mm");

    public static String getURL(String path) {
        return String.format("%s%s", DEV, path);
    }

    public static void init(String licenseKey) {
        key = licenseKey;

        socket(URI.create(getURL("/auth/login")), new SocketHelper.WebSocketHandler() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                send(new C2SLogin(licenseKey));
            }

            @Override
            public void onMessage(String s) {
                var packet = deserialize(s, S2CLogin.class);

                if(packet.code().equals("200")) {
                    jwt = packet.jwtToken();
                } else {
                    Minecraft.theMinecraft = null;
                    log("Result code: %s. Failure. " + packet.code());
                }
            }
        });


        refresh();
    }

    public static void refresh() {
        socket(URI.create(getURL("/chat/update")), new SocketHelper.WebSocketHandler() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                send(new C2SUpdate(key));
            }

            @Override
            public void onMessage(String s) {
                var packet = deserialize(s, S2CChat.class);
                Logger.addChatMessageNoPrefix(
                        String.format(
                                "%s[%sIRC%s]%s %s[%s%s%s]%s %s%s: %s%s",
                                EnumChatFormatting.DARK_GRAY, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.DARK_GRAY, EnumChatFormatting.RESET,
                                EnumChatFormatting.DARK_GRAY,
                                Objects.requireNonNull(Rank.getRank(packet.rank())).getColor(),
                                packet.rank(),
                                EnumChatFormatting.DARK_GRAY,
                                EnumChatFormatting.RESET,
                                Objects.requireNonNull(Rank.getRank(packet.rank())).getColor(),
                                packet.author(),
                                EnumChatFormatting.RESET,
                                packet.content()
                        )
                );
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                refresh();
            }
        });
    }
}
