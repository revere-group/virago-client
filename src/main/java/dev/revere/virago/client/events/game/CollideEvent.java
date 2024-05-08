package dev.revere.virago.client.events.game;

import dev.revere.virago.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Remi
 * @project Virago
 * @date 5/8/2024
 */
@Getter
@Setter
@AllArgsConstructor
public class CollideEvent extends Event {
    private Entity entity;
    private double posX;
    private double posY;
    private double posZ;
    private AxisAlignedBB boundingBox;
    private Block block;
}
