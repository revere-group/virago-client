package dev.revere.virago.api.protection;

import dev.revere.virago.api.protection.rank.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */
@Setter
@Getter
@AllArgsConstructor
public class ViragoUser {
    private final String username;
    private final String uid;
    private final Rank rank;
}
