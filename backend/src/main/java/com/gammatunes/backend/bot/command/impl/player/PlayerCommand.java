package com.gammatunes.backend.bot.command.impl.player;

import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.view.PlayerMessageManager;
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
public class PlayerCommand implements Command {

    private final PlayerMessageManager messageManager;

    public PlayerCommand(PlayerMessageManager messageManager) {
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
