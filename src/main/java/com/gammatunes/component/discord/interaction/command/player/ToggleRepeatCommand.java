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
 * Command to toggle repeat mode for the current track.
 * This command allows users to repeat the currently playing track.
 */
@Component
@RequiredArgsConstructor
public class ToggleRepeatCommand extends PlayerCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        return discordPlayerService.toggleRepeat(member).then();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("repeat", "Toggle repeat mode for the current track");
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        if (event.getMember() == null) {
            return Mono.error(new IllegalStateException("This command can only be used in a server."));
        }
        return discordPlayerService.getRepeat(event.getMember())
            .map(on -> CommandResult.toast(on ? "Repeat enabled." : "Repeat disabled.", true));
    }
}
