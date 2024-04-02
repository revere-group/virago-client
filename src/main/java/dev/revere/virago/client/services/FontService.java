package dev.revere.virago.client.services;

import dev.revere.virago.api.font.FontRenderer;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/19/2024
 */
@Getter
public class FontService implements IService {
    private FontRenderer productSans;
    private FontRenderer productSans28;
    private FontRenderer arial;
    private FontRenderer playBold;
    private FontRenderer playRegular;
    private FontRenderer poppinsMedium;
    private FontRenderer ralewayBlack;
    private FontRenderer ralewayExtraBold;
    private FontRenderer ralewayExtraBold14;
    private FontRenderer ralewayExtraBold26;
    private FontRenderer ralewayExtraLight;
    private FontRenderer ralewayLight;
    private FontRenderer ralewaySemiBold;
    private FontRenderer ralewaySemiBold14;
    private FontRenderer robotoBold;
    private FontRenderer robotoLight;
    private FontRenderer robotoMedium;
    private FontRenderer robotoRegular;
    private FontRenderer ubuntuBold;
    private FontRenderer ubuntuMedium;
    private FontRenderer ubuntuRegular;
    private FontRenderer sfProTextRegular;
    private FontRenderer visionBlack;
    private FontRenderer jetbrainsMonoBold;
    private FontRenderer icon10;

    @Override
    public void initService() {
        createFonts();
        Logger.info("Font service initialized!", getClass());
    }

    public void createFonts() {
        this.icon10 = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Icon.ttf"), 10, Font.TRUETYPE_FONT), true, true);
        this.jetbrainsMonoBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/JetBrainsMono-Bold.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.visionBlack = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Vision-Black.otf"), 20, Font.TRUETYPE_FONT), true, true);
        this.sfProTextRegular = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/SF-Pro-Text-Regular.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.productSans28 = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/product_sans.ttf"), 28, Font.TRUETYPE_FONT), true, true);
        this.productSans = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/product_sans.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.arial = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/arial.ttf"), 16, Font.TRUETYPE_FONT), true, true);
        this.poppinsMedium = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Poppins-Medium.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.playBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Play-Bold.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.playRegular = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Play-Regular.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewayBlack = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-Black.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewayExtraBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-ExtraBold.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewayExtraBold14 = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-ExtraBold.ttf"), 14, Font.TRUETYPE_FONT), true, true);
        this.ralewayExtraBold26 = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-ExtraBold.ttf"), 26, Font.TRUETYPE_FONT), true, true);
        this.ralewayExtraLight = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-ExtraLight.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewayLight = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-Light.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewaySemiBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-SemiBold.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ralewaySemiBold14 = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Raleway-SemiBold.ttf"), 14, Font.TRUETYPE_FONT), true, true);
        this.robotoBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Roboto-Bold.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.robotoLight = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Roboto-Light.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.robotoMedium = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Roboto-Medium.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.robotoRegular = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Roboto-Regular.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ubuntuBold = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Ubuntu-B.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ubuntuMedium = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Ubuntu-M.ttf"), 20, Font.TRUETYPE_FONT), true, true);
        this.ubuntuRegular = new FontRenderer(fontFromTTF(new ResourceLocation("virago/fonts/Ubuntu-R.ttf"), 20, Font.TRUETYPE_FONT), true, true);
    }

    public Font fontFromTTF(ResourceLocation resourceLocation, float fontSize, int fontType) {
        Font font = null;
        try {
            font = Font.createFont(fontType, Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream());
            font = font.deriveFont(fontSize);
        } catch (Exception e) {
            Logger.err("Error loading font from TTF: " + font + ":" + resourceLocation, e.getClass());
        }
        return font;
    }
}
