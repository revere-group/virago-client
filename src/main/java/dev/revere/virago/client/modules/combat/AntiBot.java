package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.util.Logger;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        List<EntityPlayer> playerEntities = mc.theWorld.playerEntities;
        int i = 0;
        int playerEntitiesSize = playerEntities.size();
        while (i < playerEntitiesSize) {
            EntityPlayer player = playerEntities.get(i);
            if (player == null) {
                return;
            }
            if (player.getName().startsWith("\u00a7") && player.getName().contains("\u00a7c") || this.isEntityBot(player) && !player.getDisplayName().getFormattedText().contains("NPC")) {
                Logger.addChatMessage("Removed bot: " + player.getName());
                mc.theWorld.removeEntity(player);
            }
            ++i;
        }
    };


    private boolean isEntityBot(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        if (mc.getCurrentServerData() != null) return AntiBot.mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel") && entity.getDisplayName().getFormattedText().startsWith("&") || !this.isOnTab(entity) && AntiBot.mc.thePlayer.ticksExisted > 100;
        return false;
    }

    private boolean isOnTab(Entity entity) {
        Iterator<NetworkPlayerInfo> iterator = mc.getNetHandler().getPlayerInfoMap().iterator();
        do {
            if (iterator.hasNext()) continue;
            return false;
        } while (!iterator.next().getGameProfile().getName().equals(entity.getName()));
        return true;
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
