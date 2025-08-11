package com.gammatunes.component.discord.interaction.command.player;

import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Command to stop the audio player, clear the queue, and disconnect the bot from the voice channel.
 * This command interacts with the audio service to manage playback and connection state.
 */
@Component
@RequiredArgsConstructor
public class StopCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("stop", "Stops the player, clears the queue, and disconnects the bot.");
    }

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return discordPlayerService.stop(member).then();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("⏹️ Player stopped and queue cleared.", true));
    }
}
