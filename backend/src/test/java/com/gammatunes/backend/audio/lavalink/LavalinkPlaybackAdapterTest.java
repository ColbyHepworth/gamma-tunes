//package com.gammatunes.backend.audio.lavalink;
//
//import com.gammatunes.backend.domain.model.PlayerState;
//import com.gammatunes.backend.domain.model.Session;
//import com.gammatunes.backend.domain.model.Track;
//import com.gammatunes.backend.infrastructure.lavalink.LavalinkPlaybackAdapter;
//import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.Duration;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for the LavalinkPlayer adapter class.
// */
//@ExtendWith(MockitoExtension.class)
//class LavalinkPlaybackAdapterTest {
//
//    @Mock
//    private com.sedmelluq.discord.lavaplayer.player.AudioPlayer mockLavaplayer;
//
//    @Mock
//    private AudioPlayerManager mockPlayerManager;
//
//    private LavalinkPlaybackAdapter lavalinkPlaybackAdapter;
//
//    private final Session testSession = new Session("12345");
//    private final Track testTrack1 = new Track("id1", "Song 1", "Artist 1", Duration.ofMinutes(3), "url1", null);
//    private final Track testTrack2 = new Track("id2", "Song 2", "Artist 2", Duration.ofMinutes(4), "url2", null);
//
//    @BeforeEach
//    void setUp() {
//        lavalinkPlaybackAdapter = new LavalinkPlaybackAdapter(testSession, mockLavaplayer, mockPlayerManager);
//    }
//
//    @Test
//    void play_whenPlayerIsStopped_setsStateToLoadingAndStartsPlayback() {
//        // Act
//        lavalinkPlaybackAdapter.play(testTrack1);
//
//        // Assert
//        assertEquals(PlayerState.LOADING, lavalinkPlaybackAdapter.getState(), "Player state should be LOADING");
//        verify(mockPlayerManager).loadItem(eq(testTrack1.identifier()), any());
//    }
//
//    @Test
//    void play_whenPlayerIsPlaying_onlyAddsToQueue() {
//        // Arrange
//        lavalinkPlaybackAdapter.state.set(PlayerState.PLAYING);
//
//        // Act
//        lavalinkPlaybackAdapter.play(testTrack1);
//
//        // Assert
//        assertEquals(PlayerState.PLAYING, lavalinkPlaybackAdapter.getState(), "Player state should remain PLAYING");
//        assertEquals(1, lavalinkPlaybackAdapter.getQueue().size());
//        verify(mockPlayerManager, never()).loadItem(anyString(), any());
//    }
//
//    @Test
//    void pause_whenPlayerIsPlaying_pausesAndChangesState() {
//        // Arrange
//        lavalinkPlaybackAdapter.state.set(PlayerState.PLAYING);
//
//        // Act
//        lavalinkPlaybackAdapter.pause();
//
//        // Assert
//        verify(mockLavaplayer).setPaused(true);
//        assertEquals(PlayerState.PAUSED, lavalinkPlaybackAdapter.getState());
//    }
//
//    @Test
//    void resume_whenPlayerIsPaused_resumesAndChangesState() {
//        // Arrange
//        lavalinkPlaybackAdapter.state.set(PlayerState.PAUSED);
//
//        // Act
//        lavalinkPlaybackAdapter.resume();
//
//        // Assert
//        verify(mockLavaplayer).setPaused(false);
//        assertEquals(PlayerState.PLAYING, lavalinkPlaybackAdapter.getState());
//    }
//
//    @Test
//    void skip_whenTracksInQueue_loadsNextTrack() {
//        // Arrange
//        lavalinkPlaybackAdapter.play(testTrack1);
//        lavalinkPlaybackAdapter.play(testTrack2);
//        lavalinkPlaybackAdapter.state.set(PlayerState.PLAYING);
//
//        // Act
//        lavalinkPlaybackAdapter.skip();
//
//        // Assert
//        verify(mockPlayerManager).loadItem(eq(testTrack2.identifier()), any());
//    }
//
//    @Test
//    void onTrackEnd_whenQueueIsEmpty_setsStateToStopped() {
//        // Act
//        lavalinkPlaybackAdapter.onTrackEnd();
//
//        // Assert
//        assertEquals(PlayerState.STOPPED, lavalinkPlaybackAdapter.getState());
//        assertTrue(lavalinkPlaybackAdapter.getCurrentlyPlaying().isEmpty());
//        verify(mockLavaplayer).stopTrack();
//    }
//
//    @Test
//    void getCurrentlyPlaying_whenNoTrackIsPlaying_returnsEmptyOptional() {
//        // Act
//        Optional<Track> currentTrack = lavalinkPlaybackAdapter.getCurrentlyPlaying();
//
//        // Assert
//        assertTrue(currentTrack.isEmpty());
//    }
//
//    @Test
//    void getCurrentlyPlaying_whenTrackIsPlaying_returnsCurrentTrack() {
//        // Arrange
//        lavalinkPlaybackAdapter.play(testTrack1);
//
//        // Act
//        Optional<Track> currentTrack = lavalinkPlaybackAdapter.getCurrentlyPlaying();
//
//        // Assert
//        assertTrue(currentTrack.isPresent());
//        assertEquals(testTrack1, currentTrack.get());
//    }
//
//    @Test
//    void getQueue_whenTracksAreQueued_returnsListOfTracks() {
//        // Arrange
//        lavalinkPlaybackAdapter.play(testTrack1);
//        lavalinkPlaybackAdapter.state.set(PlayerState.PLAYING);
//        lavalinkPlaybackAdapter.play(testTrack2);
//
//        // Act
//        var queue = lavalinkPlaybackAdapter.getQueue();
//
//        // Assert
//        assertEquals(1, queue.size());
//        assertTrue(queue.contains(testTrack2));
//    }
//
//    @Test
//    void clearQueue_whenCalled_clearsAllTracks() {
//        // Arrange
//        lavalinkPlaybackAdapter.play(testTrack1);
//        lavalinkPlaybackAdapter.play(testTrack2);
//
//        // Act
//        lavalinkPlaybackAdapter.clearQueue();
//
//        // Assert
//        assertTrue(lavalinkPlaybackAdapter.getQueue().isEmpty());
//    }
//
//    @Test
//    void stop_whenCalled_stopsPlaybackAndClearsQueue() {
//        // Arrange
//        lavalinkPlaybackAdapter.play(testTrack1);
//        lavalinkPlaybackAdapter.play(testTrack2);
//
//        // Act
//        lavalinkPlaybackAdapter.stop();
//
//        // Assert
//        assertTrue(lavalinkPlaybackAdapter.getQueue().isEmpty());
//        assertTrue(lavalinkPlaybackAdapter.getCurrentlyPlaying().isEmpty());
//        assertEquals(PlayerState.STOPPED, lavalinkPlaybackAdapter.getState());
//        verify(mockLavaplayer).stopTrack();
//    }
//
//    @Test
//    void previous_whenNoPreviousTrack_returnsEmptyOptional() {
//        // Act
//        Optional<Track> previousTrack = lavalinkPlaybackAdapter.previous();
//
//        // Assert
//        assertTrue(previousTrack.isEmpty());
//    }
//}
