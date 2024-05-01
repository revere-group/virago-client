package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/**
 * @author Remi
 * @project Virago
 * @date 5/1/2024
 */
@ModuleData(name = "AutoClicker", displayName = "Auto Clicker", description = "Automatically clicks for you.", type = EnumModuleType.COMBAT)
public class AutoClicker extends AbstractModule {

    private final Setting<Double> minCPS = new Setting<>("Min CPS", 8.0)
            .minimum(1.0)
            .maximum(20.0)
            .incrementation(0.1)
            .describedBy("The minimum amount of clicks per second.");

    private final Setting<Double> maxCPS = new Setting<>("Max CPS", 12.0)
            .minimum(1.0)
            .maximum(20.0)
            .incrementation(0.1)
            .describedBy("The maximum amount of clicks per second.");

    private final Setting<Boolean> jitter = new Setting<>("Jitter", false);
    private final Setting<Double> jitterRandomizationRange = new Setting<>("Range", 0.1)
            .minimum(0.05)
            .maximum(1.0)
            .incrementation(0.05)
            .describedBy("The range for randomization of jitter.")
            .visibleWhen(jitter::getValue);

    private final Setting<Boolean> randomize = new Setting<>("Randomize", false);
    private final Setting<Double> randomizationRange = new Setting<>("Range", 0.5)
            .minimum(0.1)
            .maximum(5.0)
            .incrementation(0.1)
            .describedBy("The range for randomization.")
            .visibleWhen(randomize::getValue);

    private final TimerUtil timer = new TimerUtil();

    /**
     * The pre motion event listener
     */
    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        updateMetaData();
        handleAutoClick();
    };

    /**
     * Updates the metadata
     */
    private void updateMetaData() {
        setMetaData(String.format("C: %.1f J:%s", maxCPS.getValue(), jitter.getValue() ? "On" : "Off"));
    }

    /**
     * Handles the auto clicker
     */
    private void handleAutoClick() {
        if (isLeftMouseButtonDownOnBlock()) {
            pressAttackKey();
        } else if (canAutoClick()) {
            autoClick();
        }
    }

    /**
     * Checks if the left mouse button is down on a block
     *
     * @return if the left mouse button is down on a block
     */
    private boolean isLeftMouseButtonDownOnBlock() {
        return Mouse.isButtonDown(0) && mc.objectMouseOver != null &&
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    /**
     * Presses the attack key
     */
    private void pressAttackKey() {
        mc.gameSettings.keyBindAttack.pressed = true;
    }

    /**
     * Checks if the auto clicker can click
     *
     * @return if the auto clicker can click
     */
    private boolean canAutoClick() {
        return mc.currentScreen == null && !mc.thePlayer.isBlocking();
    }

    /**
     * Performs the auto click
     */
    private void autoClick() {
        Mouse.poll();
        if (Mouse.isButtonDown(0) && timer.hasTimeElapsed(getRandomDelay())) {
            performMouseClick();
            timer.reset();
        }
        if (jitter.getValue() && Mouse.isButtonDown(0)) {
            applyJitter();
        }
    }

    /**
     * Gets a random delay
     *
     * @return a random delay
     */
    private int getRandomDelay() {
        if (randomize.getValue()) {
            double min = Math.max(minCPS.getValue() - randomizationRange.getValue(), 1.0);
            double max = Math.min(maxCPS.getValue() + randomizationRange.getValue(), 20.0);
            return (int) (1000 / getRandomInRange(min, max));
        } else {
            return (int) (1000 / maxCPS.getValue());
        }
    }

    /**
     * Performs a mouse click
     */
    private void performMouseClick() {
        callClick(true);
        callClick(false);
    }

    /**
     * Applies jitter to the players rotation
     */
    private void applyJitter() {
        double jitterRange = jitterRandomizationRange.getValue();
        mc.thePlayer.rotationYaw += (float) getRandomInRange(-jitterRange, jitterRange);
        mc.thePlayer.rotationPitch += (float) getRandomInRange(-jitterRange, jitterRange);
    }

    /**
     * Calls a click
     *
     * @param state the state of the click
     */
    private void callClick(boolean state) {
        final int keyBind = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(keyBind, state);
        if (state) KeyBinding.onTick(keyBind);
    }

    /**
     * Gets a random number in a range
     *
     * @param min the minimum number
     * @param max the maximum number
     * @return a random number in a range
     */
    private double getRandomInRange(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}
