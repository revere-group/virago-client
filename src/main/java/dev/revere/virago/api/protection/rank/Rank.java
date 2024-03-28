package dev.revere.virago.api.protection.rank;

import lombok.Getter;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */
@Getter
public enum Rank {

    USER("User"),
    TESTER("Tester"),
    STAFF("Staff"),
    DEVELOPER("Developer")

    ;

    private final String rankName;

    Rank(String rankName) {
        this.rankName = rankName;
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
