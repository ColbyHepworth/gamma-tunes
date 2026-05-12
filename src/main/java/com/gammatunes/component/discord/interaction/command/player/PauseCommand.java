package com.gammatunes.component.discord.interaction.command.player;

import com.gammatunes.service.playback.control.PlaybackControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PauseCommand extends PlayerCommand {

    private final PlaybackControlService playbackControlService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("pause", "Pauses the current track.");
    }

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return playbackControlService.pause(member).then();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("️️⏸️ Paused the current track.", true));
    }
}
