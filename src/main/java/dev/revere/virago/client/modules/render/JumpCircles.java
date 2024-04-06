package dev.revere.virago.client.modules.render;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.input.KeyDownEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.events.update.EventEntityOptionalForce;
import dev.revere.virago.client.events.update.PreMotionEvent;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.util.render.Circle;
import dev.revere.virago.util.render.ColorUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/18/2024
 */

@Setter
@Getter
@ModuleData(name = "Jump Circles", type = EnumModuleType.RENDER, description = "Draws circles around jumpable blocks")
public class JumpCircles extends AbstractModule {
    public Setting<Boolean> customColor = new Setting<>("Fading color", false).describedBy("Whether the color should fade between 2 colors on jump");
    public Setting<Color> singleColor = new Setting<>("Static Color", new Color(-1)).visibleWhen(() -> !customColor.getValue()).describedBy("Color 1");
    public Setting<Color> customColor1 = new Setting<>("Color", new Color(-1)).visibleWhen(() -> customColor.getValue()).describedBy("Color 1");
    public Setting<Color> customColor2 = new Setting<>("Color2", new Color(-1)).visibleWhen(() -> customColor.getValue()).describedBy("Color 2");

    public JumpCircles() {
        setKey(Keyboard.KEY_P);
    }

    /**
     * Updates the circles around the player
     *
     * @param event The event to update the circles for
     */
    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        for (EntityPlayer entityPlayer : mc.thePlayer.getEntityWorld().playerEntities) {
            entityPlayer.circles.removeIf(Circle::update);
        }
    };

    /**
     * Renders the circles around the player
     *
     * @param event The event to render the circles for
     */
    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);
        for (EntityPlayer player : mc.thePlayer.getEntityWorld().playerEntities) {
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glDisable(3008);
            GL11.glDisable(2884);
            GL11.glDisable(3553);
            GL11.glShadeModel(7425);

            renderPlayerCircles(player);

            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GL11.glEnable(3008);
            GL11.glShadeModel(7424);
            GL11.glEnable(2884);
            GL11.glPopMatrix();
            GlStateManager.resetColor();
        }
    };

    /**
     * Renders the circles around the player
     *
     * @param player The player to render the circles for
     */
    private void renderPlayerCircles(EntityPlayer player) {
        for (Circle circle : player.circles) {
            GL11.glBegin(8);
            renderCircleVertices(circle);
            GL11.glEnd();
        }
    }

    /**
     * Renders the vertices of the circle
     *
     * @param circle The circle to render the vertices for
     */
    private void renderCircleVertices(Circle circle) {
        Color color = customColor.getValue() ? ColorUtil.fadeBetween(10, 4, customColor1.getValue(), customColor2.getValue()) : singleColor.getValue();

        for (int i = 0; i <= 360; i += 5) {
            Vec3 pos = circle.pos();
            double x = Math.cos(Math.toRadians(i)) * createAnimation(1.0 - circle.getAnimation(mc.timer.renderPartialTicks)) * 0.6;
            double z = Math.sin(Math.toRadians(i)) * createAnimation(1.0 - circle.getAnimation(mc.timer.renderPartialTicks)) * 0.6;
            GL11.glColor4d(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (circle.getAnimation(mc.timer.renderPartialTicks)));
            GL11.glVertex3d((pos.xCoord + x), (pos.yCoord + 0.2f), (pos.zCoord + z));
            GL11.glColor4d(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (0.2 * circle.getAnimation(mc.timer.renderPartialTicks)));
            GL11.glVertex3d((pos.xCoord + x * 1.4), (pos.yCoord + 0.2f), (pos.zCoord + z * 1.4));
        }
    }

    /**
     * Adds a circle to the player when they jump
     *
     * @param event The event that is called when the player jumps
     */
    @EventHandler
    private final Listener<EventEntityOptionalForce> eventEntityOptionalForceListener = event -> {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            double motionY = event.getEntity().posY - event.getMinor().yCoord;
            if (!player.onGround && player.onGround != player.raycastGround && motionY > 0.0 && event.getEntity() == mc.thePlayer) {
                player.circles.add(new Circle(event.getMinor()));
            }
            player.raycastGround = player.onGround;
        }
    };

    /**
     * Creates an animation for the circle
     *
     * @param value The value to create the animation for
     * @return The animation for the circle
     */
    public static double createAnimation(double value) {
        return Math.sqrt(1.0 - Math.pow(value - 1.0, 2.0));
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