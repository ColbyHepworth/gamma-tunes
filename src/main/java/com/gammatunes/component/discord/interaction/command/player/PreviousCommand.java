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
 * Command to skip to the previous track in the audio player queue.
 * This command interacts with the DiscordAudioController to perform the action.
 */
@Component
@RequiredArgsConstructor
public class PreviousCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        if (member == null) {
            return Mono.error(new IllegalStateException("This command can only be used in a server."));
        }
        return discordPlayerService.previous(member).then();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("previous", "Moves to the previous track in the queue.");
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("⏮️ Playing previous track.", true));
    }
}
