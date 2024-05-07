package dev.revere.virago.client.services;

import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.gui.menu.EnumSelectDesign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignService implements IService {
    private EnumSelectDesign selectedDesign;
}
