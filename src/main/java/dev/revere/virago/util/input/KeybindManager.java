package dev.revere.virago.util.input;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.service.ServiceManager;
import dev.revere.virago.client.events.input.KeyDownEvent;
import dev.revere.virago.client.events.input.KeyUpEvent;
import dev.revere.virago.client.events.update.PostMotionEvent;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
public class KeybindManager {

    /**
     * Handles the key up event
     * If the current screen is invalid, no action is taken
     * Otherwise, it disables modules that have a HOLD bind type and match the released key
     *
     * @param event The key up event
     */
    @EventHandler
    private final Listener<KeyUpEvent> keyUpEventListener = event -> {
        if (isInvalidScreen(Minecraft.getMinecraft().currentScreen)) return;
        for (AbstractModule feature : Virago.getInstance().getServiceManager().getService(ModuleService.class).getModules().values().stream()
                .filter(feature ->
                        feature.isBound() &&
                                feature.getBindType() == BindType.HOLD &&
                                feature.getKey() == event.getKey()).collect(Collectors.toList())) {

            feature.setEnabled(false);
        }
    };

    /**
     * Handles the key down event
     * If the current screen is invalid, no action is taken
     * Otherwise, it toggles or enables modules based on their keyboard configuration.
     *
     * @param event The key down event
     */
    @EventHandler
    private final Listener<KeyDownEvent> keyDownEventListener = event -> {
        if (isInvalidScreen(Minecraft.getMinecraft().currentScreen)) return;

        for (AbstractModule feature : Virago.getInstance().getServiceManager().getService(ModuleService.class).getModules().values().stream().
                filter(feature ->
                        feature.isBound() &&
                                feature.getKey() == event.getKey()).collect(Collectors.toList())) {

            feature.setEnabled(feature.getBindType() != BindType.TOGGLE || !feature.isEnabled());
        }
    };

    /**
     * Checks if the provided screen is invalid
     * An invalid screen indicates that the keybind actions should not be processed
     *
     * @param screen The screen to check
     * @return true if the screen is invalid, false otherwise
     */
    public boolean isInvalidScreen(GuiScreen screen) {
        return screen != null;
    }
}