package dev.revere.virago.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.update.UpdateEvent;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */

@ModuleData(name = "Speed6", description = "Increases your movement speed", type = EnumModuleType.MOVEMENT)
public class Speed6 extends AbstractModule {

    private final Setting<Float> speed = new Setting<>("Speed", 0.5f)
            .minimum(0.1F)
            .maximum(1.0F)
            .incrementation(0.1F)
            .describedBy("The speed to set the player to");

    public Speed6() {
        setKey(Keyboard.KEY_V);
    }

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        //setSpeed2(speed.getValue());
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void setSpeed2(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
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
        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }

        // Calculate the player's motion.
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));

        // Set the player's motion.
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }
}
