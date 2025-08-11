package com.gammatunes.component.discord.ui.renderer;

import com.gammatunes.model.dto.PlayerView;
import com.gammatunes.component.discord.ui.constants.UiConstants;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Renderer for the player controls in the Discord UI.
 * This class generates the primary and secondary action buttons for controlling the player.
 */
@Component
@Order(100)
public final class ControlsRenderer implements ComponentRenderer {

    @Override
    public List<ActionRow> render(PlayerView playerView) {
        return List.of(
            ActionRow.of(buildPrimaryButtons(playerView)),
            ActionRow.of(buildSecondaryButtons())
        );
    }

    public List<Button> buildPrimaryButtons(PlayerView playerView) {
        boolean isPlaying = "PLAYING".equals(playerView.state());
        boolean isRepeatEnabled = playerView.repeatEnabled();

        return List.of(
            Button.secondary("player:shuffle", UiConstants.SHUFFLE),
            Button.secondary("player:previous", UiConstants.PREVIOUS),
            isPlaying ? Button.primary("player:pause", UiConstants.PAUSE)
                : Button.success("player:resume", UiConstants.PLAY),
            Button.secondary("player:skip", UiConstants.SKIP),
            isRepeatEnabled ? Button.primary("player:repeat", UiConstants.REPEAT)
                : Button.secondary("player:repeat", UiConstants.REPEAT)
        );
    }

    public List<Button> buildSecondaryButtons() {
        return List.of(
            Button.danger("player:stop", UiConstants.STOP)
        );
    }
}
