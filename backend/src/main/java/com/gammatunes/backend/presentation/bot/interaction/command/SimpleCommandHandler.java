package com.gammatunes.backend.presentation.bot.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SimpleCommandHandler extends CommandHandler {
    void execute(SlashCommandInteractionEvent event) throws Exception;
}
