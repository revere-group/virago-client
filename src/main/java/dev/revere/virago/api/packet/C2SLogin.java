package dev.revere.virago.api.packet;

import lombok.var;

import java.util.Objects;

public final class C2SLogin {
    private final String licenseKey;

    public C2SLogin(
            String licenseKey
    ) {
        this.licenseKey = licenseKey;
    }

    public String licenseKey() {
        return licenseKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SLogin) obj;
        return Objects.equals(this.licenseKey, that.licenseKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseKey);
    }

    @Override
    public String toString() {
        return "C2SLogin[" +
                "licenseKey=" + licenseKey + ']';
    }
}
