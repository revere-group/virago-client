package dev.revere.virago.client.modules.misc;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.RandomUtils;

@ModuleData(name = "Insults", description = "Insult players that you kill", type = EnumModuleType.MISC)
public class PlayerInsulter extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.MSG)
            .describedBy("The mode to use for the insults.");

    private EntityLivingBase target;

    private final String[] insults = {
            "I'd say GG, but it wasn't really a game when you were involved. More like a one-sided slaughter.",
            "Are you trying to PvP or bake a cake? Because you're just getting creamed.",
            "You've been upgraded to a first class spectator seat.",
            "Why would someone as awful as you still play legit?",
            "You're not just a clown; you're the entire circus.",
            "Somewhere, there's a tree tirelessly producing oxygen for you. I think you owe it an apology.",
            "Your family tree must be a cactus because everyone on it is a prick.",
            "You're the reason the gene pool needs a lifeguard.",
            "You're the reason the average sperm count is dropping.",
            "Seems like you lost, but you're used to that, aren't you?",
            "Might want to buy Virago Client to help you out next time.",
            "Virago Client seems to be the only thing that can help you.",
            "You still keep losing? Maybe you should try Virago Client.",
            "Jeez, you're bad. Maybe you should try Virago Client.",
            "What client are you using? It's not working. Try Virago Client.",
            "What are you doing man? Do you usually lose this much? Try Virago Client.",
    };

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        if (mode.getValue() != Mode.DISTANCE) {
            return;
        }

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

        String insult = insults[RandomUtils.nextInt(0, insults.length)];
        mc.thePlayer.sendChatMessage("/ac " + insult);
        this.target = null;
    };

    @EventHandler
    public final Listener<PacketEvent> onPacketReceiveEvent = event -> {
        if (mode.getValue() != Mode.MSG) {
            return;
        }

        if (event.getPacket() instanceof S02PacketChat) {
            KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
            if(killAura.isEnabled() && killAura.getSingleTarget() != null) {
                target = killAura.getSingleTarget();
            }

            if (target == null) {
                return;
            }

            S02PacketChat s02 = event.getPacket();
            String message = s02.getChatComponent().getUnformattedText();
            if (message.contains("killed by " + mc.thePlayer.getName())) {
                String insult = insults[RandomUtils.nextInt(0, insults.length)];
                mc.thePlayer.sendChatMessage(insult);
                this.target = null;
            }
        }
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

    private enum Mode {
        MSG,
        DISTANCE
    }
}
