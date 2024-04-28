package dev.revere.virago.api.network.socket;

import dev.revere.virago.api.network.packet.client.C2SLogin;
import dev.revere.virago.api.network.packet.client.C2SUpdate;
import dev.revere.virago.api.network.packet.server.S2CChat;
import dev.revere.virago.api.network.packet.server.S2CLogin;
import dev.revere.virago.api.protection.rank.Rank;
import dev.revere.virago.util.Logger;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Objects;

import static dev.revere.virago.api.network.socket.SocketHelper.createSocketConnection;

public class SocketClient {
    public static String jwt;
    private static String URL = "ws://89.168.45.104:7376";
    private static String DEV = "ws://localhost:7376";
    public static String key = "";

    public static void init(String licenseKey) {
        key = licenseKey;

        createSocketConnection(URI.create(getURL("/auth/login")), new SocketHelper.WebSocketHandler() {
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
        createSocketConnection(URI.create(getURL("/chat/update")), new SocketHelper.WebSocketHandler() {
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

    public static String getURL(String path) {
        return String.format("%s%s", URL, path);
    }
}
