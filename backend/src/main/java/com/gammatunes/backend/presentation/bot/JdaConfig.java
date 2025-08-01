package com.gammatunes.backend.presentation.bot;

import com.gammatunes.backend.presentation.bot.interaction.button.ButtonInteractionHandler;
import com.gammatunes.backend.presentation.bot.interaction.command.BotCommand;
import com.gammatunes.backend.presentation.bot.interaction.command.CommandInteractionHandler;
import com.gammatunes.backend.presentation.bot.interaction.selectmenu.SelectMenuInteractionHandler;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class JdaConfig {


    /**
     * Creates and configures the JDA instance for the Discord bot.
     * This method reads the bot token from environment variables and initializes JDA with it.
     * It also sets the bot's activity to "listening to your commands".
     *
     * @return The initialized JDA instance, or null if the token is not configured.
     * @throws InterruptedException If the JDA initialization is interrupted.
     */
    @Bean
    public JDA jda() throws InterruptedException {
        Dotenv.configure().ignoreIfMissing().load();
        String botToken = System.getenv("DISCORD_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            log.warn("DISCORD_BOT_TOKEN is not configured. JDA will not be started.");
            return null;
        }

        log.info("Starting JDA bot...");
        JDA jda = JDABuilder.createDefault(botToken)
            .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
            .setActivity(Activity.listening("to your commands"))
            .build()
            .awaitReady();

        log.info("Bot is ready! Logged in as {}", jda.getSelfUser().getName());
        return jda;
    }

    /**
     * Registers interaction handlers and updates slash commands when the application context is refreshed.
     * This method is triggered after the JDA instance is created and ready.
     *
     * @param event The ContextRefreshedEvent containing the application context.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        JDA jda = event.getApplicationContext().getBean(JDA.class);

        ButtonInteractionHandler buttonInteractionHandler = event.getApplicationContext().getBean(ButtonInteractionHandler.class);
        jda.addEventListener(buttonInteractionHandler);

        CommandInteractionHandler commandInteractionHandler = event.getApplicationContext().getBean(CommandInteractionHandler.class);
        jda.addEventListener(commandInteractionHandler);

        SelectMenuInteractionHandler selectMenuInteractionHandler = event.getApplicationContext().getBean(SelectMenuInteractionHandler.class);
        jda.addEventListener(selectMenuInteractionHandler);

        List<BotCommand> botCommands = event.getApplicationContext().getBeanProvider(BotCommand.class).stream().toList();

        log.info("Updating slash commands with Discord...");
        jda.updateCommands()
            .addCommands(botCommands.stream().map(BotCommand::getCommandData).collect(Collectors.toList()))
            .queue(
                success -> log.info("Successfully updated {} slash commands.", success.size()),
                error -> log.error("Failed to update slash commands.", error)
            );
    }
}
