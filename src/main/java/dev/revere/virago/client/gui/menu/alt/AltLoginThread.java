package dev.revere.virago.client.gui.menu.alt;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import dev.revere.virago.Virago;
import dev.revere.virago.client.services.AltService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import java.net.Proxy;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
public final class AltLoginThread extends Thread {
    private final String password;
    private final String username;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final AltService altmgr = Virago.getInstance().getServiceManager().getService(AltService.class);

    public AltLoginThread(String username, String password) {
        super("Alt Login Thread");
        this.username = username;
        this.password = password;
    }

    private Session createSession(String username, String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);

        auth.setUsername(username);
        auth.setPassword(password);

        try {
            auth.logIn();
            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
        } catch (AuthenticationException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        if (password.equals("")) {
            if (altmgr.isValidCrackedAlt(username)) {
                mc.session = new Session(username, "", "", "mojang");
                altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + username + ".");
            } else {
                altmgr.setStatus(EnumChatFormatting.RED + "Invalid Username!");
            }
            return;
        }

        altmgr.setStatus(EnumChatFormatting.YELLOW + "Logging in...");
        Session auth = createSession(username, password);

        if (auth == null)
            altmgr.setStatus(EnumChatFormatting.RED + "Login failed!");
        else {
            altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + auth.getUsername() + ".");
            mc.session = auth;
        }
    }
}
