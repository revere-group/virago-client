package dev.revere.virago.client.modules.misc;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;


@ModuleData(name = "Client Spoofer", description = "Fake which client you are on", type = EnumModuleType.MISC)
public class ClientSpoofer extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.LUNAR);

    @EventHandler
    private final Listener<PacketEvent> onPacketEvent = event -> {
        C17PacketCustomPayload payload;
        if (event.getPacket() instanceof C17PacketCustomPayload &&
                (payload = event.getPacket()).getChannelName().equalsIgnoreCase("MC|Brand")) {

            ByteBuf message = Unpooled.buffer();

            switch(mode.getValue()) {
                case LUNAR: {
                    message.writeBytes("Lunar-Client".getBytes());
                    break;
                }

                case FORGE: {
                    message.writeBytes("FML".getBytes());
                }

                case GEYSER: {
                    message.writeBytes("eyser".getBytes());                }

            }

            event.setPacket(new C17PacketCustomPayload("REGISTER", new PacketBuffer(message)));
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private enum Mode {
        LUNAR, GEYSER, FORGE
    }

}
