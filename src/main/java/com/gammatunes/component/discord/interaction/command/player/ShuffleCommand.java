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
 * Command to shuffle the current queue in the voice channel.
 * This command allows users to randomize the order of tracks in the queue.
 */
@Component
@RequiredArgsConstructor
public class ShuffleCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return discordPlayerService.shuffle(member).then();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("shuffle", "Shuffles the current queue.");
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("🔀 Shuffled the queue.", true));
    }
}
