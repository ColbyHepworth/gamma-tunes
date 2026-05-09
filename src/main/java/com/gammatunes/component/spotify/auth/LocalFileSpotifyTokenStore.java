package com.gammatunes.component.spotify.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class LocalFileSpotifyTokenStore implements SpotifyTokenStore {

    private static final Path TOKEN_STORE_PATH = Path.of(".local", "spotify-tokens.json");
    private static final TypeReference<Map<Long, LinkedSpotifyAccount>> STORE_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final Map<Long, LinkedSpotifyAccount> accountsByDiscordUser;

    public LocalFileSpotifyTokenStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.accountsByDiscordUser = loadAccounts();
    }

    @Override
    public synchronized void save(LinkedSpotifyAccount account) {
        accountsByDiscordUser.put(account.discordUserId(), account);
        writeAccounts();
        log.info("Saved Spotify link for Discord user {} to {}", account.discordUserId(), TOKEN_STORE_PATH);
    }

    @Override
    public synchronized Optional<LinkedSpotifyAccount> findByDiscordUserId(long discordUserId) {
        return Optional.ofNullable(accountsByDiscordUser.get(discordUserId));
    }

    private Map<Long, LinkedSpotifyAccount> loadAccounts() {
        if (!Files.exists(TOKEN_STORE_PATH)) {
            return new HashMap<>();
        }

        try {
            return new HashMap<>(objectMapper.readValue(TOKEN_STORE_PATH.toFile(), STORE_TYPE));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read Spotify token store: " + TOKEN_STORE_PATH, exception);
        }
    }

    private void writeAccounts() {
        try {
            Files.createDirectories(TOKEN_STORE_PATH.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(TOKEN_STORE_PATH.toFile(), accountsByDiscordUser);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write Spotify token store: " + TOKEN_STORE_PATH, exception);
        }
    }
}
