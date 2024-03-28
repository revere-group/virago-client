package dev.revere.virago.client.gui.panel.elements;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public class Panel extends RenderableComponent {

    private final Animation expandAnimation = new Animation(() -> 400F, false, () -> Easing.CUBIC_IN_OUT);
    private final Animation scrollAnimation = new Animation(() -> 200F, false, () -> Easing.CIRC_OUT);
    private final ArrayList<ModuleElement> elements = new ArrayList<>();
    private final EnumModuleType type;

    private float scissorHeight = 0f;
    private float scroll = 0f;
    private float real = 0f;

    private int typeSize;

    /**
     * Constructor for the Panel.
     *
     * @param type   The type of the panel.
     * @param x      The x position.
     * @param y      The y position.
     * @param width  The width.
     * @param height The height.
     */
    public Panel(EnumModuleType type, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.type = type;

        addElements();

        expandAnimation.setState(true);
    }

    /**
     * Draws the panel.
     *
     * @param mouseX     The x position of the mouse.
     * @param mouseY     The y position of the mouse.
     * @param mouseDelta The delta of the mouse.
     */
    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        float elementHeight = 0f;

        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModulesByType(type).size() != typeSize) {
            addElements();
        }

        for (ModuleElement element : elements) {
            elementHeight += element.getOffset();
        }

        scissorHeight = MathHelper.clamp_float(elementHeight, 0f, 340f);

        if (mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() + getHeight() && mouseY <= getY() + getHeight() + scissorHeight) {
            real += mouseDelta * 0.2f;
        }

        // fuck you animation you can suck my dick
        //scrollAnimation.setState(scroll != real);

        scroll = real;

        if (scroll > real) {
            float scrollDiff = scroll - real;
            scroll -= (float) ((scrollDiff / 5.0));
        }

        if (scroll < real) {
            float scrollDiff = real - scroll;
            scroll += (float) ((scrollDiff / 5.0));
        }

        scroll = MathHelper.clamp_float(scroll, -Math.max(0f, elementHeight - scissorHeight), 0f);
        real = MathHelper.clamp_float(real, -Math.max(0f, elementHeight - scissorHeight), 0f);

        RoundedUtils.shadow(getX(), getY(), getWidth(), (float) (getHeight() + scissorHeight * expandAnimation.getFactor()) - 0.5f, 5, 10f, Color.BLACK);
        RoundedUtils.round(getX() - 1, getY() - 1, getWidth() + 2, (float) (getHeight() + scissorHeight * expandAnimation.getFactor()) + 1.5f, 5, new Color(25, 25, 25, 255));
        RoundedUtils.outline(getX() - 1, getY() - 1, getWidth() + 2, (float) (getHeight() + scissorHeight * expandAnimation.getFactor()) + 1.5f, 5, 1, new Color(25, 25, 25, 255));

        if (expandAnimation.getState() && !elements.isEmpty()) {
            RenderUtils.drawRect(getX(), getY() + getHeight(), getWidth() + getX(), (getHeight() + getY()) - 0.5f, ColorUtil.getColor(false));
        }

        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        font.getProductSans().drawCenteredString(type.getName(), getX() + getWidth() / 2f, getY() + getHeight() / 2f - font.getProductSans().getHeight() / 2f, Color.WHITE.getRGB());

        RenderUtils.pushScissor(getX(), getY() + getHeight(), getWidth(), scissorHeight * expandAnimation.getFactor());

        float moduleOffset = getY() + getHeight() + scroll;

        for (ModuleElement element : elements) {
            element.setX(getX());
            element.setY(moduleOffset);

            element.draw(mouseX, mouseY, mouseDelta);
            moduleOffset += element.getOffset();
        }
        RenderUtils.popScissor();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (hovered(mouseX, mouseY) && click.equals(InteractionComponent.RIGHT)) {
            expandAnimation.setState(!expandAnimation.getState());
        }

        if (expandAnimation.getState() && mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() + getHeight() && mouseY <= getY() + getHeight() + scissorHeight) {
            for (ModuleElement module : elements) {
                module.mouseClicked(mouseX, mouseY, click);
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (ModuleElement element : elements) {
            element.keyTyped(typedChar, keyCode);
        }
    }

    private void addElements() {
        elements.clear();
        typeSize = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModulesByType(type).size();
        ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);

        List<AbstractModule> modules = moduleService.getModulesByType(type);
        modules.sort(Comparator.comparing(AbstractModule::getName));

        modules.forEach(module -> elements.add(new ModuleElement(module, -2000, -2000, getWidth(), getHeight())));
    }
}
