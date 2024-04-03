package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.draggable.Draggable;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.font.FontRenderer;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.update.JoinEvent;
import dev.revere.virago.client.events.update.LeaveEvent;
import dev.revere.virago.client.services.DraggableService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.lwjgl.Sys;

import java.awt.*;
import java.util.List;

@ModuleData(name = "Session Info", description = "Modify the scoreboard aesthetics.", type = EnumModuleType.RENDER)
public class SessionInfo extends AbstractModule {

    private final Setting<Boolean> shadow = new Setting<>("Text Shadow", true);
    public Setting<FontType> fontType = new Setting<>("Font", FontType.PRODUCT_SANS).describedBy("The font type to use.");
    private final Setting<Integer> opacity = new Setting<>("Opacity", 180)
            .minimum(0)
            .maximum(255)
            .incrementation(1)
            .describedBy("The opacity for background");

    private Draggable draggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "Session Info", 200, 200));

    FontRenderer fontRenderer;
    private int kills = 0;
    private int wins = 0;
    private long sessionTime;

    @EventHandler
    public Listener<Render2DEvent> onRender2D = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        getFont(font);

        RenderUtils.rect(draggable.getX(), draggable.getY() - 1, 115, 15, new Color(0,0,0, 120));
        RenderUtils.rect(draggable.getX(), draggable.getY() - 1, 115, 50, new Color(0,0,0, opacity.getValue()));
        RoundedUtils.shadowGradient(draggable.getX(), draggable.getY() - 1, 115, 50, 1, 5, 5, new Color(0,0,0, 100), new Color(0,0,0, 100), new Color(0,0,0, 100), new Color(0,0,0, 100), false);
        RenderUtils.renderGradientRect((int) draggable.getX(), (int) draggable.getY() + 14, (int) (115 + draggable.getX()), (int) (draggable.getY() + 15), 5.0, 2000L, 2L, RenderUtils.Direction.RIGHT);
        //RoundedUtils.round(draggable.getX(), draggable.getY() - 1, 113, 52, 4, new Color(20,20,20, 200));

        fontRenderer.drawStringWithShadow("Session Stats", draggable.getX() + 25, draggable.getY() + 2, -1);

        font.getIcon10().drawString("b", draggable.getX() + 2, draggable.getY() + 22, -1);
        fontRenderer.drawStringWithShadow("Duration: " + getSessionTime(), draggable.getX() + 8, draggable.getY() + 18, -1);

        font.getIcon10().drawString("a", draggable.getX() + 2, draggable.getY() + 31, -1);
        fontRenderer.drawStringWithShadow("Kills: " + this.kills, draggable.getX() + 8, draggable.getY() + 27, -1);

        font.getIcon10().drawString("v", draggable.getX() + 2, draggable.getY() + 40, -1);
        fontRenderer.drawStringWithShadow("Wins: " + this.wins, draggable.getX() + 8, draggable.getY() + 36, -1);

        draggable.setWidth(100);
        draggable.setHeight(100);
    };

    @EventHandler
    public final Listener<PacketEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle s45 = (S45PacketTitle) event.getPacket();
            if (s45.getMessage() == null) return;

            if (StringUtils.stripControlCodes(s45.getMessage().getUnformattedText()).equals("VICTORY!")) {
                this.wins++;
            }
        }
    };

    @EventHandler
    public final Listener<JoinEvent> onJoin = event -> {
        System.out.println("Join event called, set sessionTime");
        this.sessionTime = System.currentTimeMillis();
    };

    @EventHandler
    public final Listener<LeaveEvent> onLeave = event -> {
        System.out.println("Leave event called, reset sessionTime");
        this.sessionTime = 0;
    };

    /**
     * Gets the font
     *
     * @param font The font service
     */
    private void getFont(FontService font) {
        switch (fontType.getValue()) {
            case PRODUCT_SANS:
                fontRenderer = font.getProductSans();
                break;
            case POPPINS:
                fontRenderer = font.getPoppinsMedium();
                break;
            case SF_PRO:
                fontRenderer = font.getSfProTextRegular();
                break;
            case JETBRAINS:
                fontRenderer = font.getJetbrainsMonoBold();
                break;
        }
    }

    private String getSessionTime() {
        if(this.sessionTime == 0)
            return "Invalid Time";

        return DurationFormatUtils.formatDuration(System.currentTimeMillis() - sessionTime, "H's,' m'm,' s's'");
    }

    public enum FontType {
        PRODUCT_SANS,
        JETBRAINS,
        POPPINS,
        SF_PRO
    }

    @Override
    public void onEnable() {
        this.sessionTime = System.currentTimeMillis();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.sessionTime = 0;
        super.onDisable();
    }
}
