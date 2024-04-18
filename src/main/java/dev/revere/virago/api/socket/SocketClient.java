package dev.revere.virago.api.socket;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.packet.C2SChat;
import dev.revere.virago.api.packet.C2SLogin;
import dev.revere.virago.api.packet.S2CChat;
import dev.revere.virago.api.packet.S2CLogin;
import dev.revere.virago.api.protection.rank.Rank;
import dev.revere.virago.client.events.player.IRCEvent;
import dev.revere.virago.util.Logger;
import lombok.var;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.revere.virago.api.socket.SocketHelper.socket;

public class SocketClient {
    public static String jwt;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy HH:mm");

    public static void init(String licenseKey) {
        socket(URI.create("ws://localhost:7376/auth/login"), new SocketHelper.WebSocketHandler() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("Opened connection");

                send(new C2SLogin(licenseKey));
            }

            @Override
            public void onMessage(String s) {
                var packet = deserialize(s, S2CLogin.class);

                if(packet.code().equals("200")) {
                    jwt = packet.jwtToken();
                    System.out.println("Logged in successfully");
                } else {
                    System.out.printf("Result code: %s. Failure. %n", packet.code());
                }
            }
        });


        refresh();
    }

    public static void refresh() {
        socket(URI.create("ws://localhost:7376/chat/update"), new SocketHelper.WebSocketHandler() {
            @Override
            public void onMessage(String s) {
                var packet = deserialize(s, S2CChat.class);
                System.out.printf("[IRC] [%s] [%s] [%s]: %s%n", sdf.format(new Date(packet.timestamp())), packet.rank(), packet.author(), packet.content());
                Logger.addChatMessage(
                        String.format(
                                "%s[%sIRC%s]%s %s[%s%s%s]%s [%s]: %s",
                                EnumChatFormatting.BLACK, EnumChatFormatting.BLUE, EnumChatFormatting.BLACK, EnumChatFormatting.RESET,
                                EnumChatFormatting.BLACK,
                                Rank.getRank(packet.rank()).getColor(),
                                packet.rank(),
                                EnumChatFormatting.BLACK,
                                EnumChatFormatting.RESET,
                                packet.author(), packet.content()
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
