package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import org.reflections.Reflections;

import java.util.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Getter
public class ModuleService implements IService {
    private final LinkedHashMap<Class<?>, AbstractModule> modules = new LinkedHashMap<>();

    /**
     * Gets the module by class.
     *
     * @param clazz the class
     * @return the module
     */
    public <T extends AbstractModule> T getModule(Class<T> clazz) {
        return (T) modules.get(clazz);
    }

    /**
     * Gets the module by name.
     *
     * @param name the name
     * @return the module
     */
    public AbstractModule getModuleByName(String name) {
        return modules.values().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Gets the modules by type.
     *
     * @param type the type
     * @return the modules by type
     */
    public List<AbstractModule> getModulesByType(EnumModuleType type) {
        List<AbstractModule> mods = new ArrayList<>();
        for (AbstractModule module : modules.values()) {
            if (module.getType() == type) {
                mods.add(module);
            }
        }
        return mods;
    }

    /**
     * Returns a list of all modules.
     *
     * @return
     */
    public List<AbstractModule> getModuleList() {
        List<AbstractModule> mods = new ArrayList<>(modules.values());
        if (modules.isEmpty()) {
            Logger.err("No modules found!", getClass());
        }
        return mods;
    }

    @Override
    public void initService() {
        Virago.getInstance().getEventBus().register(this);
        Logger.info("Module service initialized!", getClass());
    }

    @Override
    public void startService() {
        Reflections reflections = new Reflections("dev.revere.virago.client.modules");
        Set<Class<? extends AbstractModule>> classes = reflections.getSubTypesOf(AbstractModule.class);

        for (Class<?> clazz : classes) {
            try {
                AbstractModule module = (AbstractModule) clazz.newInstance();
                modules.put(module.getClass(), module);
            } catch (InstantiationException | IllegalAccessException e) {
                Logger.err("Failed to instantiate module " + clazz.getSimpleName(), getClass());
            }
        }

        Logger.info("Loaded " + modules.size() + " modules!", getClass());

        getModuleList().forEach(module -> {
            Arrays.stream(module.getClass().getDeclaredFields()).filter(field -> Setting.class.isAssignableFrom(field.getType())).forEach(field -> {
                field.setAccessible(true);

                try {
                    Setting<?> setting = ((Setting<?>) field.get(module));

                    if (setting.getParent() == null) {
                        module.getSettings().add(setting);
                    }

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    Logger.err("Failed to add setting " + field.getName() + " to module " + module.getName(), getClass());
                }
            });
        });
    }
}