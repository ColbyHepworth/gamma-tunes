package com.gammatunes.backend.application.port.out;

import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.domain.player.AudioPlayer;


public interface PlayerRegistryPort {

    /**
     * Retrieves an existing AudioPlayer for the given guild ID, or creates a new one if it doesn't exist.
     *
     * @param Session session The session containing the guild ID for which the player should be retrieved or created.
     * @return An AudioPlayer instance associated with the specified guild.
     */
    AudioPlayer getOrCreatePlayer(Session session);

    /**
     * Removes the AudioPlayer associated with the specified guild ID.
     * @param Session session The session containing the guild ID for which the player should be removed.
     */
    void removePlayer(Session session);
}
