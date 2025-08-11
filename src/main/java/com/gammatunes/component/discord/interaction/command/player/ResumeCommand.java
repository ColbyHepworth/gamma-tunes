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
 * Command to stop the audio player and clear the queue.
 * This command interacts with the backend service to perform the stop operation.
 */
@Component
@RequiredArgsConstructor
public class ResumeCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("resume", "Resumes the paused player.");
    }


    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return discordPlayerService.resume(member).then();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("▶️ Resumed", true));
    }

}
