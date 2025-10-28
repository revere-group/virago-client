package dev.revere.virago.api.protection.auth;

import dev.revere.virago.Virago;
import dev.revere.virago.api.network.socket.SocketClient;
import dev.revere.virago.api.protection.ViragoUser;
import dev.revere.virago.api.protection.rank.Rank;
import dev.revere.virago.client.gui.menu.GuiLicenceKey;
import dev.revere.virago.client.gui.menu.GuiSelectDesign;
import dev.revere.virago.util.Logger;
import net.minecraft.client.Minecraft;

public class Safelock {
    private final String productKey;
    private final String server;
    private final String authorization;

    public Safelock(String licenseKey, String validationServer, String authorization) {
        this.productKey = licenseKey;
        this.server = validationServer;
        this.authorization = authorization;
    }

    public boolean nigger() {
        String[] respo = nigger4();
        return Boolean.parseBoolean(respo[3]);
    }

    public String[] nigger4() {
        final boolean valid = true;
        final String neekeri = "OPEN_SOURCE_SUCCESS";
        final String statusCode = "200";
        final String clientName = "OpenSourceUser";
        final String rank = "OWNER";

        GuiLicenceKey.isAuthorized = valid;

        Logger.info(Rank.getRank(rank) + " " + clientName, getClass());
        Virago.getInstance()
                .setViragoUser(new ViragoUser(clientName, "0001", Rank.getRank(rank)));
        SocketClient.init(productKey);
        Minecraft.getMinecraft().displayGuiScreen(new GuiSelectDesign());

        return new String[]{"2", neekeri, statusCode, String.valueOf(valid)};
    }
}