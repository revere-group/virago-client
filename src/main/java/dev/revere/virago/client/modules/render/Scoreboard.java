package dev.revere.virago.client.modules.render;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.revere.virago.Virago;
import dev.revere.virago.api.draggable.Draggable;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.game.TickEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.services.DraggableService;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ModuleData(name = "Scoreboard", description = "Modify the scoreboard aesthetics.", type = EnumModuleType.RENDER)
public class Scoreboard extends AbstractModule {

    private final Setting<Boolean> shadow = new Setting<>("Text Shadow", true);
    private final Setting<Boolean> rounded = new Setting<>("Rounded", true);
    private final Setting<Long> roundingRadius = new Setting<>("Rounding Radius", 7L)
            .minimum(1L)
            .maximum(30L)
            .incrementation(1L)
            .describedBy("The amount of rounding on the scoreboard")
            .visibleWhen(rounded::getValue);

    ScaledResolution sr = new ScaledResolution(mc);

    private Draggable draggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "Scoreboard",sr.getScaledWidth() / 2, sr.getScaledHeight() / 2));

    private Collection<Score> scores;
    private ScoreObjective objective;
    private int maxWidth;

    @EventHandler
    public Listener<Render2DEvent> onRender2D = event -> {
        if (this.objective != null) {
            renderScoreboard((int) draggable.getX(), (int) draggable.getY());
        }
    };

    @EventHandler
    public final Listener<TickEvent> onTick = event -> {
        this.objective = getObjective();

        if (this.objective != null) {
            Collection<Score> scores = this.objective.getScoreboard().getSortedScores(this.objective);
            List<Score> scoreList = scores.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());

            Collections.reverse(scoreList);

            if (scoreList.size() > 15) {
                this.scores = Lists.newArrayList(Iterables.skip(scoreList, scores.size() - 15));
            } else {
                this.scores = scoreList;
            }

            this.maxWidth = mc.fontRendererObj.getStringWidth(this.objective.getDisplayName());

            for (Score score : scores) {
                ScorePlayerTeam scorePlayerTeam = this.objective.getScoreboard().getPlayersTeam(score.getPlayerName());
                String formattedName = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());
                this.maxWidth = Math.max(this.maxWidth, mc.fontRendererObj.getStringWidth(formattedName));
            }
        }
    };

    private ScoreObjective getObjective() {
        net.minecraft.scoreboard.Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = null;
        ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(mc.thePlayer.getName());

        if (scorePlayerTeam != null) {
            int colorIndex = scorePlayerTeam.getChatFormat().getColorIndex();

            if (colorIndex > -1) {
                objective = scoreboard.getObjectiveInDisplaySlot(3 + colorIndex);
            }
        }

        return objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);
    }

    private void renderScoreboard(int x, int y) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        int fontHeight = fontRenderer.FONT_HEIGHT;
        int padding = 2;
        int width = this.maxWidth;

        int scoreboardHeight = (this.scores.size() + 1) * (fontHeight + padding);

        draggable.setWidth(width);
        draggable.setHeight(scoreboardHeight);

        if(rounded.getValue()) {
            RenderUtils.drawRoundedRect(x, y, width + padding * 4, scoreboardHeight + padding, roundingRadius.getValue(), 0x4F000000);
        } else {
            Color color = new Color(0x4F000000, true);
            RenderUtils.rect(x, y, width + padding * 4, scoreboardHeight + padding, color);
        }

        int fontColor = 553648127;

        x += padding * 2;
        y += padding;

        String objective = this.objective.getDisplayName();
        fontRenderer.drawString(objective, x + maxWidth / 2.0F - fontRenderer.getStringWidth(objective) / 2.0F, y, fontColor, shadow.getValue());

        y += fontHeight + padding;

        for (Score score : this.scores) {
            ScorePlayerTeam scorePlayerTeam = this.objective.getScoreboard().getPlayersTeam(score.getPlayerName());
            String formattedName = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());
            fontRenderer.drawString(formattedName, x, y, fontColor, shadow.getValue());
            y += fontHeight + padding;
        }
    }

}