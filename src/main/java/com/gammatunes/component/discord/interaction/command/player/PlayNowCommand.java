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
 * Command to play a track immediately in the voice channel.
 * This command allows users to play a song directly without adding it to the queue.
 */
@Component
@RequiredArgsConstructor
public class PlayNowCommand extends PlayerCommand implements QueryCommand {

    private final DiscordPlayerService discordPlayerService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("playnow", "Plays the current track immediately in the voice channel.")
            .addOption(OptionType.STRING, "query", "The song URL or search term.", true);
    }

    @Override
    protected Mono<Void> handle(Member member, SlashCommandInteractionEvent event) {
        String query = getQuery(event);
        TextChannel channel = (TextChannel) event.getChannel();
        return discordPlayerService.playNow(member, query, channel).then();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("🎵 Playing now.", true));
    }
}
