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

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private final Track testTrack1 = new Track("id1", "Song 1", "Artist 1", Duration.ofMinutes(3), "url1", null);
    private final Track testTrack2 = new Track("id2", "Song 2", "Artist 2", Duration.ofMinutes(4), "url2", null);

    @BeforeEach
    void setUp() {
        lavalinkPlayer = new LavalinkPlayer(testSession, mockLavaplayer, mockPlayerManager);
    }

    @Test
    void play_whenPlayerIsStopped_setsStateToLoadingAndStartsPlayback() {
        // Act
        lavalinkPlayer.play(testTrack1);

        // Assert
        assertEquals(PlayerState.LOADING, lavalinkPlayer.getState(), "Player state should be LOADING");
        verify(mockPlayerManager).loadItem(eq(testTrack1.identifier()), any());
    }

    @Test
    void play_whenPlayerIsPlaying_onlyAddsToQueue() {
        // Arrange
        lavalinkPlayer.state.set(PlayerState.PLAYING);

        // Act
        lavalinkPlayer.play(testTrack1);

        // Assert
        assertEquals(PlayerState.PLAYING, lavalinkPlayer.getState(), "Player state should remain PLAYING");
        assertEquals(1, lavalinkPlayer.getQueue().size());
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
    void skip_whenTracksInQueue_loadsNextTrack() {
        // Arrange
        lavalinkPlayer.play(testTrack1);
        lavalinkPlayer.play(testTrack2);
        lavalinkPlayer.state.set(PlayerState.PLAYING);

        // Act
        lavalinkPlayer.skip();

        // Assert
        verify(mockPlayerManager).loadItem(eq(testTrack2.identifier()), any());
    }

    @Test
    void onTrackEnd_whenQueueIsEmpty_setsStateToStopped() {
        // Act
        lavalinkPlayer.onTrackEnd();

        // Assert
        assertEquals(PlayerState.STOPPED, lavalinkPlayer.getState());
        assertTrue(lavalinkPlayer.getCurrentlyPlaying().isEmpty());
        verify(mockLavaplayer).stopTrack();
    }

    @Test
    void getCurrentlyPlaying_whenNoTrackIsPlaying_returnsEmptyOptional() {
        // Act
        Optional<Track> currentTrack = lavalinkPlayer.getCurrentlyPlaying();

        // Assert
        assertTrue(currentTrack.isEmpty());
    }

    @Test
    void getCurrentlyPlaying_whenTrackIsPlaying_returnsCurrentTrack() {
        // Arrange
        lavalinkPlayer.play(testTrack1);

        // Act
        Optional<Track> currentTrack = lavalinkPlayer.getCurrentlyPlaying();

        // Assert
        assertTrue(currentTrack.isPresent());
        assertEquals(testTrack1, currentTrack.get());
    }

    @Test
    void getQueue_whenTracksAreQueued_returnsListOfTracks() {
        // Arrange
        lavalinkPlayer.play(testTrack1);
        lavalinkPlayer.state.set(PlayerState.PLAYING);
        lavalinkPlayer.play(testTrack2);

        // Act
        var queue = lavalinkPlayer.getQueue();

        // Assert
        assertEquals(1, queue.size());
        assertTrue(queue.contains(testTrack2));
    }

    @Test
    void clearQueue_whenCalled_clearsAllTracks() {
        // Arrange
        lavalinkPlayer.play(testTrack1);
        lavalinkPlayer.play(testTrack2);

        // Act
        lavalinkPlayer.clearQueue();

        // Assert
        assertTrue(lavalinkPlayer.getQueue().isEmpty());
    }

    @Test
    void stop_whenCalled_stopsPlaybackAndClearsQueue() {
        // Arrange
        lavalinkPlayer.play(testTrack1);
        lavalinkPlayer.play(testTrack2);

        // Act
        lavalinkPlayer.stopAndClear();

        // Assert
        assertTrue(lavalinkPlayer.getQueue().isEmpty());
        assertTrue(lavalinkPlayer.getCurrentlyPlaying().isEmpty());
        assertEquals(PlayerState.STOPPED, lavalinkPlayer.getState());
        verify(mockLavaplayer).stopTrack();
    }

    @Test
    void previous_whenNoPreviousTrack_returnsEmptyOptional() {
        // Act
        Optional<Track> previousTrack = lavalinkPlayer.previous();

        // Assert
        assertTrue(previousTrack.isEmpty());
    }
}
