package com.gammatunes.component.discord.interaction.command.player;

import com.gammatunes.component.discord.interaction.command.QueryCommand;
import com.gammatunes.service.DiscordPlayerService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Command to play a song or add it to the queue in the voice channel.
 * This command interacts with the audio service to handle track loading and playback.
 */
@Component
@RequiredArgsConstructor
public class PlayCommand extends PlayerCommand implements QueryCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("play", "Plays a song or adds it to the queue.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        String query = getQuery(event);
        TextChannel channel = (TextChannel) event.getChannel();
        return discordPlayerService.play(member, query, channel).then();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("🎵 Added to queue.", true));
    }
}
