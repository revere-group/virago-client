package dev.revere.virago.util.misc;

import dev.revere.virago.Virago;
import lombok.Getter;
import lombok.Setter;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRichPresence;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/3/2024
 */
@Setter
@Getter
public class DiscordRPC {
    private boolean running = true;
    private long created = 0L;

    public void start() {
        this.created = System.currentTimeMillis();

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(user -> {
            this.update("Virago Client v" + Virago.getInstance().getVersion(), "discord.gg/virago");
        }).build();

        net.arikia.dev.drpc.DiscordRPC.discordInitialize("780772474333429761", handlers, true);
        new Thread("Discord RPC Callback") {
            public void run() {
                while (running) {
                    net.arikia.dev.drpc.DiscordRPC.discordRunCallbacks();
                }
            }
        }.start();
    }

    public void shutdown() {
        this.running = false;
        net.arikia.dev.drpc.DiscordRPC.discordShutdown();
    }

    public void update(String first, String second) {
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(second);
        builder.setBigImage("logo", "");
        builder.setDetails(first);
        builder.setStartTimestamps(this.created);

        net.arikia.dev.drpc.DiscordRPC.discordUpdatePresence(builder.build());
    }
}
