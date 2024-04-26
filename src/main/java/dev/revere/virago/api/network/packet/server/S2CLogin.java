package dev.revere.virago.api.network.packet.server;

import lombok.var;

import java.util.Objects;

public final class S2CLogin {
    private final String code;
    private final String jwtToken;

    public S2CLogin(
            String code,
            String jwtToken
    ) {
        this.code = code;
        this.jwtToken = jwtToken;
    }

    public String code() {
        return code;
    }

    public String jwtToken() {
        return jwtToken;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CLogin) obj;
        return Objects.equals(this.code, that.code) &&
                Objects.equals(this.jwtToken, that.jwtToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, jwtToken);
    }

    @Override
    public String toString() {
        return "S2CLogin[" +
                "code=" + code + ", " +
                "jwtToken=" + jwtToken + ']';
    }
}
