package dev.revere.virago.client.gui.menu.alt;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.revere.virago.Virago;
import dev.revere.virago.api.alt.Alt;
import dev.revere.virago.client.gui.menu.alt.components.CustomRectButton;
import dev.revere.virago.client.gui.menu.components.CustomGuiTextField;
import dev.revere.virago.client.services.AltService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.alt.CookieLogin;
import dev.revere.virago.util.alt.SkinUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
public class GuiAltManager extends GuiScreen {

    private final ResourceLocation LOGO_TEXTURE = new ResourceLocation("virago/textures/logo/logo.png");
    private final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("virago/textures/bg.png");

    private final GuiScreen previousScreen;
    private final AltService altmgr = Virago.getInstance().getServiceManager().getService(AltService.class);
    private AltLoginThread thread;
    private CustomGuiTextField combo;
    private AltList list;
    private Alt selectedAlt = null;
    private GuiButton loginButton;
    private GuiButton deleteButton;

    private CustomGuiTextField usernameField;
    private GuiButton changeUsernameButton;

    public GuiAltManager(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        altmgr.load();

        int CENTER_X = width / 2;
        int CENTER_Y = height / 2;

        list = new AltList(mc, CENTER_X - 50, 290, 42, width, height + 35, 90, height - 35);

        buttonList.clear();
        buttonList.add(new GuiButton(7, 4, 4, 60, 22, "Back"));

        loginButton = new GuiButton(5, width - 195, height - 50 - 60, 150, 18, "Login");
        deleteButton = new GuiButton(6, width - 195, height - 26 - 60, 150, 18, "Delete");

        usernameField = new CustomGuiTextField(4, CENTER_X - 255, 362 + 30 + 10, 170, 18, "New Username");
        usernameField.setMaxStringLength(16);

        changeUsernameButton = new GuiButton(8, CENTER_X - 255, 362 + 50 + 10, 170, 20, "Change Username");
        buttonList.add(changeUsernameButton);

        int BUTTON_WIDTH = 170;
        int BUTTON_HEIGHT = 40;

        Color backgroundColor = new Color(30, 30, 30);
        Color outlineColor = new Color(30, 30, 30);
        Color textColor = Color.WHITE;

        buttonList.add(new CustomRectButton(0, CENTER_X - 255, CENTER_Y - 150, BUTTON_WIDTH, BUTTON_HEIGHT, "Microsoft Login", backgroundColor, outlineColor, textColor));
        buttonList.add(new CustomRectButton(1, CENTER_X - 255, CENTER_Y - 100, BUTTON_WIDTH, BUTTON_HEIGHT, "Add Account", backgroundColor, outlineColor, textColor));
        buttonList.add(new CustomRectButton(2, CENTER_X - 255, CENTER_Y - 50, BUTTON_WIDTH, BUTTON_HEIGHT, "Add Account from Clipboard", backgroundColor, outlineColor, textColor));
        buttonList.add(new CustomRectButton(3, CENTER_X - 255, CENTER_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "Generate Cracked Account", backgroundColor, outlineColor, textColor));
        buttonList.add(new CustomRectButton(4, CENTER_X - 255, CENTER_Y + 50, BUTTON_WIDTH, BUTTON_HEIGHT, "Add Cookie Alt", backgroundColor, outlineColor, textColor));

        combo = new CustomGuiTextField(3, CENTER_X - 255, 362 + 10 + 10, 170, 18, "Username:Password");
        combo.setMaxStringLength(256);

        list.setOverlayBackground(false);
        Keyboard.enableRepeatEvents(true);

        altmgr.setStatus("§7Idle...");
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        FontService fonts = Virago.getInstance().getServiceManager().getService(FontService.class);
        String statusString = altmgr.getStatus();

        int CENTER_X = width / 2;

        RenderUtils.drawImage(BACKGROUND_TEXTURE, 0, 0, width, height);

        RoundedUtils.round(CENTER_X - 270, 90, 200, height - 120, 5, new Color(20, 20, 20));
        RoundedUtils.outline(CENTER_X - 270, 90, 200, height - 120, 5, 1, new Color(0, 0, 0, 255));

        RoundedUtils.round(CENTER_X - 55, 90, 300, height - 120, 5, new Color(20, 20, 20));
        RoundedUtils.outline(CENTER_X - 55, 90, 300, height - 120, 5, 1, new Color(0, 0, 0, 255));

        list.drawScreen(mouseX, mouseY, partialTicks);

        fonts.getRalewayExtraBold().drawCenteredString("ALT MANAGER", width / 2f, 8, -1);

        fonts.getProductSans().drawStringWithShadow(statusString, width - fonts.getProductSans().getStringWidth(statusString) - 8, 8, -1);
        fonts.getRalewayExtraBold().drawCenteredString("CURRENT USER", 120, 8, -1);
        fonts.getProductSans().drawCenteredString(mc.getSession().getUsername(), 120, 16, -1);

        if (selectedAlt != null) {
            Date date = new Date(selectedAlt.getCreationDate());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            String password = selectedAlt.getPassword();
            String accountType = selectedAlt.getType();
            String maskedPassword;

            if ("cracked".equals(accountType)) {
                maskedPassword = "Cracked account";
            } else if ("cookie".equals(accountType)) {
                maskedPassword = "Cookie account";
            } else {
                maskedPassword = !password.isEmpty() ?
                        password.substring(0, Math.min(password.length(), 3)) +
                                password.substring(3, Math.min(password.length(), 20)).replaceAll(".", "*") :
                        "Cracked account";
            }

            RoundedUtils.round(width - 220, 90, 200, height - 120, 5, new Color(20, 20, 20));
            RoundedUtils.outline(width - 220, 90, 200, height - 120, 5, 1, new Color(0, 0, 0, 255));

            fonts.getRalewayExtraBold().drawCenteredString("ALT INFO", width - 120, 110, -1);
            fonts.getProductSans().drawCenteredString(selectedAlt.getUsername(), width - 120, 134, -1);
            fonts.getProductSans().drawCenteredString(maskedPassword, width - 120, 154, 0xFFAAAAAA);
            fonts.getProductSans().drawCenteredString("Created the " + dateFormat.format(date) + " at " + timeFormat.format(date), width - 120, 170, -1);

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            RenderUtils.drawImage(new ResourceLocation("virago/textures/menu/alexbody.png"), width - 220, height / 2 - 74, 200, 200);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();

            loginButton.drawButton(mc, mouseX, mouseY);
            deleteButton.drawButton(mc, mouseX, mouseY);
        }

        combo.drawTextBox();
        usernameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(selectedAlt != null) {
            if(loginButton.mousePressed(mc, mouseX, mouseY)) {
                if (Objects.equals(selectedAlt.getType(), "cookie")) {
                    mc.session = new Session(selectedAlt.getUsername(), selectedAlt.getUuid(), selectedAlt.getPassword(), "legacy");
                    altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + selectedAlt.getUsername() + "." + " §7(If token is invalid, logged in as cracked account.)");
                    return;
                }
                if (Objects.equals(selectedAlt.getType(), "cracked")) {
                    if (altmgr.isValidCrackedAlt(selectedAlt.getUsername())) {
                        mc.session = new Session(selectedAlt.getUsername(), "", "", "mojang");
                        altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + selectedAlt.getUsername() + ".");
                    } else {
                        altmgr.setStatus(EnumChatFormatting.RED + "Invalid Username!");
                    }
                    return;
                }
                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                try {
                    MicrosoftAuthResult result = authenticator.loginWithCredentials(selectedAlt.getUsername(), selectedAlt.getPassword());
                    MinecraftProfile profile = result.getProfile();
                    mc.session = new Session(profile.getName(), profile.getId(), result.getAccessToken(), "microsoft");
                    altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + profile.getName() + ".");
                } catch (MicrosoftAuthenticationException e) {
                    altmgr.setStatus(EnumChatFormatting.RED + "Failed to login.");
                }
            }

            if(deleteButton.mousePressed(mc, mouseX, mouseY)) {
                altmgr.getAlts().remove(selectedAlt);
                altmgr.save();
                selectedAlt = null;
            }
        }

        combo.mouseClicked(mouseX, mouseY, mouseButton);
        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (typedChar == '\r') {
            actionPerformed(buttonList.get(0));
        }

        combo.textboxKeyTyped(typedChar, keyCode);
        usernameField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        combo.updateCursorCounter();
        usernameField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        int targetStringLength = 16 - 7 - 4;
        int leftLimit = 97;
        int rightLimit = 123;
        Random random = new Random();
        String generatedString = "Virago" + "_" + random.ints(leftLimit, rightLimit).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString() + Minecraft.getSystemTime() % 1000L;

        switch (button.id) {
            case 1:
                String[] comboCredentials = combo.getText().split(":", 2);

                if (combo.getText().contains(":") && comboCredentials.length > 1) {

                    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                    try {
                        MicrosoftAuthResult result = authenticator.loginWithCredentials(comboCredentials[0], comboCredentials[1]);
                        MinecraftProfile profile = result.getProfile();

                        altmgr.addAlt(new Alt(profile.getName(), comboCredentials[0], comboCredentials[1], "microsoft", profile.getId()));
                        altmgr.setStatus(EnumChatFormatting.GREEN + "Added account \"" + comboCredentials[0] + ":" + comboCredentials[1] + "\".");
                        combo.setText("");
                    } catch (MicrosoftAuthenticationException e) {
                        altmgr.setStatus(EnumChatFormatting.RED + "Failed to add account. " + e.getMessage());
                    }
                } else if (altmgr.isValidCrackedAlt(comboCredentials[0])) {
                    altmgr.addAlt(new Alt(comboCredentials[0], comboCredentials[0], "", "cracked", "cracked"));
                    altmgr.setStatus(EnumChatFormatting.GREEN + "Added account \"" + comboCredentials[0] + "\".");
                    combo.setText("");
                } else {
                    altmgr.setStatus(EnumChatFormatting.RED + "Failed to add account.");
                }

                altmgr.save();
                break;
            case 0: {
                try {
                    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                    MicrosoftAuthResult result = authenticator.loginWithWebview();

                    if (result == null) {
                        altmgr.setStatus(EnumChatFormatting.RED + "Failed to login.");
                        break;
                    }

                    MinecraftProfile profile = result.getProfile();
                    Logger.info("Logged in as " + profile.getName() + " with UUID " + profile.getId() + " and access token " + result.getAccessToken() + ".", getClass());
                    mc.session = new Session(profile.getName(), profile.getId(), result.getAccessToken(), "microsoft");
                    altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + profile.getName() + ".");
                    altmgr.addAlt(new Alt(profile.getName(), profile.getName(), result.getAccessToken(), "cookie", profile.getId()));
                    combo.setText("");
                    altmgr.save();
                } catch (Exception e) {
                    Logger.err("Failed to login with Microsoft account: " + e.getMessage(), getClass());
                }
                break;
            }
            case 2:
                if (GuiScreen.getClipboardString().contains(":")) {
                    String[] combo = GuiScreen.getClipboardString().replace("\n", "").replace(" ", "").split(":", 2);
                    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                    try {
                        MicrosoftAuthResult result = authenticator.loginWithCredentials(combo[0], combo[1]);
                        MinecraftProfile profile = result.getProfile();
                        mc.session = new Session(profile.getName(), profile.getId(), result.getAccessToken(), "microsoft");
                        altmgr.addAlt(new Alt(profile.getName(), combo[0], combo[1], "microsoft", profile.getId()));
                        altmgr.setStatus(EnumChatFormatting.GREEN + "Logged in as " + profile.getName() + ".");
                    } catch (MicrosoftAuthenticationException e) {
                        altmgr.setStatus(EnumChatFormatting.RED + "Failed to login.");
                    }
                } else {
                    altmgr.setStatus(EnumChatFormatting.RED + "Invalid clipboard.");
                }
                altmgr.save();
                break;
            case 3:
                altmgr.addAlt(new Alt(generatedString, generatedString, "", "cracked", "cracked"));
                thread = new AltLoginThread(generatedString, "");
                thread.start();
                altmgr.save();
                break;
            case 4:
                boolean fullscreen;
                if (mc.gameSettings.fullScreen) {
                    mc.toggleFullscreen();
                    fullscreen = true;
                } else {
                    fullscreen = false;
                }
                new Thread(() -> {
                    JOptionPane.showMessageDialog(null, "Please select the cookie file you want to login with.");
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
                    chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        if (fullscreen) {
                            mc.toggleFullscreen();
                        }
                        try {
                            CookieLogin.LoginData loginData = CookieLogin.loginWithCookie(chooser.getSelectedFile());

                            if (loginData == null) {
                                altmgr.status = EnumChatFormatting.RED + "Failed to login with cookie!";
                                return;
                            }

                            altmgr.status = EnumChatFormatting.GREEN + "Logged in as " + loginData.username + ".";
                            mc.session = new Session(loginData.username, loginData.uuid, loginData.mcToken, "legacy");
                            altmgr.addAlt(new Alt(loginData.username, loginData.username, loginData.mcToken, "cookie", loginData.uuid));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    altmgr.save();
                }).start();
                break;
            case 8:
                String newUsername = usernameField.getText();
                if (!newUsername.isEmpty() && mc.getSession().getToken() != null) {
                    try {
                        HttpURLConnection connection = getHttpURLConnection(newUsername);

                        int responseCode = connection.getResponseCode();
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder responseBuilder = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            responseBuilder.append(inputLine);
                        }
                        in.close();

                        if (responseCode == 200) {
                            altmgr.setStatus(EnumChatFormatting.GREEN + "Successfully changed username to " + newUsername + ".");
                            mc.session = new Session(newUsername, mc.getSession().getPlayerID(), mc.getSession().getToken(), "legacy");
                        } else {
                            altmgr.setStatus(EnumChatFormatting.RED + "Failed to change username: " + responseCode);
                        }
                    } catch (IOException e) {
                        if (e.getMessage().contains("401")) {
                            altmgr.setStatus(EnumChatFormatting.RED + "Failed to change username: Unauthorized.");
                        } else if (e.getMessage().contains("403")) {
                            altmgr.setStatus(EnumChatFormatting.RED + "Failed to change username: On cooldown.");
                        } else {
                            altmgr.setStatus(EnumChatFormatting.RED + "Failed to change username: " + e.getMessage());
                        }
                    }
                } else {
                    altmgr.setStatus(EnumChatFormatting.RED + "Please enter a valid username.");
                }
                break;
            case 7:
                mc.displayGuiScreen(previousScreen);
                break;
        }
    }

    private HttpURLConnection getHttpURLConnection(String newUsername) throws IOException {
        URL url = new URL("https://api.minecraftservices.com/minecraft/profile/name/" + newUsername);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Authorization", "Bearer " + mc.getSession().getToken());
        connection.setDoOutput(true);
        return connection;
    }

    class AltList extends Slot {
        public AltList(Minecraft mcIn, int x, int slotWidth, int slotHeight, int width, int height, int top, int bottom) {
            super(mcIn, x, slotWidth, slotHeight, width, height, top, bottom);
        }

        @Override
        public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
            ScaledResolution resolution = new ScaledResolution(mc);
            int topScissor = top + 3;
            int bottomScissor = height - 66;
            GL11.glScissor(x * resolution.getScaleFactor(), (resolution.getScaledHeight() - bottomScissor) * resolution.getScaleFactor(), width * resolution.getScaleFactor(), (bottomScissor - topScissor) * resolution.getScaleFactor());
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            super.drawScreen(mouseXIn, mouseYIn, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }


        @Override
        protected int getSize() {
            return altmgr.getAlts().size();
        }

        //logging in the alt when double left clikced
        @Override
        protected void elementClicked(int slotHeight, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedAlt = altmgr.getAlts().get(slotHeight);
        }

        //removing the alt when double right clicked
        @Override
        protected void elementRightClicked(int slotHeight, boolean isDoubleClick, int mouseX, int mouseY) {

        }

        //don't delete this
        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
        }

        //don't delete this
        @Override
        protected void drawBackground() {
        }

        //drawing the alt slot
        @Override
        protected void drawSlot(int entryID, int p_180791_2_, int y, int slotHeight, int mouseXIn, int mouseYIn) {
            Alt alt = Virago.getInstance().getServiceManager().getService(AltService.class).getAlts().get(entryID);
            FontService fonts = Virago.getInstance().getServiceManager().getService(FontService.class);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, 0, 0);

            RoundedUtils.round(0, y, slotWidth, 38, 2, new Color(30, 30, 30));

            String password = alt.getPassword();
            String maskedPassword = !password.equals("") ?
                    password.substring(0, Math.min(password.length(), 3)) +
                            password.substring(3, Math.min(password.length(), 20)).replaceAll(".", "*") :
                    "Cracked account";

            String altTitle = !Objects.equals(alt.getType(), "cracked") ? alt.getAlias() + " §7- " + maskedPassword : alt.getUsername();
            fonts.getProductSans().drawStringWithShadow(altTitle, 40, y + 7, -1);

            Date date = new Date(alt.getCreationDate());

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));

            String formattedDate = dateFormat.format(date);
            fonts.getProductSans().drawStringWithShadow(formattedDate, 40, y + 22, 0xFFAAAAAA);

            RenderUtils.drawImage(new ResourceLocation("virago/textures/menu/head.jpg"), 5, y + 5, 28, 28);
            GlStateManager.popMatrix();
        }

        @Override
        protected int getScrollBarX() {
            return width - 4;
        }
    }
}
