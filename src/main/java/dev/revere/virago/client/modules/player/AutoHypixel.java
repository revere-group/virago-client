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
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.StringUtils;

@ModuleData(name = "AutoHypixel", displayName = "Auto Hypixel", description = "All your hypixel needs", type = EnumModuleType.PLAYER)
public class AutoHypixel extends AbstractModule {


    private final Setting<Boolean> rejoin = new Setting<>("Rejoin", true);
    private final Setting<Boolean> autoGG = new Setting<>("AutoGG", false);

    private final Setting<GameType> gameTypeProperty = new Setting<>("Game Type", GameType.SKYWARS);
    private final Setting<SkywarsTypes> skywarsTypes = new Setting<>("Skywars", SkywarsTypes.SOLO_NORMAL).visibleWhen(() -> rejoin.getValue() && gameTypeProperty.getValue() == GameType.SKYWARS);
    private final Setting<BedwarsTypes> bedwarsTypes = new Setting<>("Bedwars", BedwarsTypes.FOURS).visibleWhen(() -> rejoin.getValue() && gameTypeProperty.getValue() == GameType.BEDWARS);

    @EventHandler
    private final Listener<PacketEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle s45 = event.getPacket();
            if (s45.getMessage() == null) return;

            String title = StringUtils.stripControlCodes(s45.getMessage().getUnformattedText());
            if (title.equals("VICTORY!") || title.equals("YOU LOST!") || (title.equals("YOU DIED!")) && gameTypeProperty.getValue() == GameType.SKYWARS) {
                if(autoGG.getValue()) {
                    mc.thePlayer.sendChatMessage("GG");
                }

                doRejoin();
            }
        }
    };

    private void doRejoin() {
        if(rejoin.getValue()) {
            Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.INFO, "Auto Hypixel", "You are being sent to a new " + skywarsTypes.getValue().name().toLowerCase().replace("_", " ") + " game.", 3000L);

            if (gameTypeProperty.getValue() == GameType.SKYWARS) {
                switch (skywarsTypes.getValue()) {
                    case SOLO_NORMAL:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_normal"));
                        break;
                    case SOLO_INSANE:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_insane"));
                        break;
                    case TEAMS_NORMAL:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play teams_normal"));
                        break;
                    case TEAMS_INSANE:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play teams_insane"));
                        break;
                }
            } else {
                switch (bedwarsTypes.getValue()) {
                    case SOLO:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play bedwars_eight_one"));
                        break;
                    case DOUBLES:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play bedwars_eight_two"));
                        break;
                    case FOURS_THREE:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play bedwars_four_three"));
                        break;
                    case FOURS:
                        mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play bedwars_four_four"));
                        break;
                }
            }
        }
    }

    enum GameType {
        SKYWARS,
        BEDWARS
    }

    enum SkywarsTypes {
        SOLO_NORMAL,
        SOLO_INSANE,
        TEAMS_NORMAL,
        TEAMS_INSANE,
    }

    enum BedwarsTypes {
        SOLO,
        DOUBLES,
        FOURS_THREE,
        FOURS
    }
}
