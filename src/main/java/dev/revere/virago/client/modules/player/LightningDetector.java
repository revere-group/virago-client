package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.NotificationService;
import net.minecraft.network.play.server.S29PacketSoundEffect;

import java.util.Objects;

@ModuleData(name = "Lightning Detector", description = "Detects kill lightning", type = EnumModuleType.PLAYER)
public class LightningDetector extends AbstractModule {

    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
        S29PacketSoundEffect packet;

        if (event.getPacket() instanceof S29PacketSoundEffect &&
                Objects.equals((packet = event.getPacket()).getSoundName(), "ambient.weather.thunder")) {

            // fix it calling this packet twice??
            // not sure why? minecraft bullshit.

            Virago.getInstance().getServiceManager().getService(NotificationService.class)
                    .notify(NotificationType.INFO, "Lightning", String.format("Struck at: %s, %s, %s", Math.round(packet.getX()), Math.round(packet.getY()), Math.round(packet.getZ())));
        }
    };
}
