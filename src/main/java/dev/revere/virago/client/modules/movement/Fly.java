package dev.revere.virago.client.modules.movement;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.NotificationService;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleData(name = "Fly", displayName = "Fly", description = "Allows you to fly", type = EnumModuleType.MOVEMENT)
public class Fly extends AbstractModule {

    private final Setting<FlyMode> mode = new Setting<>("Mode", FlyMode.VANILLA)
            .describedBy("The mode of flight you will use.");

    private final Setting<Double> speed = new Setting<>("Flight Speed", 1.0)
            .minimum(0.1D)
            .maximum(10.0D)
            .incrementation(0.1D)
            .describedBy("The speed you will fly at.");

    private final Setting<Boolean> noClip = new Setting<>("NoClip", false)
            .describedBy("Allows you to fly through blocks.");

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        switch (mode.getValue()) {
            case VANILLA:
                if (noClip.getValue()) {
                    mc.thePlayer.noClip = true;
                }

                mc.thePlayer.capabilities.isFlying = true;
                mc.thePlayer.capabilities.setFlySpeed(speed.getValue().floatValue());
                break;
            case PACKET: // TODO: Make this actually work
                mc.thePlayer.onGround = false;

                double speed = this.speed.getValue();
                double forward = mc.thePlayer.movementInput.moveForward;
                double strafe = mc.thePlayer.movementInput.moveStrafe;
                float yaw = mc.thePlayer.rotationYaw;

                if (forward == 0.0D && strafe == 0.0D) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionZ = 0;
                } else {
                    if (forward != 0.0D) {
                        if (strafe > 0.0D) {
                            yaw += ((forward > 0.0D) ? -45 : 45);
                        } else if (strafe < 0.0D) {
                            yaw += ((forward > 0.0D) ? 45 : -45);
                        }
                        strafe = 0.0D;
                        if (forward > 0.0D) {
                            forward = 1.0D;
                        } else if (forward < 0.0D) {
                            forward = -1.0D;
                        }
                    }

                    double cos = Math.cos(Math.toRadians(yaw + 90.0F));
                    double sin = Math.sin(Math.toRadians(yaw + 90.0F));
                    mc.thePlayer.motionX = forward * speed * cos + strafe * speed * sin;
                    mc.thePlayer.motionZ = forward * speed * sin - strafe * speed * cos;
                }

                double newX = mc.thePlayer.posX + mc.thePlayer.motionX;
                double newY = mc.thePlayer.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D) - (mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);
                double newZ = mc.thePlayer.posZ + mc.thePlayer.motionZ;

                C03PacketPlayer.C06PacketPlayerPosLook packet = new C03PacketPlayer.C06PacketPlayerPosLook(newX, newY, newZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround);
                mc.getNetHandler().addToSendQueue(packet);
                break;
        }
    };

    @Override
    public void onEnable() {
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);

        if (noClip.getValue() && mode.getValue() != FlyMode.VANILLA) {
            notificationService.notify(NotificationType.NO, "Fly", "NoClip is not supported in packet mode.");
            noClip.setValue(false);
            return;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.thePlayer.capabilities.setFlySpeed(0.1F);
        mc.thePlayer.capabilities.isFlying = false;
        super.onDisable();
    }

    private enum FlyMode {
        VANILLA,
        PACKET,
    }

}
