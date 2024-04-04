package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;


@ModuleData(name = "AutoHypixel", description = "All your hypixel needs", type = EnumModuleType.PLAYER)
public class AutoHypixel extends AbstractModule {


    private final Setting<Boolean> rejoin = new Setting<>("Rejoin", true);
    private final Setting<Boolean> autoGG = new Setting<>("AutoGG", false);

    @EventHandler
    private final Listener<PacketEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle s45 = event.getPacket();
            if (s45.getMessage() == null) return;

            if (StringUtils.stripControlCodes(s45.getMessage().getUnformattedText()).equals("VICTORY!") ) {
                if(autoGG.getValue())
                    mc.thePlayer.sendChatMessage("GG");

                if(rejoin.getValue())
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_normal"));

            } else if(StringUtils.stripControlCodes(s45.getMessage().getUnformattedText()).equals("YOU DIED!")) {
                if(rejoin.getValue())
                    mc.getNetHandler().addToSendQueue(new C01PacketChatMessage("/play solo_normal"));
            }
        }
    };
}
