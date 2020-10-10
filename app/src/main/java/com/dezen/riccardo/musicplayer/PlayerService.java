package com.dezen.riccardo.musicplayer;

import android.app.Notification;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.dezen.riccardo.musicplayer.song.Song;
import com.dezen.riccardo.musicplayer.song.SongManager;

import java.io.IOException;
import java.util.List;

/**
 * Service that manages the playback of songs.
 *
 * @author Riccardo De Zen.
 */
public class PlayerService extends MediaBrowserServiceCompat {

    public static final String LOG_TAG = "PlayerService";
    public static final int NOTIFICATION_ID = 1234;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private SongManager songManager;
    private MediaPlayer mediaPlayer;
    private int currentSong = 0;

    /**
     * When created the Service makes sure the notification channel is enabled if needed.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Media player.
        mediaPlayer = new MediaPlayer();

        // Ensure notification channel is created.
        NotificationHelper.getInstance(this).createChannelIfNeeded();

        // Setup PlaybackStateBuilder.
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE;
        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(actions);

        // Create MediaSession.
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        mediaSession.setCallback(new PlayerCallback(this));

        // Set the token for this Service. Allows finding the Session from outside.
        setSessionToken(mediaSession.getSessionToken());

        // Call the Song Manager.
        songManager = SongManager.getInstance(this);
    }

    /**
     * TODO not yet implemented, song list is provided via other classes.
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 @Nullable Bundle rootHints) {
        return new BrowserRoot("HELLO_I_AM_TEMPORARY", null);
    }

    /**
     * TODO not yet implemented, song list is provided via other classes.
     */
    @Override
    public void onLoadChildren(@NonNull String parentId,
                               @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    /**
     * Method to retrieve the Notification for this Service.
     * Must be called after {@link PlayerService#play()}. Otherwise the MediaSession will have null
     * Metadata.
     *
     * @return The {@link Notification} for this Service, built through {@link NotificationHelper}.
     */
    public Notification getNotification() {
        return NotificationHelper.getInstance(this).getServiceNotification(this);
    }

    /**
     * @return The {@link MediaSessionCompat} for this Service.
     */
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    /**
     * Method to start playing the currently selected song.
     */
    public void play() {
        List<Song> songs = songManager.getSongs().getValue();
        if (songs == null) return;

        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            Log.d("PlayerService", "Playing song " + songs.get(currentSong).getTitle());
            mediaPlayer.setDataSource(songs.get(currentSong).getDataPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, "Impossibile riprodurre il file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Method to pause the playback of a song.
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Method to stop the playback of a song.
     */
    public void stopPlayer() {
        mediaPlayer.stop();
    }

    /**
     * When the Service is destroyed the media player is released.
     */
    @Override
    public void onDestroy() {
        stopPlayer();
        mediaSession.release();
        mediaPlayer.release();
        super.onDestroy();
    }
}
