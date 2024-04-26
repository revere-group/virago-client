package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.player.UpdateEvent;
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
@ModuleData(name = "Anti Bot", description = "Removes all bots", type = EnumModuleType.COMBAT)
public class AntiBot extends AbstractModule {

    public static ArrayList<EntityPlayer> bots = new ArrayList<>();

    @EventHandler
    private final Listener<UpdateEvent> playerUpdateEvent = event -> {
        List<EntityPlayer> playerEntities = mc.theWorld.playerEntities;

        for (EntityPlayer player : playerEntities) {
            if (player == null) {
                return;
            }
            if (player.getName().startsWith("\u00a7") && player.getName().contains("\u00a7c") || this.isEntityBot(player) && !player.getDisplayName().getFormattedText().contains("NPC")) {
                mc.theWorld.removeEntity(player);
            }
        }
    };

    /**
     * Method to check if the entity is a bot.
     *
     * @param entity the entity to check
     * @return true if the entity is a bot, false otherwise
     */
    private boolean isEntityBot(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        if (mc.getCurrentServerData() != null) {
            String serverIP = mc.getCurrentServerData().serverIP.toLowerCase();
            return serverIP.contains("hypixel") && entity.getDisplayName().getFormattedText().startsWith("&") || (!this.isOnTab(entity) && mc.thePlayer.ticksExisted > 100);
        }
        return false;
    }

    /**
     * Method to check if the entity is on the tab list.
     *
     * @param entity the entity to check
     * @return true if the entity is on the tab list, false otherwise
     */
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
