package com.gammatunes.backend.bot;

import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.listener.InteractionListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    /**
     * Configures and starts the JDA bot.
     * Reads the bot token from environment variables using Dotenv.
     *
     * @return JDA instance if successful, null if the token is not configured.
     * @throws InterruptedException if the bot fails to start.
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
     * Updates the slash commands with Discord when the application context is refreshed.
     * This method retrieves all registered commands and updates them in Discord.
     *
     * @param event the ContextRefreshedEvent triggered when the application context is refreshed.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        JDA jda = event.getApplicationContext().getBean(JDA.class);

        InteractionListener interactionListener = event.getApplicationContext().getBean(InteractionListener.class);
        jda.addEventListener(interactionListener);

        List<Command> commands = event.getApplicationContext().getBeanProvider(Command.class).stream().toList();

        log.info("Updating slash commands with Discord...");
        jda.updateCommands()
            .addCommands(commands.stream().map(Command::getCommandData).collect(Collectors.toList()))
            .queue(
                success -> log.info("Successfully updated {} slash commands.", success.size()),
                error -> log.error("Failed to update slash commands.", error)
            );
    }
}
