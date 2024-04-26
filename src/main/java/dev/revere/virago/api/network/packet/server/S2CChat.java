package dev.revere.virago.api.network.packet.server;

import lombok.var;

import java.util.Objects;

public final class S2CChat {
    private final String code;
    private final String author;
    private final String rank;
    private final String content;
    private final Long timestamp;

    public S2CChat(
            String code,
            String author,
            String rank,
            String content,
            Long timestamp
    ) {
        this.code = code;
        this.author = author;
        this.rank = rank;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String code() {
        return code;
    }

    public String author() {
        return author;
    }

    public String rank() {
        return rank;
    }

    public String content() {
        return content;
    }

    public Long timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChat) obj;
        return Objects.equals(this.code, that.code) &&
                Objects.equals(this.author, that.author) &&
                Objects.equals(this.rank, that.rank) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, author, rank, content, timestamp);
    }

    @Override
    public String toString() {
        return "S2CChat[" +
                "code=" + code + ", " +
                "author=" + author + ", " +
                "rank=" + rank + ", " +
                "content=" + content + ", " +
                "timestamp=" + timestamp + ']';
    }
}
