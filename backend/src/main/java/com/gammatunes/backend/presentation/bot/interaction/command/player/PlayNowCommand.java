package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.domain.exception.TrackLoadException;
import com.gammatunes.backend.presentation.bot.interaction.command.QueryCommand;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import com.gammatunes.backend.domain.exception.MemberNotInVoiceChannelException;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;


@Component
public class PlayNowCommand extends PlayerCommand implements QueryCommand {

    public PlayNowCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("playnow", "Plays the current track immediately in the voice channel.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws TrackLoadException, MemberNotInVoiceChannelException {
        String query = getQuery(event);
        discordPlayerController.playNow(member, query);
    }
}
