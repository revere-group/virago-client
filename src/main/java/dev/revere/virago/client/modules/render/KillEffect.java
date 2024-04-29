package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.sound.SoundUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.EnumParticleTypes;
import org.lwjgl.input.Keyboard;

@Getter
@Setter
@ModuleData(name = "KillEffect", displayName = "Kill Effect", type = EnumModuleType.RENDER, description = "Displays an effect when a player is killed")
public class KillEffect extends AbstractModule {

    public Setting<Boolean> lightning = new Setting<>("Lightning", true).describedBy("Whether lightning should be enabled.");
    public Setting<Boolean> explosion = new Setting<>("Explosion", true).describedBy("Whether explosions should be enabled.");
    private EntityLivingBase target;

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        if(killAura.isEnabled() && killAura.getSingleTarget() != null) {
            target = killAura.getSingleTarget();
        }
        if (target == null) {
            return;
        }

        if(mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100) {
            return;
        }

        if (mc.thePlayer.ticksExisted > 3) {
            if (lightning.getValue()) {
                EntityLightningBolt lightningBolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                mc.theWorld.addEntityToWorld((int) (-Math.random() * 100000), lightningBolt);
                SoundUtil.playSound("ambient.weather.thunder");
            }

            if (explosion.getValue()) {
                for (int i = 0; i <= 8; i++) {
                    mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
                }

                SoundUtil.playSound("item.fireCharge.use");
            }
        }

        this.target = null;
    };

    @EventHandler
    private final Listener<AttackEvent> attackEventListener = event -> {
        Entity entity = event.getTarget();

        if(!(entity instanceof EntityLivingBase))
            return;

        target = (EntityLivingBase) entity;
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
