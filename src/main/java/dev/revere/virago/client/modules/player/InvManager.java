package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.window.WindowClickEvent;
import dev.revere.virago.client.events.player.window.WindowClickRequest;
import dev.revere.virago.client.events.update.PreMotionEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.InventoryUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
@ModuleData(name = "Inventory Manager", description = "Manages your inventory", type = EnumModuleType.PLAYER)
public class InvManager extends AbstractModule {

    private final Setting<Boolean> ignoreCustomItems = new Setting<>("Ignore Custom Items", true);
    private final Setting<Boolean> dropItems = new Setting<>("Drop Items", true);
    private final Setting<Boolean> autoArmor = new Setting<>("Auto Armor", true);
    private final Setting<Boolean> autoSort = new Setting<>("Auto Sort", true);
    private final Setting<Double> delay = new Setting<>("Delay", 0.5D)
            .minimum(0.0D)
            .maximum(50.0D)
            .incrementation(1.0D);

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.WHILE_OPEN);

    private final int[] bestArmorPieces = new int[4];
    private final List<Integer> trash = new ArrayList<>();
    private final int[] bestToolSlots = new int[3];
    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private int bestSwordSlot;
    private int bestBowSlot;

    private final List<WindowClickRequest> clickRequests = new ArrayList<>();

    private boolean serverOpen;
    private boolean clientOpen;

    private int ticksSinceLastClick;

    private boolean nextTickCloseInventory;

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = e -> {
        final Packet<?> packet = e.getPacket();
        switch (e.getEventState()) {
            case SENDING:{
                if (packet instanceof C16PacketClientStatus) {
                    final C16PacketClientStatus clientStatus = (C16PacketClientStatus) packet;

                    if (clientStatus.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                        this.clientOpen = true;
                        this.serverOpen = true;
                    }
                } else if (packet instanceof C0DPacketCloseWindow) {
                    final C0DPacketCloseWindow packetCloseWindow = (C0DPacketCloseWindow) packet;

                    if (packetCloseWindow.getWindowId() == mc.thePlayer.inventoryContainer.windowId) {
                        this.clientOpen = false;
                        this.serverOpen = false;
                    }
                }
                break;
            }
            case RECEIVING:{
                if (packet instanceof S2DPacketOpenWindow) {
                    this.clientOpen = false;
                    this.serverOpen = false;
                }
                break;
            }
        }
    };

    @EventHandler
    private final Listener<WindowClickEvent> onWindowClick = event -> {
        this.ticksSinceLastClick = 0;
    };

    private boolean dropItem(final List<Integer> listOfSlots) {
        if (this.dropItems.getValue()) {
            if (!listOfSlots.isEmpty()) {
                int slot = listOfSlots.remove(0);
                InventoryUtil.windowClick(mc, slot, 1, InventoryUtil.ClickType.DROP_ITEM);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private final Listener<PreMotionEvent> onUpdate = event -> {
        this.ticksSinceLastClick++;
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);

        if (this.ticksSinceLastClick < Math.floor(this.delay.getValue() / 3)) return;

        if (killAura.isEnabled() && (killAura.getSingleTarget() != null)) {
            if (this.nextTickCloseInventory) {
                this.nextTickCloseInventory = false;
            }

            this.close();
            return;
        }

        if (this.clientOpen || (mc.currentScreen == null && this.mode.getValue() != Mode.WHILE_OPEN)) {
            this.clear();

            for (int slot = InventoryUtil.INCLUDE_ARMOR_BEGIN; slot < InventoryUtil.END; slot++) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                if (stack != null) {
                    if (this.ignoreCustomItems.getValue() &&
                            stack.hasDisplayName())
                        continue;

                    if (stack.getItem() instanceof ItemSword && InventoryUtil.isBestSword(mc.thePlayer, stack)) {
                        this.bestSwordSlot = slot;
                    }
                    else if (stack.getItem() instanceof ItemTool && InventoryUtil.isBestTool(mc.thePlayer, stack)) {
                        final int toolType = InventoryUtil.getToolType(stack);
                        if (toolType != -1 && slot != this.bestToolSlots[toolType])
                            this.bestToolSlots[toolType] = slot;
                    }
                    else if (stack.getItem() instanceof ItemArmor && InventoryUtil.isBestArmor(mc.thePlayer, stack)) {
                        final ItemArmor armor = (ItemArmor) stack.getItem();

                        final int pieceSlot = this.bestArmorPieces[armor.armorType];

                        if (pieceSlot == -1 || slot != pieceSlot)
                            this.bestArmorPieces[armor.armorType] = slot;
                    }
                    else if (stack.getItem() instanceof ItemBow && InventoryUtil.isBestBow(mc.thePlayer, stack)) {
                        if (slot != this.bestBowSlot)
                            this.bestBowSlot = slot;
                    }
                    else if (stack.getItem() instanceof ItemAppleGold) {
                        this.gappleStackSlots.add(slot);
                    }
                    else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                        this.trash.add(slot);
                    }
                }
            }

            final boolean busy = (!this.trash.isEmpty() && this.dropItems.getValue()) || this.equipArmor(false) || this.sortItems(false) || !this.clickRequests.isEmpty();

            if (!busy) {
                if (this.nextTickCloseInventory) {
                    this.close();
                    this.nextTickCloseInventory = false;
                } else {
                    this.nextTickCloseInventory = true;
                }
                return;
            } else {
                boolean waitUntilNextTick = !this.serverOpen;

                this.open();

                if (this.nextTickCloseInventory)
                    this.nextTickCloseInventory = false;

                if (waitUntilNextTick) return;
            }

            if (!this.clickRequests.isEmpty()) {
                final WindowClickRequest request = this.clickRequests.remove(0);
                request.performRequest();
                request.onCompleted();
                return;
            }

            if (this.equipArmor(true)) return;
            if (this.dropItem(this.trash)) return;
            this.sortItems(true);
        }
    };

    private boolean sortItems(final boolean moveItems) {
        if (this.autoSort.getValue()) {
            if (this.bestSwordSlot != -1) {
                if (this.bestSwordSlot != 36) {
                    if (moveItems) {
                        this.putItemInSlot(36, this.bestSwordSlot);
                        this.bestSwordSlot = 36;
                    }

                    return true;
                }
            }

            if (this.bestBowSlot != -1) {
                if (this.bestBowSlot != 38) {
                    if (moveItems) {
                        this.putItemInSlot(38, this.bestBowSlot);
                        this.bestBowSlot = 38;
                    }
                    return true;
                }
            }

            if (!this.gappleStackSlots.isEmpty()) {
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));

                final int bestGappleSlot = this.gappleStackSlots.get(0);

                if (bestGappleSlot != 37) {
                    if (moveItems) {
                        this.putItemInSlot(37, bestGappleSlot);
                        this.gappleStackSlots.set(0, 37);
                    }
                    return true;
                }
            }

            final int[] toolSlots = {39, 40, 41};

            for (final int toolSlot : this.bestToolSlots) {
                if (toolSlot != -1) {
                    final int type = InventoryUtil.getToolType(mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());

                    if (type != -1) {
                        if (toolSlot != toolSlots[type]) {
                            if (moveItems) {
                                this.putToolsInSlot(type, toolSlots);
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean equipArmor(boolean moveItems) {
        if (this.autoArmor.getValue()) {
            for (int i = 0; i < this.bestArmorPieces.length; i++) {
                final int piece = this.bestArmorPieces[i];

                if (piece != -1) {
                    int armorPieceSlot = i + 5;
                    final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                    if (stack != null)
                        continue;

                    if (moveItems)
                        InventoryUtil.windowClick(mc, piece, 0, InventoryUtil.ClickType.SHIFT_CLICK);

                    return true;
                }
            }
        }

        return false;
    }

    private void putItemInSlot(final int slot, final int slotIn) {
        InventoryUtil.windowClick(mc, slotIn,
                slot - 36,
                InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        final int toolSlot = toolSlots[tool];

        InventoryUtil.windowClick(mc, this.bestToolSlots[tool],
                toolSlot - 36,
                InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        this.bestToolSlots[tool] = toolSlot;
    }

    private static boolean isValidStack(final ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && InventoryUtil.isStackValidToPlace(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemPotion && InventoryUtil.isBuffPotion(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemFood && InventoryUtil.isGoodFood(stack)) {
            return true;
        } else {
            return InventoryUtil.isGoodItem(stack.getItem());
        }
    }

    private void open() {
        if (!this.clientOpen && !this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.serverOpen = true;
        }
    }

    private void close() {
        if (!this.clientOpen && this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.serverOpen = false;
        }
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.gappleStackSlots.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }
    
    @Override
    public void onEnable() {
        this.ticksSinceLastClick = 0;

        this.clientOpen = mc.currentScreen instanceof GuiInventory;
        this.serverOpen = this.clientOpen;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.close();
        this.clear();
        this.clickRequests.clear();
        super.onDisable();
    }

    public enum Mode {
        WHILE_OPEN("In Inventory"),
        SPOOF("Spoof");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
