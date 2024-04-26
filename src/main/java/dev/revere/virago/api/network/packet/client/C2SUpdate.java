package dev.revere.virago.api.network.packet.client;

import lombok.var;

import java.util.Objects;

public final class C2SUpdate {
    private final String licenseKey;

    public C2SUpdate(
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
        var that = (C2SUpdate) obj;
        return Objects.equals(this.licenseKey, that.licenseKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseKey);
    }

    @Override
    public String toString() {
        return "C2SUpdateC[" +
                "licenseKey=" + licenseKey + ']';
    }

}
