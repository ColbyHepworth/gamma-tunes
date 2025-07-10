package com.gammatunes.backend.audio.lavalink;

import com.gammatunes.backend.common.model.PlayerState;
import com.gammatunes.backend.common.model.Session;
import com.gammatunes.backend.common.model.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the LavalinkPlayer adapter class.
 */
@ExtendWith(MockitoExtension.class)
class LavalinkPlayerTest {

    @Mock
    private com.sedmelluq.discord.lavaplayer.player.AudioPlayer mockLavaplayer;

    @Mock
    private AudioPlayerManager mockPlayerManager;

    private LavalinkPlayer lavalinkPlayer;

    private final Session testSession = new Session("12345");
    private final Track testTrack = new Track("test-identifier", "Test Song", "Test Artist", Duration.ofMinutes(3), "http://test.url", null);

    @BeforeEach
    void setUp() {
        // Create a new instance of our adapter before each test
        lavalinkPlayer = new LavalinkPlayer(testSession, mockLavaplayer, mockPlayerManager);
    }

    @Test
    void play_whenPlayerIsStopped_dequeuesAndStartsPlayback() {
        // Act
        lavalinkPlayer.play(testTrack);

        // Assert
        assertEquals(PlayerState.LOADING, lavalinkPlayer.getState(), "Player state should be LOADING");
        assertTrue(lavalinkPlayer.getQueue().isEmpty(), "Queue should be empty as track is immediately dequeued");
        assertEquals(Optional.of(testTrack), lavalinkPlayer.getCurrentlyPlaying(), "The track should be set as currently playing");
        // Verify that we attempt to load the item from the player manager
        verify(mockPlayerManager).loadItem(eq(testTrack.identifier()), any());
    }

    @Test
    void play_whenPlayerIsPlaying_onlyAddsToQueue() {
        // Arrange
        // Manually set the state to PLAYING to simulate an active player
        lavalinkPlayer.state.set(PlayerState.PLAYING);

        // Act
        lavalinkPlayer.play(testTrack);

        // Assert
        assertEquals(PlayerState.PLAYING, lavalinkPlayer.getState(), "Player state should remain PLAYING");
        assertEquals(1, lavalinkPlayer.getQueue().size(), "Track should be added to the queue");
        // Verify that we do NOT try to load a new item, as one is already playing
        verify(mockPlayerManager, never()).loadItem(anyString(), any());
    }

    @Test
    void pause_whenPlayerIsPlaying_pausesAndChangesState() {
        // Arrange
        lavalinkPlayer.state.set(PlayerState.PLAYING);

        // Act
        lavalinkPlayer.pause();

        // Assert
        verify(mockLavaplayer).setPaused(true);
        assertEquals(PlayerState.PAUSED, lavalinkPlayer.getState());
    }

    @Test
    void resume_whenPlayerIsPaused_resumesAndChangesState() {
        // Arrange
        lavalinkPlayer.state.set(PlayerState.PAUSED);

        // Act
        lavalinkPlayer.resume();

        // Assert
        verify(mockLavaplayer).setPaused(false);
        assertEquals(PlayerState.PLAYING, lavalinkPlayer.getState());
    }

    @Test
    void skip_whenCalled_stopsCurrentTrack() {
        // Arrange
        // Enqueue a track to set it as the currently playing one.
        lavalinkPlayer.play(testTrack);
        // Manually set the state to PLAYING to ensure the correct test conditions.
        lavalinkPlayer.state.set(PlayerState.PLAYING);
        // Act
        lavalinkPlayer.skip();

        // Assert
        // The core logic of skip is to stop the current track, which triggers the onTrackEnd event.
        verify(mockLavaplayer).stopTrack();
    }

    @Test
    void onTrackEnd_whenQueueIsEmpty_setsStateToStopped() {
        // Act
        // Directly call the internal method to simulate the event handler's action
        lavalinkPlayer.onTrackEnd();

        // Assert
        assertEquals(PlayerState.STOPPED, lavalinkPlayer.getState());
        assertTrue(lavalinkPlayer.getCurrentlyPlaying().isEmpty());
    }
}
