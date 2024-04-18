package dev.revere.virago.api.packet;

import lombok.var;

import java.util.Objects;

public final class S2CChatReply {
    private final String message;
    private final boolean success;

    public S2CChatReply(
            String message,
            boolean success
    ) {
        this.message = message;
        this.success = success;
    }

    public String message() {
        return message;
    }

    public boolean success() {
        return success;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChatReply) obj;
        return Objects.equals(this.message, that.message) &&
                this.success == that.success;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, success);
    }

    @Override
    public String toString() {
        return "S2CChatReply[" +
                "message=" + message + ", " +
                "success=" + success + ']';
    }

}
