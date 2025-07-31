package com.gammatunes.backend.presentation.bot.interaction.command.view;


import com.gammatunes.backend.presentation.bot.interaction.command.BotCommand;
import com.gammatunes.backend.presentation.bot.player.service.PlayerPanelCoordinator;
import net.dv8tion.jda.api.entities.Guild;
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
public class GetPlayerCommandHandler implements BotCommand {

    private final PlayerPanelCoordinator panelCoordinator;

    public GetPlayerCommandHandler(PlayerPanelCoordinator panelCoordinator) {
        this.panelCoordinator = panelCoordinator;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("player", "Displays the interactive music player.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!(event.getChannel() instanceof TextChannel channel)) {
            event.reply("This command can only be used in a text channel.")
                .setEphemeral(true).queue();
            return;
        }
        Guild guild = Objects.requireNonNull(event.getGuild(), "Guild cannot be null");
        panelCoordinator.createPanel(guild.getId(), channel);
    }
}
