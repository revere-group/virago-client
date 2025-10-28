package dev.revere.virago.client.gui.menu.altmanager;

import dev.revere.virago.Virago;
import dev.revere.virago.api.alt.Alt;
import dev.revere.virago.client.gui.menu.altmanager.components.GuiAltButton;
import dev.revere.virago.client.gui.menu.altmanager.components.GuiMainButton;
import dev.revere.virago.client.services.AltService;
import dev.revere.virago.client.services.DesignService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.alt.CookieLogin;
import dev.revere.virago.util.misc.ScrollUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import dev.revere.virago.util.shader.GLSLSandboxShader;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Remi
 * @project Virago
 * @date 5/4/2024
 */
public class GuiAltManager extends GuiScreen {
    private final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("virago/textures/bg.png");

    private final List<GuiAltButton> altAccountButtons = new ArrayList<>();
    private final List<GuiMainButton> altButtons = new ArrayList<>();
    private final ScrollUtil scrollUtils = new ScrollUtil();
    private final GuiScreen prevScreen;

    private GLSLSandboxShader backgroundShader;

    private Alt selectedAlt;
    private int pressedTime;

    public GuiAltManager(GuiScreen prevScreen) {
        this.createAltPanel();
        this.prevScreen = prevScreen;
        this.altButtons.add(new GuiMainButton("Add Alt", 1, 100, 20));
        this.altButtons.add(new GuiMainButton("Delete Alt", 2, 100, 20));
        this.altButtons.add(new GuiMainButton("Clear Alts", 3, 100, 20));
        this.altButtons.add(new GuiMainButton("Cookie", 4, 100, 20));
        this.altButtons.add(new GuiMainButton("Edit", 5, 100, 20));
        this.altButtons.add(new GuiMainButton("Use", 6, 100, 20));
        this.altButtons.add(new GuiMainButton("Microsoft", 7, 100, 20));
        try {
            this.backgroundShader = new GLSLSandboxShader(Virago.getInstance().getServiceManager().getService(DesignService.class).getSelectedDesign().getShaderPath());
        } catch (Exception e) {
            Logger.err("Failed to load background shader. " + e.getMessage(), getClass());
        }
        Virago.getInstance().getServiceManager().getService(AltService.class).setStatus(" ");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        AltService altService = Virago.getInstance().getServiceManager().getService(AltService.class);
        FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
        GlStateManager.disableCull();
        this.backgroundShader.useShader(this.width * 2, this.height * 2, mouseX, mouseY, (System.currentTimeMillis() - Virago.getInstance().getStarted()) / 1000f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
        GL20.glUseProgram(0);
        //RenderUtils.drawImage(BACKGROUND_TEXTURE, 0, 0, width, height);

        GL11.glPushMatrix();
        GL11.glEnable(3089);
        prepareScissorBox(0.0F, 50, this.width, (this.height - 50));

        for (GuiAltButton button : this.altAccountButtons) {
            button.x = (isHovered(mouseY, 50, this.height - 50) && button.isHovered(mouseX, mouseY)) ? (float) this.width / 2 - 103 : (float) this.width / 2 - 100;
            button.y = scrollUtils.getScrollY(50) + button.getId() * 35 - scrollUtils.getScroll(50, (altAccountButtons.size() * 35 - this.height) + 100, true);
            button.setSelected(selectedAlt == button.alt);
            button.drawScreen();
        }

        GL11.glDisable(3089);
        GL11.glPopMatrix();

        RoundedUtils.shadowGradient(-2, 0, this.width + 4, 45, 0, 5, 10, new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), false);
        fontService.getProductSans().drawCenteredString("Alt Manager | " + altService.getAlts().size(), (float) width / 2, 13, -1);
        fontService.getProductSans().drawCenteredString("Logged into " + EnumChatFormatting.GREEN + Minecraft.getMinecraft().getSession().getUsername(), (float) width / 2, 25, -1);
        fontService.getProductSans().drawStringWithShadow(altService.getStatus(), width - fontService.getProductSans().getStringWidth(altService.getStatus()) - 8, 8, -1);

        scrollUtils.drawScroll((float) this.width / 2 + 105, this.height - 110, 5, 100, mouseX, mouseY, -1);
        RoundedUtils.shadowGradient((float) -2, this.height - 50, this.width + 4, 100, 0, 5, 10, new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), new Color(50, 50, 50, 255), false);

