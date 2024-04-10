package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.NotificationService;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.StringUtils;

@ModuleData(name = "Auto Hypixel", description = "All your hypixel needs", type = EnumModuleType.PLAYER)
public class AutoHypixel extends AbstractModule {


    private final Setting<Boolean> rejoin = new Setting<>("Rejoin", true);
    private final Setting<Boolean> autoGG = new Setting<>("AutoGG", false);

    private final Setting<GameMode> gameMode = new Setting<>("GameMode", GameMode.SW_SOLO_NORMAL).visibleWhen(rejoin::getValue);

    @EventHandler
    private final Listener<PacketEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle s45 = event.getPacket();
            if (s45.getMessage() == null) return;

            String title = StringUtils.stripControlCodes(s45.getMessage().getUnformattedText());
            if (title.equals("VICTORY!") || title.equals("YOU LOST!") || title.equals("YOU DIED!")) {
                if(autoGG.getValue()) {
                    mc.thePlayer.sendChatMessage("GG");
                }

                doRejoin();
            }
        }
    };

    private void doRejoin() {
        if(rejoin.getValue()) {
            Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.INFO, "Auto Hypixel", "You are being sent to a new " + gameMode.getValue().name().toLowerCase().replace("_", " ") + " game.");

            switch (gameMode.getValue()) {
                case SW_SOLO_NORMAL:
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_normal"));
                    break;
                case SW_SOLO_INSANE:
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_insane"));
                    break;
                case SW_TEAMS_NORMAL:
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play teams_normal"));
                    break;
                case SW_TEAMS_INSANE:
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play teams_insane"));
                    break;
            }
        }
    }

    enum GameMode {
        SW_SOLO_NORMAL,
        SW_SOLO_INSANE,
        SW_TEAMS_NORMAL,
        SW_TEAMS_INSANE,
    }
}
