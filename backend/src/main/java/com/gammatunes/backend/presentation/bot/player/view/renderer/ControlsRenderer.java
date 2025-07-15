package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.model.PlayerState;
import com.gammatunes.backend.domain.player.AudioPlayer;
import com.gammatunes.backend.presentation.ui.UiConstants;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public final class ControlsRenderer {

    public List<Button> buildButtons(AudioPlayer player) {
        return List.of(
            Button.secondary("player:previous", UiConstants.PREVIOUS),
            player.getState() == PlayerState.PLAYING
                ? Button.primary("player:pause", UiConstants.PAUSE)
                : Button.success("player:resume", UiConstants.PLAY),
            Button.secondary("player:skip", UiConstants.SKIP),
            Button.danger   ("player:stop", UiConstants.STOP)
        );
    }
}
