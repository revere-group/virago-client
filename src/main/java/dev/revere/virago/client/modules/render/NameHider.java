package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;

@ModuleData(name = "Name Hider", description = "Hide your name.", type = EnumModuleType.RENDER)
public class NameHider extends AbstractModule {
    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
        String username = Virago.getInstance().getViragoUser().getUsername().toLowerCase();

        if(!(event.getPacket() instanceof S02PacketChat))
            return;

        S02PacketChat packet = event.getPacket();
        if(packet.getChatComponent().getUnformattedText().contains(mc.getSession().getUsername()))
            packet.chatComponent = new ChatComponentText(packet.getChatComponent().getFormattedText().replaceAll(mc.getSession().getUsername(), username));
    };


}
