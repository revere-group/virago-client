package dev.revere.virago.client.gui.components;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public enum InteractionComponent {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2),
    SIDE_ONE(3),
    SIDE_TWO(4);

    private final int button;

    /**
     * Interaction component
     *
     * @param button the button
     */
    InteractionComponent(int button) {
        this.button = button;
    }

    /**
     * Get interaction
     *
     * @param in the in
     * @return the interaction
     */
    public static InteractionComponent getInteraction(int buttonID) {
        return Arrays.stream(InteractionComponent.values()).filter(interaction -> interaction.getButton() == buttonID).findFirst().orElse(null);
    }
}
