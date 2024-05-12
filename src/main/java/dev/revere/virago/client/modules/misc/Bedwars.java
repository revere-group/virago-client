package dev.revere.virago.client.modules.misc;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.events.player.WorldChangeEvent;
import dev.revere.virago.util.Logger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zion
 * @project Virago
 * @date 05/05/2024
 */

@ModuleData(name = "Bedwars", displayName = "Bedwars", description = "Notify when a player buys items.", type = EnumModuleType.MISC)
public class Bedwars extends AbstractModule {

    private final Setting<Boolean> armor = new Setting<>("Armor", true);
    private final Setting<Boolean> heldItems = new Setting<>("Held Items", true);
    private final Setting<Boolean> ping = new Setting<>("Ping Sound", true);

    private final List<String> armoredPlayers = new ArrayList<>();
    private final Map<String, String> heldItemMap = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        armoredPlayers.clear();
        heldItemMap.clear();
    }

    @EventHandler
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        armoredPlayers.clear();
        heldItemMap.clear();
    };

    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
        Logger.addChatMessage("hello 1");
        if (!armor.getValue() || !heldItems.getValue())
            return;

        System.out.println("check 1");

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == null) continue;
            //if(player == mc.thePlayer) continue;

            String name = player.getName();
            ItemStack item = player.getHeldItem();

            if (armor.getValue()) {
                ItemStack leggings = player.inventory.armorInventory[1];
                String armorType = null;

                if (leggings.getItem() == null || leggings.getDisplayName().contains("LEATHER"))
                    continue;

                Logger.addChatMessage(leggings.getUnlocalizedName());

                switch (leggings.getUnlocalizedName()) {
                    case "item.minecraft.diamond_leggings":
                        armorType = "Diamond";
                        break;
                    case "item.minecraft.iron_leggings":
                        armorType = "Iron";
                        break;
                    case "item.minecraft.chainmail_leggings":
                        armorType = "Chain";
                        break;
                }

                if (armorType == null) continue;
                if (armoredPlayers.contains(name)) continue;

                armoredPlayers.add(name);
                Logger.addChatMessage("Armor: " + player.getDisplayName().getFormattedText() + " has purchased " + armorType + " Armor");
                ping();
            }

            if (heldItems.getValue()) {
                Logger.addChatMessage(item.getUnlocalizedName());
                if (item != null && !heldItemMap.containsKey(name)) {
                    String itemType = null;

                    switch (item.getUnlocalizedName()) {
                        case "OBSIDIAN":
                            itemType = "Obsidian";
                            break;
                        case "END_STONE":
                            itemType = "End Stone";
                            break;
                        case "item.minecraft.fire_charge":
                            itemType = "Fireball";
                            break;
                        case "item.minecraft.diamond_sword":
                            itemType = "Diamond Sword";
                            break;
                        case "item.minecraft.iron_sword":
                            itemType = "Iron Sword";
                            break;
                        case "item.minecraft.stone_sword":
                            itemType = "Stone Sword";
                            break;
                    }

                    if (itemType == null) continue;
                    heldItemMap.put(name, itemType);

                    double distance = Math.round(mc.thePlayer.getDistanceToEntity(player));
                    Logger.addChatMessage("Items: " + player.getDisplayName().getFormattedText() + " is holding: " + itemType + " (" + distance + ")");
                    ping();
                }
            }
        }
    };

    private void ping() {
        if (ping.getValue()) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }
    }

    @Override
    public void onDisable() {

    }
}
