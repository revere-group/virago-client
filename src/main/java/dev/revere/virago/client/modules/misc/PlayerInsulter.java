package dev.revere.virago.client.modules.misc;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.RandomUtils;

@ModuleData(name = "Insults", description = "Insult players that you kill", type = EnumModuleType.MISC)
public class PlayerInsulter extends AbstractModule {
    private EntityLivingBase target;

    private final String[] insults = {
            "I'd say GG, but it wasn't really a game when you were involved. More like a one-sided slaughter.",
            "Are you trying to PvP or bake a cake? Because you're just getting creamed.",
            "You've been upgraded to a first class spectator seat.",
            "Why would someone as awful as you still play legit?",
            "You're not just a clown; you're the entire circus.",
            "Somewhere, there's a tree tirelessly producing oxygen for you. I think you owe it an apology.",
            "Your family tree must be a cactus because everyone on it is a prick.",
    };


    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        if(killAura.isEnabled() && killAura.getSingleTarget() != null)
            target = killAura.getSingleTarget();


        if (target == null)
            return;


        if(mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100)
            return;

        String insult = insults[RandomUtils.nextInt(0, insults.length)];
        mc.thePlayer.sendChatMessage(insult);


        this.target = null;
    };


    @EventHandler
    private final Listener<AttackEvent> attackEventListener = event -> {
        Entity entity = event.getTarget();

        if(!(entity instanceof EntityLivingBase))
            return;

        target = (EntityLivingBase) entity;
    };
}
