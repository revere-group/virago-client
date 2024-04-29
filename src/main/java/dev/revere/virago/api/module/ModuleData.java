package dev.revere.virago.api.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleData {

    String name();

    String displayName();

    String description();

    EnumModuleType type();

    boolean isHidden() default false;

}
