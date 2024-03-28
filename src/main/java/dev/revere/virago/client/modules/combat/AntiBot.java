package dev.revere.virago.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.update.UpdateEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;

/**
 * @author Remi
 * @project Virago
 * @date 3/28/2024
 */
@ModuleData(name = "AntiBot", description = "Removes all bots", type = EnumModuleType.COMBAT)
public class AntiBot extends AbstractModule {

    public static ArrayList<EntityPlayer> bots = new ArrayList<>();

    @EventHandler
    private final Listener<UpdateEvent> playerUpdateEvent = event -> {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                if (entity != mc.thePlayer && !((EntityPlayer) entity).isSpectator()) {
                    if (isBot((EntityLivingBase) entity) ) {
                        mc.theWorld.removeEntity(entity);
                    }
                } else {
                    bots.remove(entity);
                }
            }
        }
    };

    private boolean isBot(EntityLivingBase entity) {
        return (entity.isInvisible() && !entity.onGround && entity.isPotionActive(14) == false) || entity.motionY == 0 && !entity.onGround;
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
