package dev.revere.virago.api.alt;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
@Setter
@Getter
public class Alt {
    String username;
    String password;
    String alias;
    String type;
    String uuid;
    long creationDate;


    public Alt(String alias, String username, String password, String type, String uuid) {
        this(alias, username, password, System.currentTimeMillis(), type, uuid);
    }

    public Alt(String alias, String username, String password, long creationDate, String type, String uuid) {
        this.username = username;
        this.alias = alias;
        this.password = password;
        this.creationDate = creationDate;
        this.type = type;
        this.uuid = uuid;
    }
}