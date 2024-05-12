package dev.revere.virago.client.events.player;

import dev.revere.virago.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Remi
 * @project Virago
 * @date 5/12/2024
 */
@Getter
@Setter
@AllArgsConstructor
public class ModalUpdateEvent extends Event {
    private final EntityPlayer player;
    private final ModelPlayer model;
}
