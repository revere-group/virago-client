package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.anticheat.AbstractCheck;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.events.game.TickEvent;
import dev.revere.virago.client.modules.misc.AntiCheat;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
public class CheckService implements IService {

    private final HashMap<Class<? extends AbstractCheck>, AbstractCheck> checksHashMap = new HashMap<>();

    @Override
    public void initService() {
        Virago.getInstance().getEventBus().register(this);
    }

    @Override
    public void startService() {
        Reflections reflections = new Reflections("dev.revere.virago.client.checks");
        Set<Class<? extends AbstractCheck>> classes = reflections.getSubTypesOf(AbstractCheck.class);

        for (Class<? extends AbstractCheck> clazz : classes) {
            try {
                AbstractCheck check = clazz.newInstance();
                checksHashMap.put(clazz, check);
            } catch (InstantiationException | IllegalAccessException e) {
                Logger.err("Failed to instantiate check: " + clazz.getSimpleName(), getClass());
            }
        }
    }

    /**
     * Tick event listener.
     */
    @EventHandler
    private final Listener<TickEvent> tickEventListener = event -> {
        AntiCheat antiCheat = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(AntiCheat.class);
        if (!antiCheat.isEnabled()) return;
        for (Entity entity : Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityPlayer) {
                for (AbstractCheck check : checksHashMap.values()) {
                    if (check.runCheck((EntityPlayer) entity)) {
                        Logger.addChatMessage(EnumChatFormatting.DARK_AQUA + entity.getName() + EnumChatFormatting.WHITE + " failed check: " + EnumChatFormatting.DARK_AQUA + check.getCheckName());
                    }
                }
            }
        }
    };

    /**
     * Gets checks.
     *
     * @return the checks
     */
    public List<AbstractCheck> getChecks() {
        final List<AbstractCheck> checks = new ArrayList<>(checksHashMap.values());
        if (checks.isEmpty()) {
            Logger.err("No checks found!", getClass());
        }
        return checks;
    }

    /**
     * Gets the check by name.
     *
     * @param checkName the check name
     * @return the check
     */
    public AbstractCheck getCheck(String checkName) {
        AbstractCheck requestedCheck = checksHashMap.values()
                .stream()
                .filter(check -> check.getCheckName().equalsIgnoreCase(checkName))
                .findFirst()
                .orElse(null);

        if (requestedCheck == null) {
            Logger.err("Check not found: " + checkName, getClass());
        }

        return requestedCheck;
    }
}
