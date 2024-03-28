package dev.revere.virago.api.command;

import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import lombok.Getter;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public abstract class AbstractCommand {
    private final String[] aliases = getClass().getAnnotation(CommandData.class).aliases();
    private final String description = getClass().getAnnotation(CommandData.class).description();
    private final String syntax = getClass().getAnnotation(CommandData.class).syntax();

    public abstract void executeCommand(String line, String[] args);
}
