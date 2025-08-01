package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.presentation.ui.UiConstants;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Renders the player controls for the audio player.
 * This component provides methods to build primary and secondary control buttons
 * for the audio player, such as play, pause, skip, and stop.
 */
@Component
@Order(3)
public final class ControlsRenderer {

    /**
     * Builds the primary buttons for the player controls.
     * These buttons are typically used for actions like play, pause, skip, etc.
     *
     * @param player The audio player instance.
     * @return A list of primary control buttons.
     */
    public List<Button> buildPrimaryButtons(AudioPlayer player) {
        return List.of(
            Button.secondary("player:shuffle", UiConstants.SHUFFLE),
            Button.secondary("player:previous", UiConstants.PREVIOUS),
            player.getState() == PlayerState.PLAYING || player.getState() == PlayerState.LOADING
                ? Button.primary("player:pause", UiConstants.PAUSE)
                : Button.success("player:resume", UiConstants.PLAY),
            Button.secondary("player:skip", UiConstants.SKIP),
            player.isRepeatEnabled()
                ? Button.primary("player:repeat", UiConstants.REPEAT)
                : Button.secondary("player:repeat", UiConstants.REPEAT)
        );
    }

    /**
     * Builds the secondary buttons for the player controls.
     * These buttons are typically used for actions like stopping playback.
     *
     * @param player The audio player instance.
     * @return A list of secondary control buttons.
     */
    public List<Button> buildSecondaryButtons(AudioPlayer player) {
        return List.of(
            Button.danger("player:stop", UiConstants.STOP)
        );
    }
}
