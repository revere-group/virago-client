package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.util.Logger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Zion
 * @project Virago-Client
 * @date 28/04/2024
 */


@ModuleData(name = "FastUse", description = "Use potions and food faster.", type = EnumModuleType.PLAYER)
public class FastUse extends AbstractModule {

    private final Setting<Boolean> stopMoving = new Setting<>("Stop Moving", false);
    private int check;

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if(mc.thePlayer.inventory.getCurrentItem() == null) return;

        if(!mc.thePlayer.isUsingItem()) {
            mc.timer.timerSpeed = 1.0f;
            check = 0;
            return;
        }

        Item heldItem = mc.thePlayer.inventory.getCurrentItem().getItem();
        if(heldItem instanceof ItemFood || heldItem instanceof ItemPotion) {
            if(check != 20) {
                mc.timer.timerSpeed = 0.5f;
                mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C03PacketPlayer(true));
                check++;
            }
        }
    };




}
