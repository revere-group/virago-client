package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;

@ModuleData(name = "JumpReset", displayName = "Jump Reset", description = "Automatically jump reset", type = EnumModuleType.COMBAT)
public class JumpReset extends AbstractModule {
    private final Setting<Integer> chance = new Setting<>("Chance", 100)
            .minimum(1)
            .maximum(100)
            .incrementation(1);

    private final Setting<Integer> jumpDelay = new Setting<>("Jump Delay", 10)
            .minimum(1)
            .maximum(40)
            .incrementation(1);

    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        if (event.getEventState() != PacketEvent.EventState.RECEIVING) return;

        Packet<?> packet = event.getPacket();
        if (!(packet instanceof S12PacketEntityVelocity)) return;

        S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;
        Entity entity = mc.theWorld.getEntityByID(velocityPacket.getEntityID());

        if (entity == null || entity != mc.thePlayer) return;

        int jumpKey = mc.gameSettings.keyBindJump.getKeyCode();
        if (!mc.thePlayer.onGround || Keyboard.isKeyDown(jumpKey)) return;

        if (chance.getValue() < 100) {
            double roll = Math.random() * 100;
            if (roll >= chance.getValue()) return;
        }

        // if (!timer.hasTimeElapsed(jumpDelay.getValue())) return;

        mc.thePlayer.jump();
        timer.reset();
    };

}
