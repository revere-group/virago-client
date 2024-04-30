package dev.revere.virago.api.protection.rank;

import lombok.Getter;
import net.minecraft.util.EnumChatFormatting;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */
@Getter
public enum Rank {

    USER("User", EnumChatFormatting.WHITE),
    TESTER("Tester", EnumChatFormatting.YELLOW),
    MEDIA("Media", EnumChatFormatting.GOLD),
    STAFF("Staff", EnumChatFormatting.LIGHT_PURPLE),
    DEVELOPER("Developer", EnumChatFormatting.AQUA),
    CONSOLE("#", EnumChatFormatting.DARK_RED)

    ;

    private final String rankName;
    private final EnumChatFormatting color;

    Rank(String rankName, EnumChatFormatting color) {
        this.rankName = rankName;
        this.color = color;
    }

    /**
     * Is staff boolean.
     *
     * @return the boolean
     */
    public boolean isStaff() {
        return this == STAFF;
    }

    /**
     * Is developer boolean.
     *
     * @return the boolean
     */
    public boolean isDeveloper() {
        return this == DEVELOPER;
    }

    /**
     * Is tester boolean.
     *
     * @return the boolean
     */
    public boolean isTester() {
        return this == TESTER;
    }

    /**
     * Is media boolean.
     *
     * @return the boolean
     */
    public boolean isMedia() {
        return this == MEDIA;
    }

    /**
     * Is user boolean.
     *
     * @return the boolean
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * Is higher than boolean.
     *
     * @param rank the rank
     * @return the boolean
     */
    public boolean isHigherThan(Rank rank) {
        return this.ordinal() > rank.ordinal();
    }

    /**
     * Is lower than boolean.
     *
     * @param rank the rank
     * @return the boolean
     */
    public boolean isLowerThan(Rank rank) {
        return this.ordinal() < rank.ordinal();
    }

    /**
     * Is higher or equal boolean.
     *
     * @param rank the rank
     * @return the boolean
     */
    public boolean isHigherOrEqual(Rank rank) {
        return this.ordinal() >= rank.ordinal();
    }

    /**
     * Is lower or equal boolean.
     *
     * @param rank the rank
     * @return the boolean
     */
    public boolean isLowerOrEqual(Rank rank) {
        return this.ordinal() <= rank.ordinal();
    }

    /**
     * Gets a rank by name.
     *
     * @param name the name
     * @return the rank
     */
    public static Rank getRank(String name) {
        for (Rank rank : values()) {
            if (rank.getRankName().equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return null;
    }
}
