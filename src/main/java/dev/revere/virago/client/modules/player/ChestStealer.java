package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.util.player.ItemUtil;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.*;
import net.optifine.util.MathUtils;

/**
 * @author Remi
 * @project Virago-Client
 * @date 3/28/2024
 */
@ModuleData(name = "Chest Stealer", description = "Steals items from players", type = EnumModuleType.PLAYER)
public class ChestStealer extends AbstractModule {

    private final Setting<Long> maxDelay = new Setting<>("Max Delay", 50L)
            .minimum(0L)
            .maximum(500L)
            .incrementation(1L)
            .describedBy("The amount of times to loot per second");
    private final Setting<Long> minDelay = new Setting<>("Min Delay", 30L)
            .minimum(0L)
            .maximum(500L)
            .incrementation(1L)
            .describedBy("The amount of times to loot per second");

    private final Setting<Boolean> ignoreTrash = new Setting<>("Ignore Trash", true).describedBy("Ignore trash items in containers");

    private final TimerUtil timer = new TimerUtil();
    private long nextClick;
    private int lastClick;
    private int lastSteal;

    @EventHandler
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.currentScreen instanceof GuiChest) {
            final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

            if ((container.getLowerChestInventory().getName().toLowerCase().contains("menu") || container.getLowerChestInventory().getName().toLowerCase().contains("play") || container.getLowerChestInventory().getName().toLowerCase().contains("selector"))) {
                return;
            }

            if (!this.timer.hasTimeElapsed(this.nextClick)) {
                return;
            }

            this.lastSteal++;

            for (int i = 0; i < container.inventorySlots.size(); i++) {
                final ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);

                if (stack == null || this.lastSteal <= 1) {
                    continue;
                }

                if (this.ignoreTrash.getValue() && !ItemUtil.isValidItem(stack)) {
                    continue;
                }

                this.nextClick = Math.round(MathUtils.randomNumber(this.maxDelay.getValue().intValue(), this.minDelay.getValue().intValue()));
                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);

                this.timer.reset();
                this.lastClick = 0;
                return;
            }

            this.lastClick++;

            if (this.lastClick > 1) {
                mc.thePlayer.closeScreen();
            }
        } else {
            this.lastClick = 0;
            this.lastSteal = 0;
        }
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
