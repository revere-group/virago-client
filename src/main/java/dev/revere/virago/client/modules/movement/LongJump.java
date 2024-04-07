package dev.revere.virago.client.modules.movement;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.MoveEvent;
import dev.revere.virago.client.events.player.PostMotionEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "Long Jump", description = "Jump further", type = EnumModuleType.MOVEMENT)
public class LongJump extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.WATCHDOG);

    private final Setting<Float> posY = new Setting<>("Y-Position", 0.48f)
            .minimum(0.10f)
            .maximum(1.00f)
            .incrementation(0.01f);

    private final Setting<Float> speed = new Setting<>("Speed", 0.172f)
            .minimum(0.10f)
            .maximum(1.00f)
            .incrementation(0.01f);

    private boolean spartanCanLongJump = false;
    private boolean spartanCheck = false;
    private int spartanJumps;

    private boolean canLongJump = false;
    private boolean check = false;

    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                if (!this.spartanCheck && spartanCanLongJump) {
                    boolean doubleJump = timer.hasTimeElapsed(1000L) && !this.timer.hasTimeElapsed(2000L);
                    if (doubleJump) {
                        Logger.addChatMessage("jump");
                        spartanJumps++;

                        if (mc.thePlayer.onGround) {
                            spartanJumps = 0;
                        }

                        if (spartanJumps == 0) {
                            mc.thePlayer.motionY = 1.02f;
                            Logger.addChatMessage("Stage 1");
                        }
                        if (spartanJumps == 1) {
                            mc.thePlayer.motionY = 0.43;
                            Logger.addChatMessage("Stage 2");
                        }
                        if (spartanJumps == 2) {
                            Logger.addChatMessage("Stage 3");
                            mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                            spartanJumps = -1;
                        }
                    }
                    if (this.timer.hasTimeElapsed(2000L)) {
                        timer.reset();
                        Logger.addChatMessage("Toggle");
                        toggle();
                    }
                }

                if (mc.thePlayer.onGround && (mc.thePlayer.prevPosY - mc.thePlayer.posY) > 0) {
                    this.spartanJumps++;
                }

                if (spartanCheck && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                if (event.getEventState() == PacketEvent.EventState.SENDING) {
                    if (event.getPacket() instanceof C03PacketPlayer) {
                        C03PacketPlayer packet = event.getPacket();
                        if (spartanJumps >= 3) {
                            Logger.addChatMessage("damage");
                            packet.setOnGround(true);
                            this.spartanJumps = 0;
                            this.spartanCheck = false;
                            this.spartanCanLongJump = true;
                        } else if (spartanJumps < 3) {
                            packet.setOnGround(false);
                        }
                    }
                }

                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
    };


    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<MoveEvent> moveEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                if (!spartanCheck && spartanCanLongJump) {
                    Logger.addChatMessage("set speed");
                    mc.thePlayer.setSpeed(event, 1.2);
                } else if (spartanCheck) {
                    event.setX(0);
                    event.setZ(0);
                }
                break;
            case FIREBALL:
                break;
        }
    };

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.WATCHDOG) {
            Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.ERROR, "Disabled as this module does not bypass.", this.getName());
            toggleSilent();
        }
        this.spartanJumps = 0;
        this.spartanCheck = true;
        this.canLongJump = false;
        this.check = false;
        this.timer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.timer.reset();
        super.onDisable();
    }

    enum Mode {
        WATCHDOG,
        SPARTAN,
        FIREBALL
    }
}
