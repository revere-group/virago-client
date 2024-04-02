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
import dev.revere.virago.util.render.RenderUtils;
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
    private final Setting<Boolean> rounded = new Setting<>("Rounded", true);
    private final Setting<Long> roundingRadius = new Setting<>("Rounding Radius", 7L)
            .minimum(1L)
            .maximum(30L)
            .incrementation(1L)
            .describedBy("The amount of rounding on the scoreboard")
            .visibleWhen(rounded::getValue);

    private Draggable draggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "Session Info", 200, 200));

    FontRenderer fontRenderer;
    private int kills = 0;
    private int wins = 0;
    private long sessionTime;

    @EventHandler
    public Listener<Render2DEvent> onRender2D = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        getFont(font);

        if(rounded.getValue()) {
            RenderUtils.drawRoundedRect(draggable.getX(), draggable.getY(), 110, 55, roundingRadius.getValue(), 0x4F000000);
        } else {
            Color color = new Color(0x4F000000, true);
            RenderUtils.rect(draggable.getX(), draggable.getY(), 100, 100, color);
        }

        fontRenderer.drawStringWithShadow("Session Stats", draggable.getX() + 2, draggable.getY() + 2, -1);

        fontRenderer.drawStringWithShadow("Duration: " + getSessionTime(), draggable.getX() + 2, draggable.getY() + 17, -1);

        fontRenderer.drawStringWithShadow("Kills: " + this.kills, draggable.getX() + 2, draggable.getY() + 32, -1);
        fontRenderer.drawStringWithShadow("Wins: " + this.wins, draggable.getX() + 2, draggable.getY() + 42, -1);

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
