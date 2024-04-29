package dev.revere.virago.client.modules.combat;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.rotation.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/17/2024
 */
@ModuleData(name = "AntiFireball", displayName = "Anti Fireball", description = "Prevents fireballs from hitting you", type = EnumModuleType.COMBAT)
public class AntiFireball extends AbstractModule {

    private final Setting<Float> range = new Setting<>("Range", 3.0F)
            .minimum(3.0F)
            .maximum(6.0F)
            .incrementation(0.1F)
            .describedBy("The range to hit the fireball");

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        EntityFireball fireball = getFireball();
        if (fireball == null) return;

        float[] rotations = RotationUtil.getRotationFromPosition(fireball.posX, fireball.posY, fireball.posZ);
        event.setYaw(rotations[0]);
        event.setPitch(rotations[1]);

        mc.thePlayer.renderYawOffset = rotations[0];
        mc.thePlayer.rotationYawHead = rotations[0];
        mc.thePlayer.rotationPitchHead = rotations[1];

        if (killAura.getSingleTarget() == null) {
            mc.thePlayer.swingItem();
            mc.playerController.attackEntity(mc.thePlayer, fireball);
        }
    };

    private EntityFireball getFireball() {
        EntityFireball fireball = null;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!entity.getName().equals(mc.thePlayer.getName()) && entity instanceof EntityFireball && mc.thePlayer.getDistanceToEntity(entity) <= range.getValue()) {
                final double distance = mc.thePlayer.getDistanceToEntity(entity);
                if (distance <= range.getValue()) {
                    fireball = (EntityFireball) entity;
                }
            }
        }

        return fireball;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
