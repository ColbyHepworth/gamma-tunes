package com.gammatunes.backend.presentation.bot.interaction.command.player;


import com.gammatunes.backend.presentation.bot.control.DiscordAudioController;
import com.gammatunes.backend.presentation.bot.interaction.command.CommandHandler;
import com.gammatunes.backend.presentation.bot.player.service.PlayerMessageService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * A command to create the persistent player message.
 */
@Component
public class GetPlayerCommandHandler implements CommandHandler {

    private final DiscordAudioController discordAudioController;
    private final PlayerMessageService messageManager;

    public GetPlayerCommandHandler(DiscordAudioController discordAudioController, PlayerMessageService messageManager) {
        this.discordAudioController = discordAudioController;
        this.messageManager = messageManager;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("player", "Displays the interactive music player.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            messageManager.create(
                Objects.requireNonNull(event.getGuild()).getId(),
                (TextChannel) event.getChannel()
            );
            event.reply("Player message created!").setEphemeral(true).queue();
        } else {
            event.reply("This command can only be used in a text channel.").setEphemeral(true).queue();
        }
    }
}
