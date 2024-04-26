package dev.revere.virago.api.network.packet.client;

import lombok.var;

import java.util.Objects;

public final class C2SChat {
    private final String content;
    private final String jwt;

    public C2SChat(
            String content,
            String jwt
    ) {
        this.content = content;
        this.jwt = jwt;
    }

    public String content() {
        return content;
    }

    public String jwt() {
        return jwt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SChat) obj;
        return Objects.equals(this.content, that.content) &&
                Objects.equals(this.jwt, that.jwt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, jwt);
    }

    @Override
    public String toString() {
        return "C2SChat[" +
                "content=" + content + ", " +
                "jwt=" + jwt + ']';
    }

}
