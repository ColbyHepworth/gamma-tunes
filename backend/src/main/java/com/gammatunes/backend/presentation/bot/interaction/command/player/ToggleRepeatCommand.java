package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.domain.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;


@Component
public class ToggleRepeatCommand extends PlayerCommand {

    protected ToggleRepeatCommand(DiscordPlayerController discordPlayerController) {
        super(discordPlayerController);
    }

    @Override
    protected void handle(Member member, SlashCommandInteractionEvent event) throws MemberNotInVoiceChannelException {
        discordPlayerController.toggleRepeat(member);
        event.reply("Repeat mode toggled!").queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("repeat", "Toggle repeat mode for the current track")
                .setGuildOnly(true);
    }
}
