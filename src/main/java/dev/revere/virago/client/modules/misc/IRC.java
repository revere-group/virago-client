package dev.revere.virago.client.modules.misc;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;

/**
 * @author Remi
 * @project Virago
 * @date 5/4/2024
 */
@ModuleData(name = "IRC", displayName = "IRC", description = "Chat with other players", type = EnumModuleType.MISC)
public class IRC extends AbstractModule {

    public IRC() {
        setEnabledSilent(true);
    }
}
