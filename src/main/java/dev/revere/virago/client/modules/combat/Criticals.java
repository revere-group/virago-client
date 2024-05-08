package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.misc.TimerUtil;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "Criticals", displayName = "Criticals", description = "Always Criticals", type = EnumModuleType.COMBAT)
public class Criticals extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.WATCHDOG);


    private final Setting<Float> delay = new Setting<>("Delay", 3.0F)
            .minimum(0F)
            .maximum(1000F)
            .incrementation(50F)
            .describedBy("Delay");


    private final TimerUtil timer = new TimerUtil();

    private boolean attacked;
    private int ticks;
    private int offGroundTicks;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {

        if (!mc.thePlayer.onGround) {
            offGroundTicks++;
        } else {
            offGroundTicks = 0;
        }

        switch (mode.getValue()) {
            case WATCHDOG2: {
                event.setGround(false);
                break;
            }

            case WATCHDOG: {
                if (attacked) {
                    ticks++;

                    switch (ticks) {
                        case 1: {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.motionY = .2f - Math.random() / 1000f;
                                Logger.addChatMessage("move up " + ticks);
                            }
                            break;
                        }

                        case 2: {
                            if (offGroundTicks == 1) {
                                mc.thePlayer.motionY -= 0.1f - Math.random() / 1000f;
                                Logger.addChatMessage("move down " + ticks);
                            }
                            break;
                        }
                    }
                    event.setGround(false);
                } else {
                    attacked = false;
                    ticks = 0;
                }
                break;
            }
        }
    };

    @EventHandler
    public final Listener<AttackEvent> onAttackEvent = event -> {
        if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && timer.getTime() >= delay.getValue()) {
            mc.thePlayer.onCriticalHit(event.getTarget());
            Logger.addChatMessage("Critical Hit! " + ticks);

            timer.reset();
            attacked = true;
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

    enum Mode {
        EDIT,
        WATCHDOG,
        WATCHDOG2
    }
}
