package dev.revere.virago.api.module;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.modules.render.Notifications;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.input.BindType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Getter
@Setter
public abstract class AbstractModule {

    // The Minecraft instance.
    public static final Minecraft mc = Minecraft.getMinecraft();

    // The name, description, and type of the module.
    private final String name = getClass().getAnnotation(ModuleData.class).name();
    private final String displayName = getClass().getAnnotation(ModuleData.class).displayName();
    private final String description = getClass().getAnnotation(ModuleData.class).description();
    private final EnumModuleType type = getClass().getAnnotation(ModuleData.class).type();
    private boolean isHidden = getClass().getAnnotation(ModuleData.class).isHidden();

    // The settings for all the modules.
    private final List<Setting<?>> settings = new ArrayList<>();

    // The state of the module (enabled or disabled).
    private boolean enabled;

    // The key binding for the module.
    private int key = Keyboard.CHAR_NONE;

    // The type of key binding (e.g., toggle, hold).
    private BindType bindType = BindType.TOGGLE;

    private String metaData = "";

    private final Animation animation = new Animation(() -> Float.valueOf(250.0f), false, () -> Easing.CUBIC_IN_OUT);

    /**
     * AbstractModule constructor to initialize the module.
     */
    public AbstractModule() {
        init();
    }

    /**
     * Toggles the module
     */
    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            Notifications notifications = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Notifications.class);

            if (notifications.isEnabled() && notifications.moduleNotifications.getValue()) {
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.YES, "Enabled Module", this.getDisplayName());
            }
            onEnable();
        } else {
            Notifications notifications = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Notifications.class);
            if (notifications.isEnabled() && notifications.moduleNotifications.getValue()) {
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.NO, "Disabled Module", this.getDisplayName());
            }
            onDisable();
        }
    }

/**
     * Toggles the module without notifications
     */
    public void toggleSilent() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Set whether the module is toggled on or off.
     *
     * @param toggled true to toggle the module on, false to toggle it off.
     */
    public void setEnabled(boolean toggled) {
        enabled = toggled;

        if (toggled) {
            Notifications notifications = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Notifications.class);

            if (notifications.isEnabled() && notifications.moduleNotifications.getValue()) {
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.YES, "Enabled Module", this.getDisplayName());
            }
            onEnable();
        } else {
            Notifications notifications = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Notifications.class);
            if (notifications.isEnabled() && notifications.moduleNotifications.getValue()) {
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.NO, "Disabled Module", this.getDisplayName());
            }
            onDisable();
        }
    }

    /**
     * Set whether the module is toggled on or off.
     *
     * @param toggled true to toggle the module on, false to toggle it off.
     */
    public void setEnabledSilent(boolean toggled) {
        enabled = toggled;

        if (toggled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Retrieves the annotation value of a field.
     *
     * @param field           The field to retrieve the annotation from.
     * @param annotationClass The annotation class.
     * @param <T>             The type of the annotation.
     * @return The annotation value or null if the annotation is not present.
     */
    private <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        if (field.isAnnotationPresent(annotationClass)) {
            return field.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * Retrieves the setting hierarchy.
     *
     * @return The setting hierarchy.
     */
    public List<Setting<?>> getSettingHierarchy() {
        List<Setting<?>> hierarchy = new ArrayList<>();

        for (Setting<?> setting : settings) {
            hierarchy.add(setting);
            hierarchy.addAll(setting.getHierarchy());
        }

        return hierarchy;
    }

    /**
     * Called when the module is initialized.
     * Override this method to perform actions when the module is initialized.
     */
    public void init() {}

    /**
     * Called when the module is enabled.
     * Override this method to perform actions when the module is enabled.
     */
    public void onEnable() {
        Virago.getInstance().getEventBus().register(this);
    }

    /**
     * Called when the module is disabled.
     * Override this method to perform actions when the module is disabled.
     */
    public void onDisable() {
        Virago.getInstance().getEventBus().unregister(this);
    }

    /**
     * Retrieves the display name of the module.
     *
     * @return The display name of the module.
     */
    public String getHUDDisplayName() {
        return getName() + (!getMetaData().isEmpty() ? (" " + EnumChatFormatting.GRAY + getMetaData()) : "");
    }

    /**
     * Checks if the module is bound to a key.
     *
     * @return true if the module is bound, false otherwise.
     */
    public boolean isBound() {
        return key != Keyboard.CHAR_NONE;
    }

}