        for (GuiMainButton button : altButtons) {
            if (button.getId() <= 3) {
                button.setX((float) this.width / 2 - 250 + button.getId() * 105);
                button.setY(this.height - 45);
            } else {
                button.setX((float) this.width / 2 - 628 + button.getId() * 105);
                button.setY(this.height - 22);
            }
            button.drawButtonCenteredString(mc, mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        AltService altService = Virago.getInstance().getServiceManager().getService(AltService.class);
        for (GuiMainButton button : altButtons) {
            if (button.isHovered()) {
                switch (button.getId()) {
                    case 1: {
                        int targetStringLength = 16 - 7 - 4;
                        int leftLimit = 97;
                        int rightLimit = 123;
                        Random random = new Random();
                        String generatedString = "Virago" + "_" +
                                random
                                        .ints(leftLimit, rightLimit)
                                        .limit(targetStringLength)
                                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                        .toString() + Minecraft.getSystemTime() % 1000L;

                        altService.addAlt(new Alt(generatedString, generatedString, "", "cracked", "cracked"));
                        altService.setStatus("");
                        altService.save();

                        AltLoginThread thread = new AltLoginThread(generatedString, "");
                        thread.start();

                        createAltPanel();
                        break;
                    }
                    case 2: {
                        if (selectedAlt != null) {
                            altService.getAlts().remove(selectedAlt);
                            altService.save();
                            createAltPanel();
                            selectedAlt = null;
                        }
                        break;
                    }
                    case 3: {
                        altService.getAlts().clear();
                        altService.save();
                        createAltPanel();
                        break;
                    }
                    case 4: {
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
                                        altService.setStatus(EnumChatFormatting.RED + "Failed to login with cookie.");
                                        Logger.err("Failed to login with cookie.", getClass());
                                        return;
                                    }

                                    mc.session = new Session(loginData.username, loginData.uuid, loginData.mcToken, "legacy");
                                    altService.addAlt(new Alt(loginData.username, loginData.username, loginData.mcToken, "cookie", loginData.uuid));
                                    altService.setStatus("");
                                } catch (Exception e) {
                                    altService.setStatus(EnumChatFormatting.RED + "Failed to login with cookie.");
                                    Logger.err("Failed to login with cookie: " + e.getMessage(), getClass());
                                }
                            }
                            altService.save();
                            createAltPanel();
                        }).start();
                        break;
                    }
                    case 6: {
                        if (selectedAlt != null) {
                            AltLoginThread thread = new AltLoginThread(selectedAlt.getUsername(), "");
                            thread.start();
                            createAltPanel();
                        }
                        break;
                    }
                    case 7: {
                        try {
                            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                            MicrosoftAuthResult result = authenticator.loginWithWebview();

                            if (result == null) {
                                altService.setStatus(EnumChatFormatting.RED + "Failed to login with Microsoft account.");
                                Logger.err("Failed to login with Microsoft account.", getClass());
                                break;
                            }

                            MinecraftProfile profile = result.getProfile();
                            Logger.info("Logged in as " + profile.getName() + " with UUID " + profile.getId() + " and access token " + result.getAccessToken() + ".", getClass());
                            mc.session = new Session(profile.getName(), profile.getId(), result.getAccessToken(), "microsoft");
                            altService.addAlt(new Alt(profile.getName(), profile.getName(), result.getAccessToken(), "cookie", profile.getId()));
                            altService.setStatus("");
                            altService.save();
                        } catch (Exception e) {
                            altService.setStatus(EnumChatFormatting.RED + "Failed to login with Microsoft account.");
                            Logger.err("Failed to login with Microsoft account: " + e.getMessage(), getClass());
                        }
                        createAltPanel();
                        break;
                    }
                }
            }
        }
        if (isHovered(mouseY, 50, this.height - 50)) {
            for (GuiAltButton button : this.altAccountButtons) {
                if (button.isHovered(mouseX, mouseY)) {
                    if (mouseButton == 0) {
                        pressedTime++;
                        if (pressedTime == 1) {
                            if (selectedAlt != button.alt) {
                                selectedAlt = button.alt;
                            }
                        }
                        if (selectedAlt == button.alt) {
                            if (pressedTime >= 2) {
                                if (Objects.equals(selectedAlt.getType(), "cookie")) {
                                    mc.session = new Session(selectedAlt.getUsername(), selectedAlt.getUuid(), selectedAlt.getPassword(), "legacy");
                                    altService.setStatus("");
                                    return;
                                }
                                if (Objects.equals(selectedAlt.getType(), "cracked")) {
                                    if (altService.isValidCrackedAlt(selectedAlt.getUsername())) {
                                        mc.session = new Session(selectedAlt.getUsername(), "", "", "mojang");
                                        altService.setStatus("");
                                    } else {
                                        altService.setStatus(EnumChatFormatting.RED + "Invalid Username!");
                                        Logger.err("Invalid cracked alt.", getClass());
                                    }
                                    return;
                                }
                                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                                try {
                                    MicrosoftAuthResult result = authenticator.loginWithCredentials(selectedAlt.getUsername(), selectedAlt.getPassword());
                                    MinecraftProfile profile = result.getProfile();
                                    mc.session = new Session(profile.getName(), profile.getId(), result.getAccessToken(), "microsoft");
                                    altService.setStatus("");
                                } catch (MicrosoftAuthenticationException e) {
                                    altService.setStatus(EnumChatFormatting.RED + "Failed to login with Microsoft account.");
                                    Logger.err("Failed to login.", getClass());
                                }
                                mc.displayGuiScreen(new GuiMultiplayer(this.prevScreen));
                                pressedTime = 0;
                            }
                        }
                        if (selectedAlt != button.alt) {
                            selectedAlt = button.alt;
                            pressedTime = 1;
                        }
                    }
                }
            }
        }
    }

    private boolean isHovered(int mouseY, float minY, float maxY) {
        return mouseY > minY && mouseY < maxY;
    }

    private void prepareScissorBox(float x, float y, float width, float height) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glScissor((int) (x * sr.getScaleFactor()), (int) ((sr.getScaledHeight() - height) * sr.getScaleFactor()), (int) ((width - x) * sr.getScaleFactor()), (int) ((height - y) * sr.getScaleFactor()));
    }

    private void createAltPanel() {
        altAccountButtons.clear();
        for (int i = 0; i < Virago.getInstance().getServiceManager().getService(AltService.class).getAlts().size(); i++) {
            this.altAccountButtons.add(new GuiAltButton(i, (float) this.width / 2 - 100, 50 + i * 35, 200, 30, Virago.getInstance().getServiceManager().getService(AltService.class).getAlts().get(i)));
        }
    }
}