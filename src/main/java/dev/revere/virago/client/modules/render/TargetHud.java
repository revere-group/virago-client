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
import dev.revere.virago.client.events.attack.AttackEvent;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.update.JoinEvent;
import dev.revere.virago.client.events.update.LeaveEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.DraggableService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.lwjgl.Sys;

import java.awt.*;
import java.util.List;

@ModuleData(name = "Target Hud", description = "Modify the target hud aesthetics", type = EnumModuleType.RENDER)
public class TargetHud extends AbstractModule {

    private final Setting<Boolean> shadow = new Setting<>("Text Shadow", true);
    public Setting<FontType> fontType = new Setting<>("Font", FontType.PRODUCT_SANS).describedBy("The font type to use.");
    private final Setting<Boolean> rounded = new Setting<>("Rounded", true);
    private final Setting<Long> roundingRadius = new Setting<>("Rounding Radius", 7L)
            .minimum(1L)
            .maximum(30L)
            .incrementation(1L)
            .describedBy("The amount of rounding on the scoreboard")
            .visibleWhen(rounded::getValue);

    private Draggable draggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "Target Hud", 300, 300));

    FontRenderer fontRenderer;

    private KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);

    @EventHandler
    public Listener<Render2DEvent> onRender2D = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        getFont(font);


        if(killAura.getSingleTarget() == null) {
            return;
        }

        if(!(killAura.getSingleTarget() instanceof AbstractClientPlayer)) {

            return;
        }

        AbstractClientPlayer target = (AbstractClientPlayer) killAura.getSingleTarget();

        if(rounded.getValue()) {
            RenderUtils.drawRoundedRect(draggable.getX(), draggable.getY(), 110, 45, roundingRadius.getValue(), 0x4F000000);
        } else {
            Color color = new Color(0x4F000000, true);
            RenderUtils.rect(draggable.getX(), draggable.getY(), 100, 100, color);
        }

        fontRenderer.drawStringWithShadow("Target Hud", draggable.getX() + 2, draggable.getY() + 2, -1);
        fontRenderer.drawStringWithShadow("Health: " + target.getHealth(), draggable.getX() + 2, draggable.getY() + 17, -1);

        draggable.setWidth(100);
        draggable.setHeight(100);
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

    public enum FontType {
        PRODUCT_SANS,
        JETBRAINS,
        POPPINS,
        SF_PRO
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
