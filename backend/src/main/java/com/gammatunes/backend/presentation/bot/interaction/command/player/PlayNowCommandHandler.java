package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.infrastructure.source.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.PlayerQueryCommandHandler;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.springframework.stereotype.Component;


@Component
public class PlayNowCommandHandler extends PlayerQueryCommandHandler {

    public PlayNowCommandHandler(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("playnow", "Plays the current track immediately in the voice channel.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    protected PlayerOutcomeResult handle(Member member, SlashCommandInteractionEvent event) throws TrackLoadException, MemberNotInVoiceChannelException {
        String query = getQuery(event);
        return new PlayerOutcomeResult (discordPlayerController.playNow(member, query), query);
    }
}
