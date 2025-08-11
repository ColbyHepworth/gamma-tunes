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
 * Command to skip the currently playing track in the voice channel.
 * This command interacts with the audio service to skip playback.
 */
@Component
@RequiredArgsConstructor
public class SkipCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return discordPlayerService.skip(member).then();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("skip", "Skips the current track.");
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("⏭️ Skipped.", true));
    }
}
