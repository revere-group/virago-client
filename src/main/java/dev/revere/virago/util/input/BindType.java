package dev.revere.virago.util.input;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
public enum BindType {
    HOLD("Hold"), // The input action is held down while active
    TOGGLE("Toggle") // The input action is toggled on/off with each activation

    ;

    // The string representation of the bind type.
    private final String type;

    /**
     * BindType constructor.
     *
     * @param type The string representation of the bind type.
     */
    BindType(String type) {
        this.type = type;
    }

    /**
     * Returns a string representation of the bind type.
     *
     * @return The string representation of the bind type.
     */
    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Retrieves the corresponding BindType enum for the given type string.
     * If the provided type does not match any known bind type, the default
     * bind type TOGGLE is returned.
     *
     * @param type The type string representing the bind type.
     * @return The BindType enum corresponding to the given type string.
     */
    public static BindType getBind(String type) {
        for(BindType bind : values()) {
            if(bind.type.equalsIgnoreCase(type)) {
                return bind;
            }
        }

        return TOGGLE;
    }
}