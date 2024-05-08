package dev.revere.virago.client.modules.combat;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.NotificationService;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "Criticals", displayName = "Criticals", description = "Always Criticals", type = EnumModuleType.COMBAT)
public class Criticals extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.WATCHDOG);


    private final Setting<Float> delay = new Setting<>("Range", 3.0F)
            .minimum(0F)
            .maximum(1000F)
            .incrementation(50F)
            .describedBy("Delay");


    private final StopWatch stopwatch = new StopWatch();

    private boolean attacked;
    private int ticks;
    private int offGroundTicks;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {

        if(!mc.thePlayer.onGround) {
            offGroundTicks++;
        }else{
            offGroundTicks = 0;
        }
        switch (mode.getValue()) {
            case WATCHDOG2: {
                event.setGround(false);
                break;
            }
            case WATCHDOG: {
                if (attacked) {
                    ticks++;

                    switch (ticks) {
                        case 1: {
                            if(mc.thePlayer.onGround)
                                mc.thePlayer.motionY = .2f - Math.random() / 1000f;
                            break;
                        }

                        case 2: {
                            if(offGroundTicks == 1)
                                mc.thePlayer.motionY -= 0.1f - Math.random() / 1000f;;
                            break;
                        }
                    }

                    event.setGround(false);
                } else {
                    attacked = false;
                    ticks = 0;
                }
                break;
            }
        }
    };

    @EventHandler
    public final Listener<AttackEvent> onAttackEvent = event -> {
        if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && stopwatch.equals(delay.getValue().longValue())) {
            mc.thePlayer.onCriticalHit(event.getTarget());

            stopwatch.reset();
            attacked = true;
        }
    };

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    enum Mode {
        EDIT,
        WATCHDOG,
        WATCHDOG2
    }
}
