package com.gammatunes.backend.presentation.bot.player.view.renderer;

import com.gammatunes.backend.domain.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;

public final class StatusFieldRenderer implements FieldRenderer {

    @Override
    public void render(EmbedBuilder eb, AudioPlayer player, String status) {
        if (status != null && !status.isBlank()) {
            eb.addField("Status", status, false);
        }
    }
}

